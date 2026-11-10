package com.project.service;

import com.project.dto.AuthorDto;
import com.project.dto.CategoryDto;
import com.project.dto.CourseRequestDto;
import com.project.dto.CourseResponseDto;
import com.project.dto.PageResponseDto;
import com.project.dto.VideoResponseDto;
import com.project.entity.Course;
import com.project.entity.CourseStatus;
import com.project.entity.ModerationFlag;
import com.project.entity.Video;
import com.project.entity.VideoStatus;
import com.project.exception.ApiException;
import com.project.exception.CourseNotFoundException;
import com.project.moderation.TextModerator;
import com.project.repository.CourseRepository;
import com.project.repository.ModerationFlagRepository;
import com.project.repository.VideoRepository;
import com.project.repository.projection.LessonStats;
import com.project.security.Viewer;
import com.project.util.LikePatternBuilder;
import com.project.util.PageRequestFactory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Курсы: черновик → публикация (уроки прошли автопроверку + текст курса чист) или на модерацию.
 */
@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final VideoRepository videoRepository;
    private final ModerationFlagRepository flagRepository;
    private final TextModerator textModerator;
    private final VideoMapper videoMapper;
    private final LessonPositions lessonPositions;
    private final VideoVisibilityPolicy visibilityPolicy;
    private final PageRequestFactory pageRequestFactory;
    private final LikePatternBuilder likePatternBuilder;

    public CourseService(CourseRepository courseRepository, VideoRepository videoRepository,
                         ModerationFlagRepository flagRepository, TextModerator textModerator,
                         VideoMapper videoMapper, LessonPositions lessonPositions,
                         VideoVisibilityPolicy visibilityPolicy, PageRequestFactory pageRequestFactory,
                         LikePatternBuilder likePatternBuilder) {
        this.courseRepository = courseRepository;
        this.videoRepository = videoRepository;
        this.flagRepository = flagRepository;
        this.textModerator = textModerator;
        this.videoMapper = videoMapper;
        this.lessonPositions = lessonPositions;
        this.visibilityPolicy = visibilityPolicy;
        this.pageRequestFactory = pageRequestFactory;
        this.likePatternBuilder = likePatternBuilder;
    }

    public CourseResponseDto create(CourseRequestDto requestDto, Viewer viewer) {
        Course course = new Course();
        course.setAuthorId(viewer.userId());
        course.updateDetails(requestDto.title(), requestDto.description(), requestDto.category());
        course.setStatus(CourseStatus.DRAFT);
        return toDto(courseRepository.save(course), viewer, true);
    }

    /**
     * Правка опубликованного курса: текст проверяется заново, при находке курс уходит на модерацию.
     */
    @Transactional
    public CourseResponseDto update(Long id, CourseRequestDto requestDto, Viewer viewer) {
        Course course = getManageable(id, viewer);
        course.updateDetails(requestDto.title(), requestDto.description(), requestDto.category());
        if (course.getStatus() == CourseStatus.REJECTED) {
            course.setStatus(CourseStatus.DRAFT);
            course.setModerationComment(null);
        } else if (course.getStatus() == CourseStatus.PUBLISHED) {
            moderateText(course);
        }
        return toDto(courseRepository.save(course), viewer, true);
    }

    /**
     * Уроки не удаляются, а становятся отдельными видео мастера.
     */
    @Transactional
    public void delete(Long id, Viewer viewer) {
        Course course = getManageable(id, viewer);
        List<Video> lessons = videoRepository.findAllByCourseIdOrderByLessonPositionAsc(id);
        lessons.forEach(Video::detachFromCourse);
        videoRepository.saveAll(lessons);
        flagRepository.deleteAllByCourseId(id);
        courseRepository.delete(course);
    }

    public CourseResponseDto get(Long id, Viewer viewer) {
        return toDto(getVisible(id, viewer), viewer, true);
    }

    public PageResponseDto<CourseResponseDto> listPublic(int page, int size, com.project.common.Category category,
                                                         Long authorId, String query, Viewer viewer) {
        Specification<Course> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), CourseStatus.PUBLISHED));
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (authorId != null) {
                predicates.add(cb.equal(root.get("authorId"), authorId));
            }
            if (query != null && !query.isBlank()) {
                String like = likePatternBuilder.contains(query);
                predicates.add(cb.or(cb.like(cb.lower(root.get("title")), like, LikePatternBuilder.ESCAPE),
                        cb.like(cb.lower(root.get("description")), like, LikePatternBuilder.ESCAPE)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<Course> courses = courseRepository.findAll(spec,
                pageRequestFactory.create(page, size, Sort.by(Sort.Direction.DESC, "publishedAt")));
        return PageResponseDto.of(courses, toDtos(courses.getContent(), viewer));
    }

    public PageResponseDto<CourseResponseDto> listMine(int page, int size, Viewer viewer) {
        Page<Course> courses = courseRepository.findAllByAuthorId(viewer.userId(),
                pageRequestFactory.create(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponseDto.of(courses, toDtos(courses.getContent(), viewer));
    }

    /**
     * Видео становится последним уроком; первый урок по умолчанию бесплатный.
     */
    @Transactional
    public CourseResponseDto addLesson(Long courseId, Long videoId, Boolean freePreview, Viewer viewer) {
        Course course = getManageable(courseId, viewer);
        Video video = videoRepository.findById(videoId)
                .filter(v -> v.getAuthorId().equals(course.getAuthorId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Видео " + videoId + " не найдено среди видео автора курса"));
        if (video.getCourseId() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "Видео уже является уроком курса " + video.getCourseId());
        }
        int position = videoRepository.findAllByCourseIdOrderByLessonPositionAsc(courseId).size() + 1;
        video.setCourseId(courseId);
        video.setLessonPosition(position);
        video.setFreePreview(freePreview != null ? freePreview : position == 1);
        videoRepository.save(video);
        return toDto(course, viewer, true);
    }

    @Transactional
    public CourseResponseDto setFreePreview(Long courseId, Long videoId, boolean freePreview, Viewer viewer) {
        Course course = getManageable(courseId, viewer);
        Video lesson = getLesson(courseId, videoId);
        lesson.setFreePreview(freePreview);
        videoRepository.save(lesson);
        return toDto(course, viewer, true);
    }

    @Transactional
    public CourseResponseDto reorder(Long courseId, List<Long> videoIds, Viewer viewer) {
        Course course = getManageable(courseId, viewer);
        List<Video> lessons = videoRepository.findAllByCourseIdOrderByLessonPositionAsc(courseId);
        Set<Long> current = lessons.stream().map(Video::getId).collect(Collectors.toSet());
        if (videoIds.size() != current.size() || !current.equals(new HashSet<>(videoIds))) {
            throw new IllegalArgumentException("videoIds должны содержать все уроки курса ровно по одному разу");
        }
        Map<Long, Video> byId = lessons.stream().collect(Collectors.toMap(Video::getId, v -> v));
        for (int i = 0; i < videoIds.size(); i++) {
            byId.get(videoIds.get(i)).setLessonPosition(i + 1);
        }
        videoRepository.saveAll(lessons);
        return toDto(course, viewer, true);
    }

    /**
     * Урок снова становится отдельным видео мастера.
     */
    @Transactional
    public CourseResponseDto removeLesson(Long courseId, Long videoId, Viewer viewer) {
        Course course = getManageable(courseId, viewer);
        Video lesson = getLesson(courseId, videoId);
        lesson.detachFromCourse();
        videoRepository.save(lesson);
        lessonPositions.renumber(courseId);
        return toDto(course, viewer, true);
    }

    @Transactional
    public CourseResponseDto publish(Long id, Viewer viewer) {
        Course course = getManageable(id, viewer);
        if (course.getStatus() == CourseStatus.PUBLISHED) {
            return toDto(course, viewer, true);
        }
        if (course.getStatus() == CourseStatus.ON_REVIEW) {
            throw new ApiException(HttpStatus.CONFLICT, "Курс уже на модерации");
        }
        List<Video> lessons = videoRepository.findAllByCourseIdOrderByLessonPositionAsc(id);
        if (lessons.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Добавьте в курс хотя бы один урок");
        }
        List<String> notReady = lessons.stream()
                .filter(lesson -> lesson.getStatus() != VideoStatus.PUBLISHED)
                .map(lesson -> "«" + lesson.getName() + "» (" + lesson.getStatus() + ")")
                .toList();
        if (!notReady.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Уроки ещё не прошли проверку: " + String.join(", ", notReady));
        }
        course.setStatus(CourseStatus.PUBLISHED);
        moderateText(course);
        return toDto(courseRepository.save(course), viewer, true);
    }

    @Transactional
    public CourseResponseDto unpublish(Long id, Viewer viewer) {
        Course course = getManageable(id, viewer);
        course.setStatus(CourseStatus.DRAFT);
        course.setModerationComment(null);
        return toDto(courseRepository.save(course), viewer, true);
    }

    public Course getManageable(Long id, Viewer viewer) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        if (!viewer.canManage(course.getAuthorId())) {
            if (course.getStatus() == CourseStatus.PUBLISHED) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Менять курс может только его автор");
            }
            throw new CourseNotFoundException(id);
        }
        return course;
    }

    public List<CourseResponseDto> toDtos(List<Course> courses, Viewer viewer) {
        if (courses.isEmpty()) {
            return List.of();
        }
        List<Long> ids = courses.stream().map(Course::getId).toList();
        Map<Long, LessonStats> stats = videoRepository.lessonStats(ids).stream()
                .collect(Collectors.toMap(LessonStats::courseId, Function.identity()));
        Map<Long, Video> firstLessons = videoRepository.findAllByCourseIdInAndLessonPosition(ids, 1).stream()
                .collect(Collectors.toMap(Video::getCourseId, v -> v));
        Map<Long, AuthorDto> authors = videoMapper.authors(courses.stream().map(Course::getAuthorId).toList());
        return courses.stream().map(course -> {
            Video first = firstLessons.get(course.getId());
            LessonStats courseStats = stats.getOrDefault(course.getId(), new LessonStats(course.getId(), 0L, 0.0));
            String coverUrl = first == null ? null
                    : videoMapper.coverUrl(first, visibilityPolicy.isPublic(first, course.getStatus()));
            return new CourseResponseDto(course.getId(), course.getTitle(), course.getDescription(),
                    CategoryDto.from(course.getCategory()), course.getStatus().name(),
                    viewer.canManage(course.getAuthorId()) ? course.getModerationComment() : null,
                    authors.getOrDefault(course.getAuthorId(), new AuthorDto(course.getAuthorId(), null, null, null, null)),
                    courseStats.lessonsCount(), courseStats.totalDurationSeconds(), coverUrl,
                    course.getCreatedAt(), course.getPublishedAt(), null);
        }).toList();
    }

    /**
     * Курс с уроками: гость и ученик видят только опубликованные уроки.
     */
    private CourseResponseDto toDto(Course course, Viewer viewer, boolean withLessons) {
        CourseResponseDto base = toDtos(List.of(course), viewer).get(0);
        if (!withLessons) {
            return base;
        }
        boolean manage = viewer.canManage(course.getAuthorId());
        List<Video> lessons = videoRepository.findAllByCourseIdOrderByLessonPositionAsc(course.getId()).stream()
                .filter(lesson -> manage || lesson.getStatus() == VideoStatus.PUBLISHED)
                .toList();
        List<VideoResponseDto> lessonDtos = videoMapper.toDtos(lessons, viewer);
        return new CourseResponseDto(base.id(), base.title(), base.description(), base.category(), base.status(),
                base.moderationComment(), base.author(), base.lessonsCount(), base.totalDurationSeconds(),
                base.coverUrl(), base.createdAt(), base.publishedAt(), lessonDtos);
    }

    private Course getVisible(Long id, Viewer viewer) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        if (course.getStatus() != CourseStatus.PUBLISHED && !viewer.canManage(course.getAuthorId())) {
            throw new CourseNotFoundException(id);
        }
        return course;
    }

    /**
     * Находка в названии/описании → ON_REVIEW, иначе статус не меняется.
     */
    private void moderateText(Course course) {
        flagRepository.deleteAllByCourseId(course.getId());
        List<TextModerator.Match> matches = textModerator.check(course.getTitle() + "\n"
                + (course.getDescription() == null ? "" : course.getDescription()));
        if (matches.isEmpty()) {
            if (course.getPublishedAt() == null) {
                course.setPublishedAt(LocalDateTime.now());
            }
            course.setModerationComment(null);
            return;
        }
        flagRepository.saveAll(matches.stream()
                .map(match -> ModerationFlag.forCourse(course.getId(), match.category(),
                        "Название/описание курса: «" + match.fragment() + "»"))
                .toList());
        course.setStatus(CourseStatus.ON_REVIEW);
        course.setModerationComment("Автопроверка нашла спорные места, курс ждёт проверки модератором");
    }

    private Video getLesson(Long courseId, Long videoId) {
        return videoRepository.findById(videoId)
                .filter(v -> courseId.equals(v.getCourseId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Урок " + videoId + " не найден в курсе"));
    }

}
