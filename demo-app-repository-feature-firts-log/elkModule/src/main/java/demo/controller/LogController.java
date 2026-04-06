package demo.controller;

import com.example.demo.module.LogRequest;
import com.example.demo.service.ElasticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private static final Logger log = LoggerFactory.getLogger(LogController.class);
    private final ElasticSearchService elasticSearchService;

    public LogController(ElasticSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    @PostMapping
    public void logOnly(@RequestBody LogRequest logRequest) {
        log.info("Пришел запрос: {}", logRequest.getMessage());

        elasticSearchService.logEvent(logRequest.getMessage());
    }
}