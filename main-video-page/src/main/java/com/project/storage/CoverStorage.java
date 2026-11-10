package com.project.storage;

import com.project.common.ImageFormat;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Обложки видео: uploads/covers/&lt;uuid&gt;.&lt;ext&gt;, своя от мастера или кадр из видео.
 */
@Component
public class CoverStorage {
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private final Path storagePath = Paths.get("uploads/covers");

    @PostConstruct
    public void createStorageDirectory() throws IOException {
        Files.createDirectories(storagePath);
    }

    public String save(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Обложка должна быть не больше 5 МБ");
        }
        try (InputStream in = file.getInputStream()) {
            ImageFormat format = ImageFormat.detect(in.readNBytes(12));
            if (format == null) {
                throw new IllegalArgumentException("Обложка должна быть в формате JPEG, PNG или WebP");
            }
            String filename = UUID.randomUUID() + format.getExtension();
            try (InputStream content = file.getInputStream()) {
                Files.copy(content, resolve(filename));
            }
            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Кадр, вырезанный ffmpeg во временную папку.
     */
    public String saveGenerated(Path jpg) {
        String filename = UUID.randomUUID() + ImageFormat.JPEG.getExtension();
        try {
            Files.move(jpg, resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return filename;
    }

    public Path resolve(String filename) {
        return storagePath.resolve(filename).normalize();
    }

    public void delete(String filename) {
        if (filename == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(filename));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
