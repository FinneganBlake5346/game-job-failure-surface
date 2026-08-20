package dev.infrai.gamejobs.service;

import dev.infrai.gamejobs.domain.FailureNotice;
import dev.infrai.gamejobs.domain.GameJobFailure;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GameJobFailurePolicy {
    public FailureNotice evaluate(GameJobFailure failure) {
        String level = failure.workload() == GameJobFailure.Workload.LIVE_EVENT
                || failure.attempt() >= 3 ? "error" : "warning";
        return new FailureNotice(
                failure.jobName() + " scheduled run failed",
                failure.cause().getClass().getSimpleName() + ": " + failure.cause().getMessage(),
                level,
                List.of("game-job", failure.workload().name(), failure.jobName()),
                stackTrace(failure.cause()),
                Map.of(
                        "execution_id", failure.executionId(),
                        "workload", failure.workload().name(),
                        "subject_id", failure.playerOrEventId(),
                        "attempt", failure.attempt(),
                        "scheduled_at", failure.scheduledAt().toString()));
    }

    private String stackTrace(RuntimeException cause) {
        StringWriter output = new StringWriter();
        cause.printStackTrace(new PrintWriter(output));
        return output.toString();
    }
}
