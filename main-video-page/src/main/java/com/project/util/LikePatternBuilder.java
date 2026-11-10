package com.project.util;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Строит шаблон для SQL {@code LIKE} «содержит подстроку» из пользовательского поиска.
 * Символы {@code %}, {@code _} и {@code \} экранируются, поэтому ищутся буквально.
 */
@Component
public class LikePatternBuilder {

    /** Символ экранирования, который нужно передать в {@code CriteriaBuilder#like}. */
    public static final char ESCAPE = '\\';

    /**
     * @param query строка поиска от пользователя
     * @return шаблон вида {@code %запрос%} в нижнем регистре
     */
    public String contains(String query) {
        String escaped = query.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
