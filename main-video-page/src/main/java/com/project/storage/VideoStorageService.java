package com.project.storage;

import com.project.entity.Video;
import com.project.repository.VideoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class VideoStorageService {

    private final VideoRepository videoRepository;
    private final Path storagePath = Paths.get ("uploads/videos");

    public VideoStorageService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @PostConstruct
    public void createStorageDirectory() {
        try {
            Files.createDirectories (storagePath);
        }
        catch(Exception e) {
            System.out.println (e.getMessage ());
        }

    }

    public Video upload(String name ,MultipartFile file) {
        if (file.isEmpty ()) {
            throw new IllegalArgumentException ("видео-файл не выбран");
        }
        String contentType = file.getContentType ();
        if (contentType == null || !contentType.startsWith ("video/")) {
            throw new IllegalArgumentException ("можно загружать только видео");
        }
        String originalFileName = file.getOriginalFilename ();
        if (originalFileName == null) {
            throw new IllegalArgumentException ("у файла отсутствует название");
        }
        originalFileName = Paths.get (originalFileName).getFileName ().toString ();
        String extention = getExtention (originalFileName);
        String storageFileName = UUID.randomUUID () + extention;
        Path targetPath = storagePath.resolve (storageFileName).normalize();
        try {
            Files.copy ( file.getInputStream (),targetPath,StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new RuntimeException ( e );
        }
        Video video = new Video();
        video.setName(name);
        video.setFilename(originalFileName);
        video.setSize(file.getSize());
        video.setContentType(contentType);
        video.setStorageFilename (storageFileName);
        return videoRepository.save(video);
    }

    public Video update(Long id, String name, MultipartFile file) throws IOException {
        Video video = videoRepository.findById(id).orElseThrow(()
        -> new RuntimeException ("Видео не найдено"));
        if (name != null & !name.isBlank()) {
            video.setName(name);
        }
        if (file !=null & !file.isEmpty()) {
            String contentType = file.getContentType();
            String originalName = file.getOriginalFilename();
            originalName = Paths.get(originalName).getFileName().toString();
            String extention = getExtention(originalName);
            String newstorageFilename = UUID.randomUUID()+extention;
            Path newPath = storagePath.resolve(newstorageFilename).normalize();
            String oldstorageFilename = video.getStorageFilename();
            try {
                Files.copy(file.getInputStream(),newPath
                        ,StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            video.setFilename(originalName);
            video.setSize(file.getSize());
            video.setContentType(contentType);
            video.setStorageFilename(newstorageFilename);
            Video updateVideo = videoRepository.save(video);
            Path oldPath = storagePath.resolve(oldstorageFilename)
                    .normalize();
            Files.deleteIfExists(oldPath);
            return updateVideo;
        }
        return videoRepository.save(video);
    }

    public Resource getVideo (Long id) {
        Video video = videoRepository.findById(id).orElseThrow(()
         -> new RuntimeException("Видео не найдено"));
        Path filePath = storagePath.resolve (video.getStorageFilename()).normalize();
        if (!Files.exists (filePath)) {
            throw new RuntimeException("Файл не найден");
        }
        return new FileSystemResource(filePath);
    }

    public Video getVideoInfo(Long id) {
        return videoRepository.findById(id).orElseThrow(()
        -> new RuntimeException("Видео не найдено"));
    }

    private String getExtention (String fileName) {
        Integer dotPossition = fileName.lastIndexOf ('.');
        if (dotPossition == -1) {
            return "";
        }
        return fileName.substring(dotPossition);
    }

    public Boolean containsVideoById (Long id) {
       return !videoRepository.findById(id).isEmpty();
    }

    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }

}
