package com.project.repository;

import com.project.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository <Video, Long> {
}
