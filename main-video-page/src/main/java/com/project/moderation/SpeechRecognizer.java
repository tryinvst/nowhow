package com.project.moderation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.media.ProcessRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Распознавание речи локальным whisper.cpp (whisper-cli), без облака.
 */
@Component
public class SpeechRecognizer {

    private final String cli;
    private final String model;
    private final String language;
    private final ObjectMapper objectMapper;
    private final ProcessRunner processRunner;

    public SpeechRecognizer(@Value("${app.moderation.whisper.cli}") String cli,
                            @Value("${app.moderation.whisper.model}") String model,
                            @Value("${app.moderation.whisper.language}") String language,
                            ObjectMapper objectMapper, ProcessRunner processRunner) {
        this.cli = cli;
        this.model = model;
        this.language = language;
        this.objectMapper = objectMapper;
        this.processRunner = processRunner;
    }

    public boolean isAvailable() {
        return model != null && !model.isBlank() && Files.isRegularFile(Path.of(model));
    }

    public List<Segment> transcribe(Path wav, Path workDir) {
        Path outBase = workDir.resolve("transcript");
        processRunner.run(List.of(cli, "-m", model, "-l", language, "-oj", "-np", "-of", outBase.toString(),
                "-f", wav.toString()), Duration.ofMinutes(20));
        try {
            JsonNode root = objectMapper.readTree(outBase.resolveSibling("transcript.json").toFile());
            List<Segment> segments = new ArrayList<>();
            for (JsonNode node : root.path("transcription")) {
                String text = node.path("text").asText("").trim();
                if (!text.isEmpty()) {
                    segments.add(new Segment(node.path("offsets").path("from").asDouble() / 1000.0, text));
                }
            }
            return segments;
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать результат whisper: " + e.getMessage(), e);
        }
    }

    public record Segment(double fromSeconds, String text) { }
}
