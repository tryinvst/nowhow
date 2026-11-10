package com.project.service;

import com.project.entity.CourseStatus;
import com.project.entity.Video;
import com.project.entity.VideoStatus;
import org.springframework.stereotype.Component;

/**
 * Решает, видно ли видео всем, включая гостей.
 */
@Component
public class VideoVisibilityPolicy {

    /**
     * Видео публично, если прошло проверку, а урок курса - ещё и если курс опубликован.
     *
     * @param video        видео
     * @param courseStatus статус курса, в который входит видео, или {@code null} для видео вне курса
     * @return {@code true}, если видео можно показывать всем
     */
    public boolean isPublic(Video video, CourseStatus courseStatus) {
        return video.getStatus() == VideoStatus.PUBLISHED
                && (video.getCourseId() == null || courseStatus == CourseStatus.PUBLISHED);
    }
}
