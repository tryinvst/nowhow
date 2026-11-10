package com.project.testApp.dto.response;

/**
 * То, что видят все, включая гостей: без телефона, email и username.
 */
public record MasterPublicDto(Long id, String displayName, String specialization, String specializationTitle,
                              String about, Integer experienceYears, String avatarUrl) { }
