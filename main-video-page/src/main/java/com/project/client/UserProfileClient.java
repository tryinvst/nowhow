package com.project.client;

import com.project.exception.AuthorNotFoundException;
import com.project.exception.UserServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ходит в сервис userProfile: проверка автора и публичные профили мастеров для карточек.
 */
@Component
public class UserProfileClient {
    private static final Logger log = LoggerFactory.getLogger(UserProfileClient.class);

    private final RestClient client;

    public UserProfileClient(@Value("${app.user-service.url}") String userServiceUrl) {
        this.client = RestClient.builder().baseUrl(userServiceUrl).build();
    }

    /**
     * Токен пользователя пробрасываем дальше: userProfile отвечает только авторизованным запросам.
     */
    public void checkUserExists(Long userId, String bearerToken) {
        try {
            client.get()
                    .uri(uriBuilder -> uriBuilder.path("/userById").queryParam("id", userId).build())
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AuthorNotFoundException("Автор с id " + userId + " не найден");
            }
            throw new UserServiceUnavailableException("Сервис пользователей вернул ошибку", e);
        } catch (RestClientException e) {
            throw new UserServiceUnavailableException("Сервис пользователей недоступен", e);
        }
    }

    /**
     * Карточки не должны падать из-за профилей: при сбое вернём пусто, автор будет только с id.
     */
    public Map<Long, MasterProfile> findMasters(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            List<MasterProfile> masters = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/masters").queryParam("ids", ids.toArray()).build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MasterProfile>>() { });
            return masters == null ? Map.of()
                    : masters.stream().collect(Collectors.toMap(MasterProfile::id, Function.identity()));
        } catch (RestClientException e) {
            log.warn("Не удалось получить профили мастеров {}: {}", ids, e.getMessage());
            return Map.of();
        }
    }

    public record MasterProfile(Long id, String displayName, String specialization, String specializationTitle,
                                String about, Integer experienceYears, String avatarUrl) { }
}
