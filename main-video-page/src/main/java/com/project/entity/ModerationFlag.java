package com.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Одна находка автопроверки: у видео (videoId) или у текста курса (courseId)
 */
@Entity
@Table(name = "moderation_flags", indexes = {
        @Index(name = "idx_flags_video", columnList = "video_id"),
        @Index(name = "idx_flags_course", columnList = "course_id")
})
public class ModerationFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long videoId;
    private Long courseId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlagSource source;
    @Enumerated(EnumType.STRING)
    private RuleCategory ruleCategory;
    @Column(length = 1000)
    private String detail;
    /**
     * Секунда видео, где найдено (для речи и кадров)
     */
    private Double timecodeSeconds;
    private LocalDateTime createdAt;

    public static ModerationFlag forVideo(Long videoId, FlagSource source, RuleCategory ruleCategory,
                                          String detail, Double timecodeSeconds) {
        ModerationFlag flag = new ModerationFlag();
        flag.videoId = videoId;
        flag.source = source;
        flag.ruleCategory = ruleCategory;
        flag.detail = detail.length() > 1000 ? detail.substring(0, 1000) : detail;
        flag.timecodeSeconds = timecodeSeconds;
        return flag;
    }

    public static ModerationFlag forCourse(Long courseId, RuleCategory ruleCategory, String detail) {
        ModerationFlag flag = forVideo(null, FlagSource.TEXT, ruleCategory, detail, null);
        flag.courseId = courseId;
        return flag;
    }

    public Long getId() {
        return id;
    }

    public Long getVideoId() {
        return videoId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public FlagSource getSource() {
        return source;
    }

    public RuleCategory getRuleCategory() {
        return ruleCategory;
    }

    public String getDetail() {
        return detail;
    }

    public Double getTimecodeSeconds() {
        return timecodeSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    public void beforeSave() {
        createdAt = LocalDateTime.now();
    }
}
