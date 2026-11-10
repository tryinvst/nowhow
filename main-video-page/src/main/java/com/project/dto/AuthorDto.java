package com.project.dto;

/**
 * Автор в карточке: если сервис профилей недоступен, будет только id.
 */
public record AuthorDto(Long id, String displayName, String specialization, String specializationTitle,
                        String avatarUrl) { }
