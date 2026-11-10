package com.project.client;

import com.project.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Ходит в billing: есть ли у пользователя активная подписка (с его же токеном)
 */
@Component
public class BillingClient {

    private final RestClient client;

    public BillingClient(@Value("${app.billing-service.url}") String billingServiceUrl) {
        this.client = RestClient.builder().baseUrl(billingServiceUrl).build();
    }

    /**
     * Не знаем статус - не пускаем, но честно говорим, что это сбой, а не отсутствие подписки.
     */
    public boolean hasActiveSubscription(String bearerToken) {
        try {
            SubscriptionStatus status = client.get()
                    .uri("/subscriptions/me")
                    .headers(headers -> headers.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(SubscriptionStatus.class);
            return status != null && status.active();
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Не удалось проверить подписку, попробуйте позже");
        }
    }

    public record SubscriptionStatus(boolean active) { }
}
