# Config & Environment

| Variable | Default | Purpose |
|---|---|---|
| `EXTERNAL_DIAGNOSIS_API_URL` | `http://localhost:3232` | Base URL of the real external Diagnosis API (`stackroutenew/diagnosisapi` container). |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Where to publish `bookmark.created` events. |
| `EUREKA_URL` | `http://localhost:8761/eureka` | Eureka `defaultZone`. |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` | Hostname this instance registers under. |

Fixed (not env-driven): `server.port = 8083`, `bookmark.kafka.topic = bookmark.created`.

No `DB_*` variables — no database ([ADR-0003](../../00-infrastructure/adr/0003-diagnosis-service-stateless-no-db.md)).
No `JWT_SECRET` — no token validation, `X-User-Id` is trusted as a plain header.
