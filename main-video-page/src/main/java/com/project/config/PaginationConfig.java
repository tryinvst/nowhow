package com.project.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Подключает {@link PaginationProperties} к контексту Spring.
 */
@Configuration
@EnableConfigurationProperties(PaginationProperties.class)
public class PaginationConfig {
}
