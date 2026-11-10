package com.project.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Где лежат видеофайлы: uploads/videos/&lt;uuid&gt;.&lt;ext&gt;
 */
@Component
public class VideoFiles {
    private final Path storagePath = Paths.get("uploads/videos");

    @PostConstruct
    public void createStorageDirectory() throws IOException {
        Files.createDirectories(storagePath);
    }

    public Path resolve(String storageFilename) {
        return storagePath.resolve(storageFilename).normalize();
    }
}
