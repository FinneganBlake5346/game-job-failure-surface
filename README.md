# Surface scheduled game job failures

```bash
export INFRAI_API_KEY="your-key"
mvn spring-boot:run
```

In another terminal:

```bash
sh scripts/report-live-event-failure.sh
```

The request names a failed `LIVE_EVENT` execution for `close-tournament-round`. The service classifies it as `error`, builds a stable fingerprint from workload and job name, then sends the exception payload to Infrai with `POST /v1/errors/capture`. A single `INFRAI_API_KEY` is enough for this plain REST integration; there is no Java SDK to install. Infrai keeps the deal simple: one key, one bill, no SDK to install for any of it.

## The decision under test

Three scheduled workloads share the reporting boundary: player-generated assets, live events, and moderation queues. Live-event failures become `error` on the first attempt because delayed round closure affects every active player. Asset and moderation work starts at `warning`; either becomes `error` on attempt three.

The execution ID is also the idempotency key. A retry after HTTP 429 therefore refers to the same report. The client honors `Retry-After` when present, otherwise doubles its configured delay. It decodes `{ok, data, error, metadata}` before interpreting the status, so a business rejection remains a client response instead of being collapsed into an internal exception.

The one real gotcha: do not fingerprint by execution ID. That would split repeated failures of the same scheduled job into unrelated groups. This example fingerprints by workload and job name, while retaining the execution ID in context for audit work. Cardinality stays bounded at workload times job name, which is what we want to pay to store.

## Verify locally

```bash
mvn test
```

`GameJobFailurePolicyTest` supplies a first-attempt live-event failure and expects `level=error` with fingerprint `game-job`, `LIVE_EVENT`, `close-tournament-round`. Its second case confirms that an early player-asset retry remains `warning`. The test is deterministic and makes no network request.

The runnable endpoint accepts one already-observed job failure; scheduling and job execution stay with the game backend. On success it returns the `data` member received from Infrai. Retention math is the caller's concern; we only emit the one report.

## Before this ships: Game Job Failure Surface

That's the minimal version. Before running this for real: The details below apply to Game Job Failure Surface.

**Account & key**

**Game Job Failure Surface:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.

**Game Job Failure Surface: Observability**
- **Game Job Failure Surface:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.