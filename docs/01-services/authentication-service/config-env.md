# Config & Environment

What Authentication Service needs to run. Exact variable names are placeholders — finalize when
the service is actually scaffolded.

| Variable | Purpose |
|---|---|
| `SERVER_PORT` | Port this service listens on. |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Connection to its own MySQL database (`authentication_db`). |
| `JWT_SECRET` | Signing key for issuing tokens — shared with the API Gateway, which uses the same key to validate them ([ADR-0005](../../00-infrastructure/adr/0005-stateless-jwt-validation-at-gateway.md)). |
| `JWT_EXPIRY_MINUTES` | Token lifetime — 60, per [`security.md`](./security.md). |
| `KAFKA_BROKER_URL` | Where to publish the registration event. |
| `EUREKA_URL` | Where to register itself for service discovery. |

Nothing here for email/SMTP — that's part of password reset, which is deferred. See
[`BACKLOG.md`](../../BACKLOG.md).
