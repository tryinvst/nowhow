package com.project.dto;

import java.util.List;

public record CourseReviewDto(CourseResponseDto course, List<ModerationFlagDto> flags) { }
