# Config & Environment

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | MongoDB host for `bookmark_db`. |
| `DB_PORT` | `27017` | MongoDB port. |
| `DB_USER` | `root` | MongoDB username. |
| `DB_PASSWORD` | `rootpassword` | MongoDB password. |
| `REDIS_HOST` | `localhost` | Redis host. |
| `REDIS_PORT` | `6379` | Redis port. |
| `REDIS_PASSWORD` | `rootpassword` | Redis password. |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Where to consume `bookmark.created` from. |
| `SERVER_PORT` | `8082` | Port this service listens on. |
| `EUREKA_URL` | `http://localhost:8761/eureka` | Eureka `defaultZone`. |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` | Hostname this instance registers under. |

No `JWT_SECRET` — no token validation, `X-User-Id` is trusted as a plain header. Fixed
(not env-driven): consumer `group-id = bookmark-service`, cache TTL `bookmark.cache.ttl-seconds
= 300`.
