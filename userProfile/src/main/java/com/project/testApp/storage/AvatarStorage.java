package com.project.testApp.storage;

import com.project.common.ImageFormat;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Аватары мастеров на диске: uploads/avatars/&lt;uuid&gt;.&lt;ext&gt;
 */
@Component
public class AvatarStorage {
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private final Path storagePath = Paths.get("uploads/avatars");

    @PostConstruct
    public void createStorageDirectory() throws IOException {
        Files.createDirectories(storagePath);
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл аватара не выбран");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Аватар должен быть не больше 5 МБ");
        }
        try (InputStream in = file.getInputStream()) {
            ImageFormat format = ImageFormat.detect(in.readNBytes(12));
            if (format == null) {
                throw new IllegalArgumentException("Аватар должен быть в формате JPEG, PNG или WebP");
            }
            String filename = UUID.randomUUID() + format.getExtension();
            try (InputStream content = file.getInputStream()) {
                Files.copy(content, storagePath.resolve(filename));
            }
            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Resource load(String filename) {
        return new FileSystemResource(storagePath.resolve(filename).normalize());
    }

    public void delete(String filename) {
        if (filename == null) {
            return;
        }
        try {
            Files.deleteIfExists(storagePath.resolve(filename).normalize());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
