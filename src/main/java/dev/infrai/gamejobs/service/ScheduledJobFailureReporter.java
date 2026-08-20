package dev.infrai.gamejobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import dev.infrai.gamejobs.domain.FailureNotice;
import dev.infrai.gamejobs.domain.GameJobFailure;
import dev.infrai.gamejobs.infra.InfraiErrorClient;
import org.springframework.stereotype.Service;

@Service
public class ScheduledJobFailureReporter {
    private final GameJobFailurePolicy policy;
    private final InfraiErrorClient client;

    public ScheduledJobFailureReporter(GameJobFailurePolicy policy, InfraiErrorClient client) {
        this.policy = policy;
        this.client = client;
    }

    public JsonNode report(GameJobFailure failure) {
        FailureNotice notice = policy.evaluate(failure);
        return client.capture(notice, failure.executionId());
    }
}
