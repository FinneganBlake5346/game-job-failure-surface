package dev.infrai.gamejobs;

import dev.infrai.gamejobs.config.InfraiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InfraiProperties.class)
public class GameJobVisibilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameJobVisibilityApplication.class, args);
    }
}
