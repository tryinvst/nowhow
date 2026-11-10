package com.project.testApp.controller;

import com.project.testApp.dto.request.LoginRequestDto;
import com.project.testApp.dto.request.RefreshRequestDto;
import com.project.testApp.dto.response.PersonResponseDto;
import com.project.testApp.dto.response.TokenResponseDto;
import com.project.testApp.service.PersonalService;
import com.project.testApp.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "auth", description = "Вход и обновление токенов")
public class AuthController {

    private final PersonalService personalService;
    private final TokenService tokenService;

    public AuthController(PersonalService personalService, TokenService tokenService) {
        this.personalService = personalService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "Войти", description = "Возвращает access- и refresh-токены")
    public TokenResponseDto login(@Valid @RequestBody LoginRequestDto requestDto) {
        return personalService.login(requestDto);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить токены", description = "Обменивает refresh-токен на новую пару токенов")
    public TokenResponseDto refresh(@Valid @RequestBody RefreshRequestDto requestDto) {
        return tokenService.refresh(requestDto.refreshToken());
    }

    @GetMapping("/me")
    @Operation(summary = "Текущий пользователь", description = "Данные владельца access-токена")
    public PersonResponseDto me(@AuthenticationPrincipal Jwt jwt) {
        return personalService.findCurrentPerson(jwt);
    }
}
