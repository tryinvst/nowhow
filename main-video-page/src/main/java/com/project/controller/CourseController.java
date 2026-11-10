package com.project.controller;

import com.project.common.Category;
import com.project.dto.CourseRequestDto;
import com.project.dto.CourseResponseDto;
import com.project.dto.FreePreviewRequestDto;
import com.project.dto.LessonOrderRequestDto;
import com.project.dto.LessonRequestDto;
import com.project.dto.PageResponseDto;
import com.project.security.Viewer;
import com.project.service.CourseService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/courses")
@Tag(name = "courses", description = "Курсы мастеров: курс -> уроки (видео)")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "Каталог курсов", description = "Только опубликованные. Фильтры: category, authorId, q")
    public PageResponseDto<CourseResponseDto> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) Category category,
                                                   @RequestParam(required = false) Long authorId,
                                                   @RequestParam(required = false) String q,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return courseService.listPublic(page, size, category, authorId, q, Viewer.from(jwt));
    }

    @GetMapping("/mine")
    @Operation(summary = "Мои курсы", description = "Все курсы мастера в любом статусе")
    public PageResponseDto<CourseResponseDto> mine(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @AuthenticationPrincipal Jwt jwt) {
        return courseService.listMine(page, size, Viewer.from(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать курс", description = "Только мастер; создаётся черновиком")
    public CourseResponseDto create(@Valid @RequestBody CourseRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {
        return courseService.create(requestDto, Viewer.from(jwt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Курс с уроками", description = "Черновики видны только автору и админу")
    public CourseResponseDto get(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return courseService.get(id, Viewer.from(jwt));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить название, описание, категорию")
    public CourseResponseDto update(@PathVariable Long id, @Valid @RequestBody CourseRequestDto requestDto,
                                    @AuthenticationPrincipal Jwt jwt) {
        return courseService.update(id, requestDto, Viewer.from(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить курс", description = "Уроки остаются отдельными видео мастера")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        courseService.delete(id, Viewer.from(jwt));
    }

    @PostMapping("/{id}/lessons")
    @Operation(summary = "Добавить урок", description = "Своё видео, не входящее в другой курс; встаёт последним")
    public CourseResponseDto addLesson(@PathVariable Long id, @Valid @RequestBody LessonRequestDto requestDto,
                                       @AuthenticationPrincipal Jwt jwt) {
        return courseService.addLesson(id, requestDto.videoId(), requestDto.freePreview(), Viewer.from(jwt));
    }

    @PutMapping("/{id}/lessons/order")
    @Operation(summary = "Порядок уроков", description = "Все videoId курса в новом порядке")
    public CourseResponseDto reorder(@PathVariable Long id, @Valid @RequestBody LessonOrderRequestDto requestDto,
                                     @AuthenticationPrincipal Jwt jwt) {
        return courseService.reorder(id, requestDto.videoIds(), Viewer.from(jwt));
    }

    @PutMapping("/{id}/lessons/{videoId}")
    @Operation(summary = "Бесплатный урок", description = "freePreview=true - урок смотрят без подписки")
    public CourseResponseDto setFreePreview(@PathVariable Long id, @PathVariable Long videoId,
                                            @Valid @RequestBody FreePreviewRequestDto requestDto,
                                            @AuthenticationPrincipal Jwt jwt) {
        return courseService.setFreePreview(id, videoId, requestDto.freePreview(), Viewer.from(jwt));
    }

    @DeleteMapping("/{id}/lessons/{videoId}")
    @Operation(summary = "Убрать урок из курса", description = "Видео остаётся у мастера")
    public CourseResponseDto removeLesson(@PathVariable Long id, @PathVariable Long videoId,
                                          @AuthenticationPrincipal Jwt jwt) {
        return courseService.removeLesson(id, videoId, Viewer.from(jwt));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Опубликовать", description = "Все уроки должны пройти автопроверку (409 иначе). "
            + "Если текст курса не прошёл black-лист - курс уходит на модерацию")
    public CourseResponseDto publish(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return courseService.publish(id, Viewer.from(jwt));
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Снять с публикации", description = "Курс снова становится черновиком")
    public CourseResponseDto unpublish(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return courseService.unpublish(id, Viewer.from(jwt));
    }
}
