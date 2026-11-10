package com.project.dto;

import java.util.List;

/**
 * Видео в очереди модерации: находки и расшифровка речи.
 */
public record VideoReviewDto(VideoResponseDto video, List<ModerationFlagDto> flags, String transcript) { }
