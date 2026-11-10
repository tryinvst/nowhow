package com.project.moderation;

import com.project.dto.CourseReviewDto;
import com.project.dto.ModerationFlagDto;
import com.project.dto.ModerationRuleDto;
import com.project.dto.PageResponseDto;
import com.project.dto.VideoResponseDto;
import com.project.dto.VideoReviewDto;
import com.project.entity.Course;
import com.project.entity.CourseStatus;
import com.project.entity.ModerationFlag;
import com.project.entity.ModerationRule;
import com.project.entity.Video;
import com.project.entity.VideoStatus;
import com.project.exception.ApiException;
import com.project.repository.CourseRepository;
import com.project.repository.ModerationFlagRepository;
import com.project.repository.ModerationRuleRepository;
import com.project.repository.VideoRepository;
import com.project.security.Viewer;
import com.project.service.CourseService;
import com.project.service.VideoMapper;
import com.project.util.PageRequestFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ручные решения админа по очереди «На модерации» и ведение black-листа.
 */
@Service
public class ModerationAdminService {

    private final VideoRepository videoRepository;
    private final CourseRepository courseRepository;
    private final ModerationFlagRepository flagRepository;
    private final ModerationRuleRepository ruleRepository;
    private final VideoMapper videoMapper;
    private final CourseService courseService;
    private final PatternCompiler patternCompiler;
    private final PageRequestFactory pageRequestFactory;

    public ModerationAdminService(VideoRepository videoRepository, CourseRepository courseRepository,
                                  ModerationFlagRepository flagRepository, ModerationRuleRepository ruleRepository,
                                  VideoMapper videoMapper, CourseService courseService,
                                  PatternCompiler patternCompiler, PageRequestFactory pageRequestFactory) {
        this.videoRepository = videoRepository;
        this.courseRepository = courseRepository;
        this.flagRepository = flagRepository;
        this.ruleRepository = ruleRepository;
        this.videoMapper = videoMapper;
        this.courseService = courseService;
        this.patternCompiler = patternCompiler;
        this.pageRequestFactory = pageRequestFactory;
    }

    public PageResponseDto<VideoReviewDto> videoQueue(VideoStatus status, int page, int size, Viewer viewer) {
        Page<Video> videos = videoRepository.findAllByStatus(status,
                pageRequestFactory.create(page, size, Sort.by(Sort.Direction.ASC, "createdAt")));
        Map<Long, List<ModerationFlag>> flags = flagRepository
                .findAllByVideoIdIn(videos.getContent().stream().map(Video::getId).toList()).stream()
                .collect(Collectors.groupingBy(ModerationFlag::getVideoId));
        List<VideoResponseDto> dtos = videoMapper.toDtos(videos.getContent(), viewer);
        List<VideoReviewDto> items = dtos.stream()
                .map(dto -> new VideoReviewDto(dto, flags.getOrDefault(dto.id(), List.of()).stream()
                        .map(ModerationFlagDto::from).toList(), null))
                .toList();
        return PageResponseDto.of(videos, items);
    }

    public VideoReviewDto videoReview(Long id, Viewer viewer) {
        Video video = getVideo(id);
        return new VideoReviewDto(videoMapper.toDto(video, viewer),
                flagRepository.findAllByVideoIdOrderByTimecodeSecondsAsc(id).stream().map(ModerationFlagDto::from).toList(),
                video.getTranscript());
    }

    public VideoReviewDto decideVideo(Long id, boolean approve, String reason, Viewer viewer) {
        Video video = getVideo(id);
        if (video.getStatus() == VideoStatus.PROCESSING) {
            throw new ApiException(HttpStatus.CONFLICT, "Автопроверка ещё идёт, дождитесь её окончания");
        }
        if (!approve && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Укажите причину отклонения - её увидит мастер");
        }
        video.setStatus(approve ? VideoStatus.PUBLISHED : VideoStatus.REJECTED);
        video.setModerationComment(approve ? null : reason.trim());
        videoRepository.save(video);
        return videoReview(id, viewer);
    }

    public PageResponseDto<CourseReviewDto> courseQueue(CourseStatus status, int page, int size, Viewer viewer) {
        Page<Course> courses = courseRepository.findAllByStatus(status,
                pageRequestFactory.create(page, size, Sort.by(Sort.Direction.ASC, "updatedAt")));
        Map<Long, List<ModerationFlag>> flags = flagRepository
                .findAllByCourseIdIn(courses.getContent().stream().map(Course::getId).toList()).stream()
                .collect(Collectors.groupingBy(ModerationFlag::getCourseId));
        List<CourseReviewDto> items = courseService.toDtos(courses.getContent(), viewer).stream()
                .map(dto -> new CourseReviewDto(dto, flags.getOrDefault(dto.id(), List.of()).stream()
                        .map(ModerationFlagDto::from).toList()))
                .toList();
        return PageResponseDto.of(courses, items);
    }

    public CourseReviewDto decideCourse(Long id, boolean approve, String reason, Viewer viewer) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Курс с id " + id + " не найден"));
        if (!approve && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Укажите причину отклонения - её увидит мастер");
        }
        course.setStatus(approve ? CourseStatus.PUBLISHED : CourseStatus.REJECTED);
        course.setModerationComment(approve ? null : reason.trim());
        if (approve && course.getPublishedAt() == null) {
            course.setPublishedAt(LocalDateTime.now());
        }
        courseRepository.save(course);
        return new CourseReviewDto(courseService.toDtos(List.of(course), viewer).get(0),
                flagRepository.findAllByCourseId(id).stream().map(ModerationFlagDto::from).toList());
    }

    public List<ModerationRuleDto> rules() {
        return ruleRepository.findAll(Sort.by("category", "id")).stream()
                .map(rule -> new ModerationRuleDto(rule.getId(), rule.getCategory(), rule.getPattern(), rule.getComment()))
                .toList();
    }

    /**
     * Новые правила действуют для следующих проверок; уже опубликованное не перепроверяется.
     */
    public ModerationRuleDto addRule(ModerationRuleDto requestDto) {
        patternCompiler.validate(requestDto.pattern());
        ModerationRule rule = ruleRepository.save(
                new ModerationRule(requestDto.category(), requestDto.pattern(), requestDto.comment()));
        return new ModerationRuleDto(rule.getId(), rule.getCategory(), rule.getPattern(), rule.getComment());
    }

    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Правило " + id + " не найдено");
        }
        ruleRepository.deleteById(id);
    }

    private Video getVideo(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Видео с id " + id + " не найдено"));
    }
}
