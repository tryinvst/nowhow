package com.project.testApp.dto.response;

public record TokenResponseDto(String accessToken, String refreshToken, long expiresIn) { }
