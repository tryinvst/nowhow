package com.project.repository;

import com.project.entity.ModerationFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface ModerationFlagRepository extends JpaRepository<ModerationFlag, Long> {
    List<ModerationFlag> findAllByVideoIdOrderByTimecodeSecondsAsc(Long videoId);

    List<ModerationFlag> findAllByVideoIdIn(Collection<Long> videoIds);

    List<ModerationFlag> findAllByCourseId(Long courseId);

    List<ModerationFlag> findAllByCourseIdIn(Collection<Long> courseIds);

    @Transactional
    void deleteAllByVideoId(Long videoId);

    @Transactional
    void deleteAllByCourseId(Long courseId);
}
