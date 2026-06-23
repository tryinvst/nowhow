package com.project.testApp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonRequestDto(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be greater 8")
        String password,
        @NotBlank(message = "Phonenumber is required")
        String phonenumber,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email) { }
