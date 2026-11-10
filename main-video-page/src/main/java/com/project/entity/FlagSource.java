package com.project.entity;

/**
 * Что именно сработало при автопроверке.
 */
public enum FlagSource {
    /**
     * Название или описание.
     */
    TEXT,
    /**
     * Распознанная речь.
     */
    SPEECH,
    /**
     * Кадр видео (NSFW-модель)
     */
    FRAME,
    /**
     * Проверку не удалось выполнить - решает админ.
     */
    PROCESSING_ERROR
}
