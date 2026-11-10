package com.project.playback;

import com.project.client.BillingClient;
import com.project.dto.PlaybackResponseDto;
import com.project.entity.Video;
import com.project.exception.ApiException;
import com.project.repository.CourseRepository;
import com.project.security.Viewer;
import com.project.service.VideoMapper;
import com.project.storage.VideoStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Кто может смотреть видео: витрина мастера - все, бесплатный урок - все, остальные уроки - по подписке.
 */
@Service
public class PlaybackService {

    private final VideoStorageService videoStorageService;
    private final CourseRepository courseRepository;
    private final BillingClient billingClient;
    private final UrlSigner urlSigner;
    private final String publicUrl;
    private final Duration ttl;

    public PlaybackService(VideoStorageService videoStorageService, CourseRepository courseRepository,
                           BillingClient billingClient, UrlSigner urlSigner,
                           @Value("${app.public-url}") String publicUrl, @Value("${app.playback.ttl}") Duration ttl) {
        this.videoStorageService = videoStorageService;
        this.courseRepository = courseRepository;
        this.billingClient = billingClient;
        this.urlSigner = urlSigner;
        this.publicUrl = publicUrl;
        this.ttl = ttl;
    }

    /**
     * 404 для чужих непубличных.
     */
    public PlaybackResponseDto playback(Long videoId, Viewer viewer) {
        Video video = videoStorageService.getVisible(videoId, viewer);
        boolean paidLesson = video.getCourseId() != null && !video.isFreePreview();
        if (paidLesson && !viewer.canManage(video.getAuthorId())) {
            if (viewer.isAnonymous()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Войдите, чтобы смотреть уроки курса");
            }
            if (!billingClient.hasActiveSubscription(viewer.bearerToken())) {
                throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Урок доступен по подписке NowHow");
            }
        }
        long expiresAt = Instant.now().plus(ttl).getEpochSecond();
        String url = publicUrl + "/videos/" + videoId + "/stream?exp=" + expiresAt
                + "&sig=" + urlSigner.sign("stream", videoId, expiresAt);
        return new PlaybackResponseDto(url, Instant.ofEpochSecond(expiresAt), !paidLesson);
    }

    /**
     * Проверяем только подпись: права уже проверены при выдаче ссылки.
     */
    public Video streamable(Long videoId, Long exp, String sig) {
        if (!urlSigner.verify("stream", videoId, exp, sig)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Ссылка на видео недействительна или истекла");
        }
        return videoStorageService.getVisible(videoId, new Viewer(null, true, null));
    }
}
