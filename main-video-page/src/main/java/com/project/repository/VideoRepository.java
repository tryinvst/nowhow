package com.project.repository;

import com.project.entity.Video;
import com.project.entity.VideoStatus;
import com.project.repository.projection.LessonStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface VideoRepository extends JpaRepository <Video, Long>, JpaSpecificationExecutor<Video> {
    Page<Video> findAllByAuthorId(Long authorId, Pageable pageable);

    Page<Video> findAllByStatus(VideoStatus status, Pageable pageable);

    List<Video> findAllByStatus(VideoStatus status);

    List<Video> findAllByCourseIdOrderByLessonPositionAsc(Long courseId);

    List<Video> findAllByCourseIdInAndLessonPosition(Collection<Long> courseIds, Integer lessonPosition);

    /**
     * Число уроков и их суммарная длительность для нескольких курсов одним запросом.
     *
     * @param courseIds id курсов на странице каталога
     * @return по одной записи на курс, у которого есть уроки
     */
    @Query("""
            select new com.project.repository.projection.LessonStats(
                v.courseId, count(v), coalesce(sum(v.durationSeconds), 0.0))
            from Video v
            where v.courseId in :courseIds
            group by v.courseId
            """)
    List<LessonStats> lessonStats(Collection<Long> courseIds);
}
