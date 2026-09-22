
package com.project.controller;

import com.project.entity.Video;
import com.project.storage.VideoStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.io.IOException;

@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoStorageService videoStorageService;

    public VideoController(VideoStorageService videoStorageService) {
        this.videoStorageService = videoStorageService;
    }

    @Operation(summary = "Загрузить видео")
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Video upload(
            @Parameter(description = "Название видео", example = "My video")
            @RequestParam("name") String name,

            @Parameter(description = "Видео файл")
            @RequestParam("file") MultipartFile file
    ) {
        return videoStorageService.upload(name, file);
    }

    @Operation(summary = "Получить видео")
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getVideo(
            @Parameter(description = "ID видео", example = "1")
            @PathVariable Long id
    ) {
        Video video = videoStorageService.getVideoInfo(id);
        Resource resource = videoStorageService.getVideo(id);

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(video.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + video.getFilename() + "\""
                )
                .body(resource);
    }

    @Operation(summary = "Удалить видео")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVideo(
            @Parameter(description = "ID видео", example = "1")
            @PathVariable Long id
    ) {
        if (videoStorageService.containsVideoById(id)) {
            videoStorageService.deleteVideo(id);
        }
    }

    @Operation(summary = "Обновить видео")
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateVideo(
            @Parameter(description = "ID видео", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Новое название видео", example = "Updated video")
            @RequestParam("name") String name,

            @Parameter(description = "Новый видео файл")
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        videoStorageService.update(id, name, file);
    }
}