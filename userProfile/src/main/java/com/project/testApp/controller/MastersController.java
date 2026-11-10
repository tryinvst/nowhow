package com.project.testApp.controller;

import com.project.testApp.dto.request.ProfileUpdateRequestDto;
import com.project.testApp.dto.response.MasterPublicDto;
import com.project.testApp.dto.response.PersonResponseDto;
import com.project.testApp.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

@RestController
@Tag(name = "masters", description = "Публичные профили мастеров")
public class MastersController {

    private final ProfileService profileService;

    public MastersController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/masters/{id}")
    @Operation(summary = "Публичный профиль мастера", description = "Доступно без входа; 404 для учеников и несуществующих id")
    public MasterPublicDto getMaster(@PathVariable Long id) {
        return profileService.findMaster(id);
    }

    @GetMapping("/masters")
    @Operation(summary = "Профили нескольких мастеров", description = "Для карточек каталога: /masters?ids=1,2,3 (до 100)")
    public List<MasterPublicDto> getMasters(@RequestParam List<Long> ids) {
        return profileService.findMasters(ids);
    }

    /**
     * URL меняется вместе с файлом, поэтому можно кэшировать надолго.
     */
    @GetMapping("/masters/{id}/avatar/{filename}")
    @Operation(summary = "Аватар мастера")
    public ResponseEntity<Resource> getAvatar(@PathVariable Long id, @PathVariable String filename) {
        ProfileService.Avatar avatar = profileService.loadAvatar(id, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.format().getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(avatar.resource());
    }

    @PutMapping("/me/profile")
    @Operation(summary = "Заполнить свой профиль", description = "Имя, специализация (для мастера обязательна), о себе, стаж")
    public PersonResponseDto updateProfile(@Valid @RequestBody ProfileUpdateRequestDto requestDto,
                                           @AuthenticationPrincipal Jwt jwt) {
        return profileService.updateProfile(requestDto, jwt);
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Загрузить аватар", description = "JPEG, PNG или WebP до 5 МБ")
    public PersonResponseDto uploadAvatar(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        return profileService.updateAvatar(file, jwt);
    }

    @DeleteMapping("/me/avatar")
    @Operation(summary = "Удалить аватар")
    public PersonResponseDto deleteAvatar(@AuthenticationPrincipal Jwt jwt) {
        return profileService.deleteAvatar(jwt);
    }
}
