# Config & Environment

What UserProfile Service needs to run. Exact variable names are placeholders — finalize when the
service is actually scaffolded.

| Variable | Purpose |
|---|---|
| `SERVER_PORT` | Port this service listens on. |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Connection to its own MySQL database (`userprofile_db`). |
| `KAFKA_BROKER_URL` | Where to consume the registration event from. |
| `KAFKA_CONSUMER_GROUP` | Consumer group ID for this service's subscription to Authentication's topic. |
| `EUREKA_URL` | Where to register itself for service discovery. |

No `JWT_SECRET` here — this service never validates a token itself, it trusts the `X-User-Id` /
`X-User-Email` headers the Gateway already validated and forwarded (see
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)).
