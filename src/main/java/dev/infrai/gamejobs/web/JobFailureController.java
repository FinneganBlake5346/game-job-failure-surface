package dev.infrai.gamejobs.web;

import com.fasterxml.jackson.databind.JsonNode;
import dev.infrai.gamejobs.domain.GameJobFailure;
import dev.infrai.gamejobs.infra.InfraiException;
import dev.infrai.gamejobs.service.ScheduledJobFailureReporter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job-failures")
public class JobFailureController {
    private final ScheduledJobFailureReporter reporter;

    public JobFailureController(ScheduledJobFailureReporter reporter) {
        this.reporter = reporter;
    }

    @PostMapping
    public ResponseEntity<JsonNode> report(@Valid @RequestBody FailureRequest request) {
        GameJobFailure failure = new GameJobFailure(
                request.executionId(), request.workload(), request.jobName(), request.subjectId(),
                request.attempt(), request.scheduledAt(), new RuntimeException(request.failureMessage()));
        return ResponseEntity.accepted().body(reporter.report(failure));
    }

    @ExceptionHandler(InfraiException.class)
    public ResponseEntity<Map<String, Object>> rejected(InfraiException exception) {
        int status = exception.status() >= 400 && exception.status() < 500 ? exception.status() : 502;
        return ResponseEntity.status(status).body(Map.of("error", exception.detail()));
    }

    public record FailureRequest(
            @NotBlank String executionId,
            @NotNull GameJobFailure.Workload workload,
            @NotBlank String jobName,
            @NotBlank String subjectId,
            @Min(1) int attempt,
            @NotNull Instant scheduledAt,
            @NotBlank String failureMessage) {
    }
}
