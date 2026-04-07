package demo.service;

import demo.module.LogEventTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ElasticSearchService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchService.class);
    private final RestClient client;
    private final String indexname;

    public ElasticSearchService(RestClient.Builder restClientBuilder,
                                @Value("${app.elasticsearch.url}") String elkConnection,
                                @Value("${app.elasticsearch.index}") String indexname) {
        this.client = restClientBuilder.baseUrl(elkConnection).build();
        this.indexname = indexname;
    }

    public void logEvent(String message) {
        LogEventTest event = LogEventTest.event( message);
        try {
            client.post()
                    .uri("/" + indexname + "/_doc/" + event.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
            logger.debug("Залогировано: {}", event);
        } catch (Exception e) {
            logger.error("Ошибка при логировании: {}", e.getMessage(), e);
        }
    }
}