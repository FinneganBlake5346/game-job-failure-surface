package dev.infrai.gamejobs.domain;

import java.time.Instant;

public record GameJobFailure(
        String executionId,
        Workload workload,
        String jobName,
        String playerOrEventId,
        int attempt,
        Instant scheduledAt,
        RuntimeException cause) {

    public enum Workload {
        PLAYER_ASSET,
        LIVE_EVENT,
        MODERATION_QUEUE
    }
}
