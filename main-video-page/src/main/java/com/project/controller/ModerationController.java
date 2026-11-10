package com.project.controller;

import com.project.dto.CourseReviewDto;
import com.project.dto.ModerationDecisionDto;
import com.project.dto.ModerationRuleDto;
import com.project.dto.PageResponseDto;
import com.project.dto.VideoReviewDto;
import com.project.entity.CourseStatus;
import com.project.entity.VideoStatus;
import com.project.moderation.ModerationAdminService;
import com.project.security.Viewer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Только Admin и Develop_admin (см. SecurityConfig)
 */
@RestController
@RequestMapping("/moderation")
@Tag(name = "moderation", description = "Очередь модерации и black-лист")
public class ModerationController {

    private final ModerationAdminService moderationAdminService;

    public ModerationController(ModerationAdminService moderationAdminService) {
        this.moderationAdminService = moderationAdminService;
    }

    @GetMapping("/videos")
    @Operation(summary = "Видео на модерации", description = "По умолчанию ON_REVIEW, старые сверху")
    public PageResponseDto<VideoReviewDto> videoQueue(@RequestParam(defaultValue = "ON_REVIEW") VideoStatus status,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return moderationAdminService.videoQueue(status, page, size, Viewer.from(jwt));
    }

    @GetMapping("/videos/{id}")
    @Operation(summary = "Видео с находками и расшифровкой речи")
    public VideoReviewDto videoReview(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return moderationAdminService.videoReview(id, Viewer.from(jwt));
    }

    @PostMapping("/videos/{id}/approve")
    @Operation(summary = "Одобрить видео")
    public VideoReviewDto approveVideo(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return moderationAdminService.decideVideo(id, true, null, Viewer.from(jwt));
    }

    @PostMapping("/videos/{id}/reject")
    @Operation(summary = "Отклонить видео", description = "Причина обязательна - её увидит мастер")
    public VideoReviewDto rejectVideo(@PathVariable Long id, @Valid @RequestBody ModerationDecisionDto decision,
                                      @AuthenticationPrincipal Jwt jwt) {
        return moderationAdminService.decideVideo(id, false, decision.reason(), Viewer.from(jwt));
    }

    @GetMapping("/courses")
    @Operation(summary = "Курсы на модерации")
    public PageResponseDto<CourseReviewDto> courseQueue(@RequestParam(defaultValue = "ON_REVIEW") CourseStatus status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return moderationAdminService.courseQueue(status, page, size, Viewer.from(jwt));
    }

    @PostMapping("/courses/{id}/approve")
    @Operation(summary = "Одобрить курс")
    public CourseReviewDto approveCourse(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return moderationAdminService.decideCourse(id, true, null, Viewer.from(jwt));
    }

    @PostMapping("/courses/{id}/reject")
    @Operation(summary = "Отклонить курс", description = "Причина обязательна")
    public CourseReviewDto rejectCourse(@PathVariable Long id, @Valid @RequestBody ModerationDecisionDto decision,
                                        @AuthenticationPrincipal Jwt jwt) {
        return moderationAdminService.decideCourse(id, false, decision.reason(), Viewer.from(jwt));
    }

    @GetMapping("/rules")
    @Operation(summary = "Black-лист", description = "Регулярные выражения по тексту в нижнем регистре (ё = е)")
    public List<ModerationRuleDto> rules() {
        return moderationAdminService.rules();
    }

    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить правило")
    public ModerationRuleDto addRule(@Valid @RequestBody ModerationRuleDto requestDto) {
        return moderationAdminService.addRule(requestDto);
    }

    @DeleteMapping("/rules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить правило")
    public void deleteRule(@PathVariable Long id) {
        moderationAdminService.deleteRule(id);
    }
}
