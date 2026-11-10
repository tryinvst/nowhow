package com.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Ограничения пагинации списков ({@code app.pagination.*}).
 *
 * @param maxPageSize максимальный размер страницы, который может запросить клиент
 */
@ConfigurationProperties(prefix = "app.pagination")
public record PaginationProperties(@DefaultValue("100") int maxPageSize) {
}
