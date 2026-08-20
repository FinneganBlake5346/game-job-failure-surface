package dev.infrai.gamejobs.domain;

import java.util.List;
import java.util.Map;

public record FailureNotice(
        String title,
        String message,
        String level,
        List<String> fingerprint,
        String exception,
        Map<String, Object> context) {
}
