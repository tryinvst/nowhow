package demo.controller;


import demo.module.LogRequest;
import demo.service.ElasticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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