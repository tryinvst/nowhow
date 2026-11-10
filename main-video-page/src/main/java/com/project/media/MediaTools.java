package com.project.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

/**
 * Обёртка над ffmpeg/ffprobe: длительность, обложка, кадры для NSFW, звук для распознавания речи.
 */
@Component
public class MediaTools {

    private final String ffmpeg;
    private final String ffprobe;
    private final ProcessRunner processRunner;

    public MediaTools(@Value("${app.media.ffmpeg}") String ffmpeg, @Value("${app.media.ffprobe}") String ffprobe,
                      ProcessRunner processRunner) {
        this.ffmpeg = ffmpeg;
        this.ffprobe = ffprobe;
        this.processRunner = processRunner;
    }

    /**
     * {@code null} - файл не читается как видео.
     */
    public Double probeDurationSeconds(Path video) {
        try {
            String output = processRunner.run(List.of(ffprobe, "-v", "error", "-select_streams", "v:0",
                    "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1",
                    video.toString()), Duration.ofSeconds(30));
            double duration = Double.parseDouble(output.trim().lines().findFirst().orElse(""));
            return duration > 0 ? duration : null;
        } catch (MediaException | NumberFormatException e) {
            return null;
        }
    }

    public void extractCover(Path video, double atSeconds, Path outJpg) {
        processRunner.run(List.of(ffmpeg, "-y", "-v", "error", "-ss", String.format("%.2f", atSeconds).replace(',', '.'),
                "-i", video.toString(), "-frames:v", "1", "-vf", "scale=640:-2", "-q:v", "3",
                outJpg.toString()), Duration.ofMinutes(1));
    }

    /**
     * Кадр раз в intervalSeconds, сразу 384x384 - размер входа NSFW-модели.
     */
    public List<Path> extractFrames(Path video, int intervalSeconds, Path dir) {
        processRunner.run(List.of(ffmpeg, "-y", "-v", "error", "-i", video.toString(),
                "-vf", "fps=1/" + intervalSeconds + ",scale=384:384", "-q:v", "3",
                dir.resolve("frame_%05d.jpg").toString()), Duration.ofMinutes(10));
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().startsWith("frame_")).sorted().toList();
        } catch (IOException e) {
            throw new MediaException("Не удалось прочитать кадры: " + e.getMessage());
        }
    }

    /**
     * 16 кГц моно WAV - формат, который ждёт whisper.cpp.
     */
    public void extractAudio(Path video, Path outWav) {
        processRunner.run(List.of(ffmpeg, "-y", "-v", "error", "-i", video.toString(), "-vn", "-ac", "1", "-ar", "16000",
                "-c:a", "pcm_s16le", outWav.toString()), Duration.ofMinutes(5));
    }

    public boolean hasAudio(Path video) {
        try {
            String output = processRunner.run(List.of(ffprobe, "-v", "error", "-select_streams", "a",
                    "-show_entries", "stream=index", "-of", "csv=p=0", video.toString()), Duration.ofSeconds(30));
            return !output.isBlank();
        } catch (MediaException e) {
            return false;
        }
    }

}
