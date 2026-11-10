package com.project.moderation;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Компилирует и кэширует регулярные выражения black-листа.
 * Флаг {@link Pattern#UNICODE_CHARACTER_CLASS} нужен, чтобы {@code \b} и {@code \w} понимали кириллицу.
 */
@Component
public class PatternCompiler {

    private static final int FLAGS = Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE;

    private final Map<String, Pattern> cache = new ConcurrentHashMap<>();

    /**
     * @param pattern регулярное выражение правила
     * @return скомпилированный шаблон, повторные вызовы берут его из кэша
     */
    public Pattern compile(String pattern) {
        return cache.computeIfAbsent(pattern, source -> Pattern.compile(source, FLAGS));
    }

    /**
     * Проверяет выражение до сохранения правила.
     *
     * @param pattern регулярное выражение от администратора
     * @throws IllegalArgumentException если выражение не компилируется
     */
    public void validate(String pattern) {
        try {
            Pattern.compile(pattern, FLAGS);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Некорректное регулярное выражение: " + e.getDescription());
        }
    }
}
