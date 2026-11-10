
package com.project.controller;

import com.project.common.Category;
import com.project.dto.PageResponseDto;
import com.project.dto.PlaybackResponseDto;
import com.project.dto.VideoResponseDto;
import com.project.entity.Video;
import com.project.playback.PlaybackService;
import com.project.security.Viewer;
import com.project.storage.VideoStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoStorageService videoStorageService;
    private final PlaybackService playbackService;

    public VideoController(VideoStorageService videoStorageService, PlaybackService playbackService) {
        this.videoStorageService = videoStorageService;
        this.playbackService = playbackService;
    }

    @Operation(summary = "Каталог видео", description = "Опубликованные видео вне курсов, новые сверху. "
            + "Фильтры: category, authorId, q (поиск по названию и описанию)")
    @GetMapping
    public PageResponseDto<VideoResponseDto> listVideos(
            @Parameter(description = "Номер страницы с 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы, до 100", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Код категории", example = "TILING")
            @RequestParam(required = false) Category category,
            @Parameter(description = "ID мастера", example = "1")
            @RequestParam(required = false) Long authorId,
            @Parameter(description = "Поиск по названию и описанию", example = "затирка швов")
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return videoStorageService.listPublic(page, size, category, authorId, q, Viewer.from(jwt));
    }

    @Operation(summary = "Мои видео", description = "Все видео мастера в любом статусе, с комментарием модерации")
    @GetMapping("/mine")
    public PageResponseDto<VideoResponseDto> listMine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return videoStorageService.listMine(page, size, Viewer.from(jwt));
    }

    @Operation(summary = "Загрузить видео", description = "Только для мастеров. До 15 минут. Сразу после загрузки "
            + "статус PROCESSING: идёт автопроверка, затем PUBLISHED или ON_REVIEW")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public VideoResponseDto upload(
            @Parameter(description = "Название видео", example = "Затирка швов на кафеле")
            @RequestParam("name") String name,

            @Parameter(description = "Описание (необязательно)")
            @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "Код категории", example = "TILING")
            @RequestParam("category") Category category,

            @Parameter(description = "Видео файл")
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Своя обложка JPEG/PNG/WebP (необязательно, иначе кадр из видео)")
            @RequestParam(value = "cover", required = false) MultipartFile cover,

            @AuthenticationPrincipal Jwt jwt
    ) {
        return videoStorageService.upload(name, description, category, file, cover, Viewer.from(jwt));
    }

    @Operation(summary = "Информация о видео")
    @GetMapping("/{id}/info")
    public VideoResponseDto getVideoInfo(
            @Parameter(description = "ID видео", example = "1")
            @PathVariable Long id,

            @AuthenticationPrincipal Jwt jwt
    ) {
        return videoStorageService.getVideoInfo(id, Viewer.from(jwt));
    }

    @Operation(summary = "Ссылка для просмотра", description = "Временная ссылка для <video src>. "
            + "401 - нужно войти, 402 - урок доступен по подписке")
    @GetMapping("/{id}/playback")
    public PlaybackResponseDto playback(
            @Parameter(description = "ID видео", example = "1")
            @PathVariable Long id,

            @AuthenticationPrincipal Jwt jwt
    ) {
        return playbackService.playback(id, Viewer.from(jwt));
    }

    /**
     * Builder экранирует кавычки и кодирует кириллицу в имени файла.
     */
    @Operation(summary = "Видеопоток", description = "Только по ссылке из /playback; поддерживает Range (перемотку)")
    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> stream(
            @PathVariable Long id,
            @RequestParam(required = false) Long exp,
            @RequestParam(required = false) String sig
    ) {
        Video video = playbackService.streamable(id, exp, sig);
        Resource resource = new FileSystemResource(videoStorageService.videoPath(video));

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(video.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition disposition = ContentDisposition.inline()
                .filename(video.getFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    /**
     * В URL имя файла: новая обложка - новый URL, поэтому кэш на месяц безопасен.
     */
    @Operation(summary = "Обложка видео")
    @GetMapping("/{id}/cover/{filename}")
    public ResponseEntity<Resource> cover(
            @PathVariable Long id,
            @PathVariable String filename,
            @RequestParam(required = false) Long exp,
            @RequestParam(required = false) String sig
    ) {
        VideoStorageService.CoverFile cover = videoStorageService.loadCover(id, filename, exp, sig);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(cover.format().getContentType()))
                .cacheControl(exp == null ? CacheControl.maxAge(Duration.ofDays(30)).cachePublic() : CacheControl.noStore())
                .body(new FileSystemResource(cover.path()));
    }

    @Operation(summary = "Заменить обложку", description = "JPEG/PNG/WebP до 5 МБ")
    @PutMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VideoResponseDto setCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return videoStorageService.setCover(id, file, Viewer.from(jwt));
    }

    @Operation(summary = "Вернуть обложку-кадр из видео")
    @DeleteMapping("/{id}/cover")
    public VideoResponseDto resetCover(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return videoStorageService.resetCover(id, Viewer.from(jwt));
    }

    @Operation(summary = "Удалить видео", description = "Только автор или администратор")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVideo(
            @Parameter(description = "ID видео", example = "1")
            @PathVariable Long id,

            @AuthenticationPrincipal Jwt jwt
    ) {
        videoStorageService.deleteVideo(id, Viewer.from(jwt));
    }

    @Operation(summary = "Обновить видео", description = "Только автор или администратор. "
            + "Любое изменение отправляет видео на повторную автопроверку")
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public VideoResponseDto updateVideo(
            @Parameter(description = "ID видео", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Новое название видео (необязательно)", example = "Updated video")
            @RequestParam(value = "name", required = false) String name,

            @Parameter(description = "Новое описание (необязательно)")
            @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "Новая категория (необязательно)")
            @RequestParam(value = "category", required = false) Category category,

            @Parameter(description = "Новый видео файл (необязательно)")
            @RequestParam(value = "file", required = false) MultipartFile file,

            @AuthenticationPrincipal Jwt jwt
    ) {
        return videoStorageService.update(id, name, description, category, file, Viewer.from(jwt));
    }
}
