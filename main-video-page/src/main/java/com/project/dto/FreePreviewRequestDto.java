package com.project.dto;

import jakarta.validation.constraints.NotNull;

public record FreePreviewRequestDto(@NotNull(message = "Укажите freePreview") Boolean freePreview) { }
