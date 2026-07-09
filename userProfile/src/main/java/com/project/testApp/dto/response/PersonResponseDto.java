package com.project.testApp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PersonResponseDto(@Schema(description = "Имя пользователя", example = "55")
 Long id,String phonenumber,String username,String email,String password) {
}
