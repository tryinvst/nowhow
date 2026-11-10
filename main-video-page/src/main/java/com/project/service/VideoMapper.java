package com.project.service;

import com.project.client.UserProfileClient;
import com.project.dto.AuthorDto;
import com.project.dto.CategoryDto;
import com.project.dto.VideoResponseDto;
import com.project.entity.Course;
import com.project.entity.CourseStatus;
import com.project.entity.Video;
import com.project.playback.UrlSigner;
import com.project.repository.CourseRepository;
import com.project.security.Viewer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сборка карточек видео: автор из userProfile одним запросом на страницу, ссылки на обложки.
 */
@Component
public class VideoMapper {

    private final String publicUrl;
    private final Duration urlTtl;
    private final UrlSigner urlSigner;
    private final UserProfileClient userProfileClient;
    private final CourseRepository courseRepository;
    private final VideoVisibilityPolicy visibilityPolicy;

    public VideoMapper(@Value("${app.public-url}") String publicUrl,
                       @Value("${app.playback.ttl}") Duration urlTtl,
                       UrlSigner urlSigner, UserProfileClient userProfileClient, CourseRepository courseRepository,
                       VideoVisibilityPolicy visibilityPolicy) {
        this.publicUrl = publicUrl;
        this.urlTtl = urlTtl;
        this.urlSigner = urlSigner;
        this.userProfileClient = userProfileClient;
        this.courseRepository = courseRepository;
        this.visibilityPolicy = visibilityPolicy;
    }

    public VideoResponseDto toDto(Video video, Viewer viewer) {
        return toDtos(List.of(video), viewer).get(0);
    }

    public List<VideoResponseDto> toDtos(List<Video> videos, Viewer viewer) {
        Map<Long, AuthorDto> authors = authors(videos.stream().map(Video::getAuthorId).toList());
        List<Long> courseIds = videos.stream().map(Video::getCourseId).filter(Objects::nonNull).distinct().toList();
        Map<Long, CourseStatus> courseStatuses = courseRepository.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getStatus));
        return videos.stream().map(video -> {
            boolean manage = viewer.canManage(video.getAuthorId());
            boolean isPublic = visibilityPolicy.isPublic(video, courseStatuses.get(video.getCourseId()));
            return new VideoResponseDto(video.getId(), video.getName(), video.getDescription(),
                    CategoryDto.from(video.getCategory()), video.getDurationSeconds(), coverUrl(video, isPublic),
                    video.getStatus().name(), manage ? video.getModerationComment() : null, video.getCreatedAt(),
                    authors.getOrDefault(video.getAuthorId(), new AuthorDto(video.getAuthorId(), null, null, null, null)),
                    video.getCourseId(), video.getLessonPosition(), video.isFreePreview(), video.getSize());
        }).toList();
    }

    public Map<Long, AuthorDto> authors(Collection<Long> authorIds) {
        return userProfileClient.findMasters(authorIds.stream().distinct().toList()).values().stream()
                .map(m -> new AuthorDto(m.id(), m.displayName(), m.specialization(), m.specializationTitle(), m.avatarUrl()))
                .collect(Collectors.toMap(AuthorDto::id, Function.identity()));
    }

    /**
     * Публичная обложка - постоянная ссылка (кэшируется), непубличная - временная подписанная.
     */
    public String coverUrl(Video video, boolean isPublic) {
        if (video.getCoverStorageFilename() == null) {
            return null;
        }
        String url = publicUrl + "/videos/" + video.getId() + "/cover/" + video.getCoverStorageFilename();
        if (isPublic) {
            return url;
        }
        long expiresAt = Instant.now().plus(urlTtl).getEpochSecond();
        return url + "?exp=" + expiresAt + "&sig=" + urlSigner.sign("cover", video.getId(), expiresAt);
    }
}
