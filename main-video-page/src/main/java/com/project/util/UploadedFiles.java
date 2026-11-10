package com.project.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Проверки файлов из multipart-запроса.
 */
@Component
public class UploadedFiles {

    /**
     * @param file необязательная часть multipart-запроса
     * @return {@code true}, если файл передан и не пустой
     */
    public boolean hasContent(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}
