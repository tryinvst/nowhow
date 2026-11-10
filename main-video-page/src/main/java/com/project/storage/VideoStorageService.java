package com.project.storage;

import com.project.client.UserProfileClient;
import com.project.common.Category;
import com.project.common.ImageFormat;
import com.project.dto.PageResponseDto;
import com.project.dto.VideoResponseDto;
import com.project.entity.Course;
import com.project.entity.CourseStatus;
import com.project.entity.Video;
import com.project.entity.VideoStatus;
import com.project.exception.ApiException;
import com.project.exception.VideoNotFoundException;
import com.project.media.MediaTools;
import com.project.moderation.ModerationService;
import com.project.playback.UrlSigner;
import com.project.repository.CourseRepository;
import com.project.repository.ModerationFlagRepository;
import com.project.repository.VideoRepository;
import com.project.security.Viewer;
import com.project.service.LessonPositions;
import com.project.service.VideoMapper;
import com.project.service.VideoVisibilityPolicy;
import com.project.util.DurationFormatter;
import com.project.util.LikePatternBuilder;
import com.project.util.PageRequestFactory;
import com.project.util.UploadedFiles;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VideoStorageService {
    private static final Logger log = LoggerFactory.getLogger(VideoStorageService.class);

    private final VideoRepository videoRepository;
    private final CourseRepository courseRepository;
    private final ModerationFlagRepository flagRepository;
    private final UserProfileClient userProfileClient;
    private final VideoFiles videoFiles;
    private final CoverStorage coverStorage;
    private final MediaTools mediaTools;
    private final ModerationService moderationService;
    private final VideoMapper videoMapper;
    private final LessonPositions lessonPositions;
    private final UrlSigner urlSigner;
    private final Duration maxDuration;
    private final VideoVisibilityPolicy visibilityPolicy;
    private final PageRequestFactory pageRequestFactory;
    private final LikePatternBuilder likePatternBuilder;
    private final DurationFormatter durationFormatter;
    private final UploadedFiles uploadedFiles;

    public VideoStorageService(VideoRepository videoRepository, CourseRepository courseRepository,
                               ModerationFlagRepository flagRepository, UserProfileClient userProfileClient,
                               VideoFiles videoFiles, CoverStorage coverStorage, MediaTools mediaTools,
                               ModerationService moderationService, VideoMapper videoMapper,
                               LessonPositions lessonPositions, UrlSigner urlSigner,
                               @Value("${app.video.max-duration}") Duration maxDuration,
                               VideoVisibilityPolicy visibilityPolicy, PageRequestFactory pageRequestFactory,
                               LikePatternBuilder likePatternBuilder, DurationFormatter durationFormatter,
                               UploadedFiles uploadedFiles) {
        this.videoRepository = videoRepository;
        this.courseRepository = courseRepository;
        this.flagRepository = flagRepository;
        this.userProfileClient = userProfileClient;
        this.videoFiles = videoFiles;
        this.coverStorage = coverStorage;
        this.mediaTools = mediaTools;
        this.moderationService = moderationService;
        this.videoMapper = videoMapper;
        this.lessonPositions = lessonPositions;
        this.urlSigner = urlSigner;
        this.maxDuration = maxDuration;
        this.visibilityPolicy = visibilityPolicy;
        this.pageRequestFactory = pageRequestFactory;
        this.likePatternBuilder = likePatternBuilder;
        this.durationFormatter = durationFormatter;
        this.uploadedFiles = uploadedFiles;
    }

    /**
     * Проверяем автора до записи файла, чтобы не оставлять мусор на диске.
     */
    public VideoResponseDto upload(String name, String description, Category category, MultipartFile file,
                                   MultipartFile cover, Viewer viewer) {
        validateText(name, description);
        if (category == null) {
            throw new IllegalArgumentException("Укажите категорию видео");
        }
        userProfileClient.checkUserExists(viewer.userId(), viewer.bearerToken());
        StoredFile stored = storeVideoFile(file);
        String coverFilename = null;
        try {
            coverFilename = uploadedFiles.hasContent(cover) ? coverStorage.save(cover) : null;
        } catch (RuntimeException e) {
            deleteFile(videoFiles.resolve(stored.storageFilename()));
            throw e;
        }
        Video video = new Video();
        video.setName(name.trim());
        video.setDescription(description);
        video.setCategory(category);
        video.setFilename(stored.originalFilename());
        video.setSize(stored.size());
        video.setContentType(stored.contentType());
        video.setDurationSeconds(stored.durationSeconds());
        video.setStorageFilename(stored.storageFilename());
        video.setAuthorId(viewer.userId());
        video.setCoverStorageFilename(coverFilename);
        video.setCustomCover(coverFilename != null);
        return videoMapper.toDto(requestModeration(video), viewer);
    }

    /**
     * Любое изменение отправляет видео на повторную автопроверку.
     */
    public VideoResponseDto update(Long id, String name, String description, Category category, MultipartFile file,
                                   Viewer viewer) {
        Video video = getManageable(id, viewer);
        if (name == null && description == null && category == null && !uploadedFiles.hasContent(file)) {
            throw new IllegalArgumentException("Нечего обновлять: передайте name, description, category или file");
        }
        validateText(name == null ? video.getName() : name, description);
        if (name != null) {
            video.setName(name.trim());
        }
        if (description != null) {
            video.setDescription(description);
        }
        if (category != null) {
            video.setCategory(category);
        }
        String oldStorageFilename = null;
        if (uploadedFiles.hasContent(file)) {
            StoredFile stored = storeVideoFile(file);
            oldStorageFilename = video.getStorageFilename();
            video.setFilename(stored.originalFilename());
            video.setSize(stored.size());
            video.setContentType(stored.contentType());
            video.setDurationSeconds(stored.durationSeconds());
            video.setStorageFilename(stored.storageFilename());
            video.setTranscript(null);
        }
        Video saved = requestModeration(video);
        if (oldStorageFilename != null) {
            deleteFile(videoFiles.resolve(oldStorageFilename));
        }
        return videoMapper.toDto(saved, viewer);
    }

    /**
     * Своя обложка не влияет на автопроверку видео, но кадр из видео больше не подставляется.
     */
    public VideoResponseDto setCover(Long id, MultipartFile cover, Viewer viewer) {
        Video video = getManageable(id, viewer);
        if (!uploadedFiles.hasContent(cover)) {
            throw new IllegalArgumentException("Файл обложки не выбран");
        }
        String old = video.getCoverStorageFilename();
        video.setCoverStorageFilename(coverStorage.save(cover));
        video.setCustomCover(true);
        Video saved = videoRepository.save(video);
        coverStorage.delete(old);
        return videoMapper.toDto(saved, viewer);
    }

    /**
     * Вернуть обложку-кадр из видео.
     */
    public VideoResponseDto resetCover(Long id, Viewer viewer) {
        Video video = getManageable(id, viewer);
        Path tmp = null;
        try {
            tmp = Files.createTempFile("nowhow-cover", ".jpg");
            double duration = video.getDurationSeconds() == null ? 0 : video.getDurationSeconds();
            mediaTools.extractCover(videoFiles.resolve(video.getStorageFilename()), Math.min(3.0, duration / 3), tmp);
            String old = video.getCoverStorageFilename();
            video.setCoverStorageFilename(coverStorage.saveGenerated(tmp));
            video.setCustomCover(false);
            Video saved = videoRepository.save(video);
            coverStorage.delete(old);
            return videoMapper.toDto(saved, viewer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (tmp != null) {
                deleteFile(tmp);
            }
        }
    }

    /**
     * Сначала запись в БД, потом файлы: если файл не удалится, останется мусор на диске, а не запись без файла.
     */
    public void deleteVideo(Long id, Viewer viewer) {
        Video video = getManageable(id, viewer);
        videoRepository.delete(video);
        flagRepository.deleteAllByVideoId(id);
        if (video.getCourseId() != null) {
            lessonPositions.renumber(video.getCourseId());
        }
        deleteFile(videoFiles.resolve(video.getStorageFilename()));
        try {
            coverStorage.delete(video.getCoverStorageFilename());
        } catch (UncheckedIOException e) {
            log.warn("Не удалось удалить обложку видео {}", id, e);
        }
    }

    public VideoResponseDto getVideoInfo(Long id, Viewer viewer) {
        return videoMapper.toDto(getVisible(id, viewer), viewer);
    }

    /**
     * Каталог: только опубликованные видео вне курсов (уроки показываются внутри курса)
     */
    public PageResponseDto<VideoResponseDto> listPublic(int page, int size, Category category, Long authorId,
                                                        String query, Viewer viewer) {
        Specification<Video> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), VideoStatus.PUBLISHED));
            predicates.add(cb.isNull(root.get("courseId")));
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (authorId != null) {
                predicates.add(cb.equal(root.get("authorId"), authorId));
            }
            if (query != null && !query.isBlank()) {
                String like = likePatternBuilder.contains(query);
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), like, LikePatternBuilder.ESCAPE),
                        cb.like(cb.lower(root.get("description")), like, LikePatternBuilder.ESCAPE)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<Video> videos = videoRepository.findAll(spec, newestFirst(page, size));
        return PageResponseDto.of(videos, videoMapper.toDtos(videos.getContent(), viewer));
    }

    /**
     * Все свои видео в любом статусе - для кабинета мастера.
     */
    public PageResponseDto<VideoResponseDto> listMine(int page, int size, Viewer viewer) {
        Page<Video> videos = videoRepository.findAllByAuthorId(viewer.userId(), newestFirst(page, size));
        return PageResponseDto.of(videos, videoMapper.toDtos(videos.getContent(), viewer));
    }

    /**
     * Обложка: публичного видео - всем, остальных - по подписанной ссылке.
     */
    public CoverFile loadCover(Long id, String filename, Long exp, String sig) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new VideoNotFoundException(id));
        if (video.getCoverStorageFilename() == null || !video.getCoverStorageFilename().equals(filename)) {
            throw new VideoNotFoundException(id);
        }
        if (!visibilityPolicy.isPublic(video, courseStatus(video)) && !urlSigner.verify("cover", id, exp, sig)) {
            throw new VideoNotFoundException(id);
        }
        return new CoverFile(coverStorage.resolve(filename), ImageFormat.fromFilename(filename));
    }

    /**
     * Чужие непубличные видео не палим даже статусом 403.
     */
    public Video getVisible(Long id, Viewer viewer) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new VideoNotFoundException(id));
        if (!viewer.canManage(video.getAuthorId()) && !visibilityPolicy.isPublic(video, courseStatus(video))) {
            throw new VideoNotFoundException(id);
        }
        return video;
    }

    public Path videoPath(Video video) {
        Path path = videoFiles.resolve(video.getStorageFilename());
        if (!Files.exists(path)) {
            throw new VideoNotFoundException("Файл видео с id " + video.getId() + " не найден");
        }
        return path;
    }

    private Video getManageable(Long id, Viewer viewer) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new VideoNotFoundException(id));
        if (!viewer.canManage(video.getAuthorId())) {
            if (visibilityPolicy.isPublic(video, courseStatus(video))) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Менять и удалять видео может только его автор");
            }
            throw new VideoNotFoundException(id);
        }
        return video;
    }

    private Video requestModeration(Video video) {
        video.setStatus(VideoStatus.PROCESSING);
        video.setModerationComment(null);
        video.setModerationRun(video.getModerationRun() + 1);
        Video saved = videoRepository.save(video);
        moderationService.moderateAsync(saved.getId(), saved.getModerationRun());
        return saved;
    }

    /**
     * Файл → uploads/videos, сразу проверяем, что это видео и что оно не длиннее лимита.
     * <p>Секунда запаса: телефоны пишут 15:00 как 900.4 с.
     */
    private StoredFile storeVideoFile(MultipartFile file) {
        if (!uploadedFiles.hasContent(file)) {
            throw new IllegalArgumentException ("видео-файл не выбран");
        }
        String contentType = file.getContentType ();
        if (contentType == null || !contentType.startsWith ("video/")) {
            throw new IllegalArgumentException ("можно загружать только видео");
        }
        String originalFileName = file.getOriginalFilename ();
        if (originalFileName == null) {
            throw new IllegalArgumentException ("у файла отсутствует название");
        }
        originalFileName = Paths.get (originalFileName).getFileName ().toString ();
        String storageFileName = UUID.randomUUID () + getExtention (originalFileName);
        Path targetPath = videoFiles.resolve (storageFileName);
        try {
            Files.copy ( file.getInputStream (),targetPath,StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new UncheckedIOException ( e );
        }
        Double duration = mediaTools.probeDurationSeconds(targetPath);
        if (duration == null) {
            deleteFile(targetPath);
            throw new IllegalArgumentException("Файл не читается как видео");
        }
        if (duration > maxDuration.toSeconds() + 1) {
            deleteFile(targetPath);
            throw new IllegalArgumentException("Видео длиннее " + maxDuration.toMinutes() + " минут ("
                    + durationFormatter.minutesAndSeconds(duration) + "), сократите его");
        }
        return new StoredFile(originalFileName, storageFileName, contentType, file.getSize(), duration);
    }

    private void validateText(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Укажите название видео");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Название - не длиннее 200 символов");
        }
        if (description != null && description.length() > 5000) {
            throw new IllegalArgumentException("Описание - не длиннее 5000 символов");
        }
    }

    private CourseStatus courseStatus(Video video) {
        if (video.getCourseId() == null) {
            return null;
        }
        return courseRepository.findById(video.getCourseId()).map(Course::getStatus).orElse(null);
    }

    private Pageable newestFirst(int page, int size) {
        return pageRequestFactory.create(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String getExtention (String fileName) {
        int dotPossition = fileName.lastIndexOf ('.');
        if (dotPossition == -1) {
            return "";
        }
        return fileName.substring(dotPossition);
    }

    private void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Не удалось удалить файл {}", path, e);
        }
    }

    private record StoredFile(String originalFilename, String storageFilename, String contentType, long size,
                              double durationSeconds) { }

    public record CoverFile(Path path, ImageFormat format) { }
}
