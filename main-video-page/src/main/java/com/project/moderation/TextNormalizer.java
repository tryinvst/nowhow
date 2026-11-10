package com.project.moderation;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Приводит текст к виду, в котором его сверяют с black-листом.
 */
@Component
public class TextNormalizer {

    /**
     * @param text исходный текст
     * @return текст в нижнем регистре с заменой «ё» на «е»
     */
    public String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replace('ё', 'е');
    }
}
