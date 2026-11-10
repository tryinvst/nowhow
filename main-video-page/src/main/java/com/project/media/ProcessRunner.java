package com.project.media;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Запускает внешние программы (ffmpeg, ffprobe, whisper-cli) с таймаутом.
 * Вывод процесса пишется во временный файл, чтобы переполненный pipe не подвесил процесс.
 */
@Component
public class ProcessRunner {

    /**
     * @param command программа и её аргументы
     * @param timeout сколько ждать завершения
     * @return объединённый stdout и stderr процесса
     * @throws MediaException если процесс не запустился, не уложился в таймаут или вернул ненулевой код
     */
    public String run(List<String> command, Duration timeout) {
        File log = null;
        Process process = null;
        try {
            log = File.createTempFile("nowhow-cmd", ".log");
            process = new ProcessBuilder(new ArrayList<>(command))
                    .redirectErrorStream(true)
                    .redirectOutput(log)
                    .start();
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new MediaException(command.get(0) + " не уложился в " + timeout.toSeconds() + " с");
            }
            String output = Files.readString(log.toPath(), StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                throw new MediaException(command.get(0) + " завершился с кодом " + process.exitValue() + ": "
                        + output.substring(0, Math.min(500, output.length())));
            }
            return output;
        } catch (IOException e) {
            throw new MediaException("Не удалось запустить " + command.get(0) + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MediaException(command.get(0) + " прерван");
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (log != null) {
                log.delete();
            }
        }
    }
}
