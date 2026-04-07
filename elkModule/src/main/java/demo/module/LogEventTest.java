package demo.module;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class LogEventTest {
    private final String id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private final Instant timestamp;

    private final String level;
    private final String message;

      public LogEventTest(@JsonProperty("id") String id,
                          @JsonProperty("level") String level,
                          @JsonProperty("timestamp") Instant timestamp,
                          @JsonProperty("message") String message) {
        this.id = id;
        this.level = level;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return level + "\n" + message + "\n" + timestamp;
    }

    public static LogEventTest event(String message) {
        return new LogEventTest (
                UUID.randomUUID().toString(),
                "INFO",
                Instant.now(),
                message
        );
    }
}