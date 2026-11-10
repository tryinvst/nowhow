package com.project.repository;

import com.project.entity.Course;
import com.project.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    Page<Course> findAllByAuthorId(Long authorId, Pageable pageable);

    Page<Course> findAllByStatus(CourseStatus status, Pageable pageable);
}
