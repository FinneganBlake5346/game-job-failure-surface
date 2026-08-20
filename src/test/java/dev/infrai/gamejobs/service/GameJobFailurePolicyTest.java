package dev.infrai.gamejobs.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.infrai.gamejobs.domain.FailureNotice;
import dev.infrai.gamejobs.domain.GameJobFailure;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class GameJobFailurePolicyTest {
    private final GameJobFailurePolicy policy = new GameJobFailurePolicy();

    @Test
    void escalatesLiveEventOnItsFirstFailedAttempt() {
        GameJobFailure failure = new GameJobFailure(
                "run-20260819-001",
                GameJobFailure.Workload.LIVE_EVENT,
                "close-tournament-round",
                "event-42",
                1,
                Instant.parse("2026-08-19T12:00:00Z"),
                new IllegalStateException("round ledger is incomplete"));

        FailureNotice notice = policy.evaluate(failure);

        assertThat(notice.level()).isEqualTo("error");
        assertThat(notice.fingerprint())
                .containsExactly("game-job", "LIVE_EVENT", "close-tournament-round");
        assertThat(notice.context()).containsEntry("execution_id", "run-20260819-001");
    }

    @Test
    void keepsEarlyAssetRetryAtWarningSeverity() {
        GameJobFailure failure = new GameJobFailure(
                "run-20260819-002",
                GameJobFailure.Workload.PLAYER_ASSET,
                "scan-player-banner",
                "player-7",
                2,
                Instant.parse("2026-08-19T12:01:00Z"),
                new IllegalArgumentException("image dimensions rejected"));

        assertThat(policy.evaluate(failure).level()).isEqualTo("warning");
    }
}
