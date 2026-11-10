package com.project.entity;

import com.project.common.Category;
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

@Entity
@Table(name="videos", indexes = {
        @Index(name = "idx_videos_status_course", columnList = "status, course_id"),
        @Index(name = "idx_videos_author", columnList = "author_id"),
        @Index(name = "idx_videos_course", columnList = "course_id, lesson_position")
})

public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(length = 5000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    private String filename;
    private String contentType;
    private Long size;
    private Double durationSeconds;
    private LocalDateTime createdAt;
    private String storageFilename;
    /**
     * {@code id} пользователя из сервиса userProfile (разные сервисы, поэтому без FK)
     */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    private String coverStorageFilename;
    /**
     * {@code true} - обложку загрузил мастер, автопроверка её не перезаписывает.
     */
    private boolean customCover;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;
    @Column(length = 1000)
    private String moderationComment;
    /**
     * Номер запуска автопроверки: результат старого запуска после правки видео отбрасывается.
     */
    private int moderationRun;
    @Column(columnDefinition = "text")
    private String transcript;

    /**
     * Урок курса: видео входит максимум в один курс.
     */
    @Column(name = "course_id")
    private Long courseId;
    @Column(name = "lesson_position")
    private Integer lessonPosition;
    private boolean freePreview;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSize() {
        return size;
    }

    public Double getDurationSeconds() {
        return durationSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStorageFilename() {
        return storageFilename;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getCoverStorageFilename() {
        return coverStorageFilename;
    }

    public boolean isCustomCover() {
        return customCover;
    }

    public VideoStatus getStatus() {
        return status;
    }

    public String getModerationComment() {
        return moderationComment;
    }

    public int getModerationRun() {
        return moderationRun;
    }

    public String getTranscript() {
        return transcript;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Integer getLessonPosition() {
        return lessonPosition;
    }

    public boolean isFreePreview() {
        return freePreview;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setDurationSeconds(Double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStorageFilename(String storageFilename) {
        this.storageFilename = storageFilename;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public void setCoverStorageFilename(String coverStorageFilename) {
        this.coverStorageFilename = coverStorageFilename;
    }

    public void setCustomCover(boolean customCover) {
        this.customCover = customCover;
    }

    public void setStatus(VideoStatus status) {
        this.status = status;
    }

    public void setModerationComment(String moderationComment) {
        this.moderationComment = moderationComment;
    }

    public void setModerationRun(int moderationRun) {
        this.moderationRun = moderationRun;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setLessonPosition(Integer lessonPosition) {
        this.lessonPosition = lessonPosition;
    }

    public void setFreePreview(boolean freePreview) {
        this.freePreview = freePreview;
    }

    /**
     * Делает урок снова отдельным видео мастера: убирает курс, позицию и бесплатный доступ.
     */
    public void detachFromCourse() {
        this.courseId = null;
        this.lessonPosition = null;
        this.freePreview = false;
    }

    @PrePersist
    public void beforeSave () {
      createdAt = LocalDateTime.now();
    }

}
