package dev.infrai.gamejobs.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.infrai.gamejobs.config.InfraiProperties;
import dev.infrai.gamejobs.domain.FailureNotice;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InfraiErrorClient {
    private static final String CAPTURE_PATH = "/v1/errors/capture";
    private final InfraiProperties properties;
    private final ObjectMapper mapper;
    private final HttpClient http;

    @Autowired
    public InfraiErrorClient(InfraiProperties properties, ObjectMapper mapper) {
        this(properties, mapper, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    InfraiErrorClient(InfraiProperties properties, ObjectMapper mapper, HttpClient http) {
        this.properties = properties;
        this.mapper = mapper;
        this.http = http;
    }

    public JsonNode capture(FailureNotice notice, String executionId) {
        Duration delay = properties.initialBackoff();
        for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++) {
            HttpResponse<String> response = sendCapture(notice, executionId);
            JsonNode envelope = decodeEnvelope(response.body(), response.statusCode());
            if (response.statusCode() == 429 && attempt < properties.maxAttempts()) {
                delay = pause(retryDelay(response, delay));
                continue;
            }
            if (!envelope.path("ok").asBoolean(false)) {
                JsonNode error = envelope.path("error");
                throw new InfraiException(error.path("code").asText("REQUEST_REJECTED"), error, response.statusCode());
            }
            return envelope.path("data");
        }
        throw new IllegalStateException("retry loop exhausted");
    }

    private HttpResponse<String> sendCapture(FailureNotice notice, String executionId) {
        try {
            String body = mapper.writeValueAsString(notice);
            HttpRequest request = HttpRequest.newBuilder(properties.baseUrl().resolve(CAPTURE_PATH))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", executionId)
                    .method("POST", HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new IllegalStateException("Infrai transport failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Infrai request interrupted", exception);
        }
    }

    private JsonNode decodeEnvelope(String body, int status) {
        try {
            return mapper.readTree(body);
        } catch (IOException exception) {
            throw new IllegalStateException("Invalid response envelope at HTTP " + status, exception);
        }
    }

    private Duration retryDelay(HttpResponse<?> response, Duration fallback) {
        return response.headers().firstValue("Retry-After")
                .map(value -> Duration.ofSeconds(Long.parseLong(value)))
                .orElse(fallback);
    }

    private Duration pause(Duration delay) {
        try {
            Thread.sleep(delay);
            return delay.multipliedBy(2);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted", exception);
        }
    }
}
