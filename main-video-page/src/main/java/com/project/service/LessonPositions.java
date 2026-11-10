package com.project.service;

import com.project.entity.Video;
import com.project.repository.VideoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * После удаления урока позиции снова идут 1..n без дыр.
 */
@Component
public class LessonPositions {

    private final VideoRepository videoRepository;

    public LessonPositions(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public void renumber(Long courseId) {
        List<Video> lessons = videoRepository.findAllByCourseIdOrderByLessonPositionAsc(courseId);
        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).setLessonPosition(i + 1);
        }
        videoRepository.saveAll(lessons);
    }
}
