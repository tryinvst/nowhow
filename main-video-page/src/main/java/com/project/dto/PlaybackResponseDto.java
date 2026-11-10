package com.project.dto;

import java.time.Instant;

/**
 * Ссылка для &lt;video src&gt;, действует до expiresAt.
 */
public record PlaybackResponseDto(String streamUrl, Instant expiresAt, boolean freePreview) { }
