package com.project.moderation;

import com.project.entity.FlagSource;
import com.project.entity.ModerationFlag;
import com.project.entity.Video;
import com.project.entity.VideoStatus;
import com.project.media.MediaTools;
import com.project.repository.ModerationFlagRepository;
import com.project.repository.VideoRepository;
import com.project.storage.CoverStorage;
import com.project.storage.VideoFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Автопроверка видео в фоне: обложка → текст → речь → кадры.
 * <p>Чисто → PUBLISHED, любая находка или сбой проверки → ON_REVIEW (решает админ), сами не отклоняем.
 */
@Service
public class ModerationService {
    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);
    private static final int MAX_FRAME_FLAGS = 10;

    private final VideoRepository videoRepository;
    private final ModerationFlagRepository flagRepository;
    private final VideoFiles videoFiles;
    private final CoverStorage coverStorage;
    private final MediaTools mediaTools;
    private final TextModerator textModerator;
    private final SpeechRecognizer speechRecognizer;
    private final NsfwDetector nsfwDetector;
    private final float nsfwThreshold;
    private final int frameIntervalSeconds;

    public ModerationService(VideoRepository videoRepository, ModerationFlagRepository flagRepository,
                             VideoFiles videoFiles, CoverStorage coverStorage, MediaTools mediaTools,
                             TextModerator textModerator, SpeechRecognizer speechRecognizer, NsfwDetector nsfwDetector,
                             @Value("${app.moderation.nsfw.threshold}") float nsfwThreshold,
                             @Value("${app.moderation.nsfw.frame-interval-seconds}") int frameIntervalSeconds) {
        this.videoRepository = videoRepository;
        this.flagRepository = flagRepository;
        this.videoFiles = videoFiles;
        this.coverStorage = coverStorage;
        this.mediaTools = mediaTools;
        this.textModerator = textModerator;
        this.speechRecognizer = speechRecognizer;
        this.nsfwDetector = nsfwDetector;
        this.nsfwThreshold = nsfwThreshold;
        this.frameIntervalSeconds = frameIntervalSeconds;
    }

    /**
     * После рестарта сервиса доделываем то, что не успели проверить.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void resumeUnfinished() {
        for (Video video : videoRepository.findAllByStatus(VideoStatus.PROCESSING)) {
            log.info("Возобновляю автопроверку видео {}", video.getId());
            moderateAsync(video.getId(), video.getModerationRun());
        }
    }

    @Async("moderationExecutor")
    public void moderateAsync(Long videoId, int run) {
        try {
            moderate(videoId, run);
        } catch (Exception e) {
            log.error("Автопроверка видео {} упала", videoId, e);
        }
    }

    /**
     * Видео удалили или уже изменили - этот запуск не актуален.
     */
    void moderate(Long videoId, int run) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null || video.getStatus() != VideoStatus.PROCESSING || video.getModerationRun() != run) {
            return;
        }
        List<ModerationFlag> flags = new ArrayList<>();
        String transcript = null;
        String generatedCover = null;
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("nowhow-moderation-" + videoId);
            Path videoPath = videoFiles.resolve(video.getStorageFilename());
            double duration = video.getDurationSeconds() == null ? 0 : video.getDurationSeconds();

            if (!video.isCustomCover()) {
                Path cover = workDir.resolve("cover.jpg");
                mediaTools.extractCover(videoPath, Math.min(3.0, duration / 3), cover);
                generatedCover = coverStorage.saveGenerated(cover);
            }

            String text = (video.getName() == null ? "" : video.getName()) + "\n"
                    + (video.getDescription() == null ? "" : video.getDescription());
            for (TextModerator.Match match : textModerator.check(text)) {
                flags.add(ModerationFlag.forVideo(videoId, FlagSource.TEXT, match.category(),
                        "Название/описание: «" + match.fragment() + "»", null));
            }

            transcript = checkSpeech(videoId, videoPath, workDir, flags);
            checkFrames(videoId, videoPath, workDir, flags);
        } catch (Exception e) {
            log.warn("Автопроверка видео {} не завершилась", videoId, e);
            flags.add(ModerationFlag.forVideo(videoId, FlagSource.PROCESSING_ERROR, null,
                    "Автопроверка не завершилась: " + e.getMessage(), null));
        } finally {
            deleteQuietly(workDir);
        }
        saveResult(videoId, run, flags, transcript, generatedCover);
    }

    private String checkSpeech(Long videoId, Path videoPath, Path workDir, List<ModerationFlag> flags) {
        if (!mediaTools.hasAudio(videoPath)) {
            return null;
        }
        if (!speechRecognizer.isAvailable()) {
            flags.add(ModerationFlag.forVideo(videoId, FlagSource.PROCESSING_ERROR, null,
                    "Распознавание речи не настроено (WHISPER_MODEL) - речь не проверена", null));
            return null;
        }
        Path wav = workDir.resolve("audio.wav");
        mediaTools.extractAudio(videoPath, wav);
        List<SpeechRecognizer.Segment> segments = speechRecognizer.transcribe(wav, workDir);
        for (SpeechRecognizer.Segment segment : segments) {
            for (TextModerator.Match match : textModerator.check(segment.text())) {
                flags.add(ModerationFlag.forVideo(videoId, FlagSource.SPEECH, match.category(),
                        "Речь: «" + match.fragment() + "» в фразе «" + segment.text() + "»", segment.fromSeconds()));
            }
        }
        return segments.stream().map(SpeechRecognizer.Segment::text).collect(Collectors.joining(" "));
    }

    private void checkFrames(Long videoId, Path videoPath, Path workDir, List<ModerationFlag> flags) throws Exception {
        if (!nsfwDetector.isAvailable()) {
            flags.add(ModerationFlag.forVideo(videoId, FlagSource.PROCESSING_ERROR, null,
                    "NSFW-модель не найдена (NSFW_MODEL) - кадры не проверены", null));
            return;
        }
        Path framesDir = Files.createDirectory(workDir.resolve("frames"));
        List<Path> frames = mediaTools.extractFrames(videoPath, frameIntervalSeconds, framesDir);
        int frameFlags = 0;
        for (int i = 0; i < frames.size() && frameFlags < MAX_FRAME_FLAGS; i++) {
            float probability = nsfwDetector.nsfwProbability(frames.get(i));
            if (probability >= nsfwThreshold) {
                frameFlags++;
                flags.add(ModerationFlag.forVideo(videoId, FlagSource.FRAME, null,
                        String.format("Кадр похож на откровенный контент (%.0f%%)", probability * 100),
                        (double) i * frameIntervalSeconds));
            }
        }
    }

    /**
     * Перечитываем видео: за время проверки его могли изменить или удалить.
     */
    private void saveResult(Long videoId, int run, List<ModerationFlag> flags, String transcript, String generatedCover) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null || video.getModerationRun() != run || video.getStatus() != VideoStatus.PROCESSING) {
            coverStorage.delete(generatedCover);
            return;
        }
        if (generatedCover != null && !video.isCustomCover()) {
            coverStorage.delete(video.getCoverStorageFilename());
            video.setCoverStorageFilename(generatedCover);
        } else {
            coverStorage.delete(generatedCover);
        }
        video.setTranscript(transcript);
        flagRepository.deleteAllByVideoId(videoId);
        flagRepository.saveAll(flags);
        if (flags.isEmpty()) {
            video.setStatus(VideoStatus.PUBLISHED);
            video.setModerationComment(null);
        } else {
            video.setStatus(VideoStatus.ON_REVIEW);
            video.setModerationComment("Автопроверка нашла спорные места, видео ждёт проверки модератором");
        }
        videoRepository.save(video);
        log.info("Видео {}: автопроверка завершена, статус {}, находок {}", videoId, video.getStatus(), flags.size());
    }

    private void deleteQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try {
            FileSystemUtils.deleteRecursively(dir);
        } catch (IOException e) {
            log.warn("Не удалось удалить временную папку {}", dir, e);
        }
    }
}
