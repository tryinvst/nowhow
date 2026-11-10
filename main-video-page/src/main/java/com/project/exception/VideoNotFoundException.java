package com.project.exception;

/**
 * Видео или его файл на диске не найдены: ответ 404.
 */
public class VideoNotFoundException extends RuntimeException {

    /**
     * @param message текст для клиента
     */
    public VideoNotFoundException(String message) {
        super(message);
    }

    /**
     * @param id id видео из запроса
     */
    public VideoNotFoundException(Long id) {
        this("Видео с id " + id + " не найдено");
    }
}
