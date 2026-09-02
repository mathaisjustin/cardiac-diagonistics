# Config & Environment

What Diagnosis Service needs to run. Exact variable names are placeholders — finalize when the
service is actually scaffolded.

| Variable | Purpose |
|---|---|
| `SERVER_PORT` | Port this service listens on. |
| `EXTERNAL_DIAGNOSIS_API_URL` | Base URL of the external Diagnosis API (`http://localhost:3232` per the case study's Docker setup, or the container's internal address once everything runs in `docker-compose.yml`). |
| `EUREKA_URL` | Where to register itself — needed both for the Gateway to route to it, and for Bookmark Service to look it up for the direct call. |

No `JWT_SECRET`, no `DB_URL`, no `KAFKA_BROKER_URL` — this service doesn't validate tokens itself
(the Gateway already did, per [ADR-0005](../../00-infrastructure/adr/0005-stateless-jwt-validation-at-gateway.md)),
has no database ([ADR-0003](../../00-infrastructure/adr/0003-diagnosis-service-stateless-no-db.md)),
and doesn't use Kafka ([`messaging.md`](./messaging.md)).
