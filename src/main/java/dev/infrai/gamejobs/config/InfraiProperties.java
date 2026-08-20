package dev.infrai.gamejobs.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("infrai")
public record InfraiProperties(
        URI baseUrl,
        String apiKey,
        int maxAttempts,
        Duration initialBackoff) {

    public InfraiProperties {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("INFRAI_API_KEY must be set");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
    }
}
