package com.project.util;

import org.springframework.stereotype.Component;

/**
 * Форматирует длительность видео для сообщений пользователю.
 */
@Component
public class DurationFormatter {

    /**
     * @param seconds длительность в секундах
     * @return строка вида {@code 16:01}
     */
    public String minutesAndSeconds(double seconds) {
        long total = Math.round(seconds);
        return String.format("%d:%02d", total / 60, total % 60);
    }
}
