#!/usr/bin/env sh
set -eu

curl --fail-with-body --request POST http://localhost:8080/job-failures \
  --header 'Content-Type: application/json' \
  --data '{
    "executionId": "run-20260819-001",
    "workload": "LIVE_EVENT",
    "jobName": "close-tournament-round",
    "subjectId": "event-42",
    "attempt": 1,
    "scheduledAt": "2026-08-19T12:00:00Z",
    "failureMessage": "round ledger is incomplete"
  }'
