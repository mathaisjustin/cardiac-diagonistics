# Config & Environment

What Bookmark Service needs to run. Exact variable names are placeholders — finalize when the
service is actually scaffolded.

| Variable | Purpose |
|---|---|
| `SERVER_PORT` | Port this service listens on. |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Connection to its own MySQL database (`bookmark_db`). |
| `REDIS_URL` | Connection to the shared Redis cache. |
| `EUREKA_URL` | Where to register itself, and where to look up Diagnosis Service for the direct call. |

No `JWT_SECRET` and no `KAFKA_BROKER_URL` — this service trusts the Gateway-forwarded
`X-User-Id` header rather than validating tokens itself
([ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)), and
doesn't use Kafka ([`messaging.md`](./messaging.md)).
