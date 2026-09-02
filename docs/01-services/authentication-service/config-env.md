# Config & Environment

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host for `auth_db` (auto-created if missing). |
| `DB_PORT` | `3306` | MySQL port. |
| `DB_USER` | *(required, no default)* | MySQL username. |
| `DB_PASSWORD` | *(required, no default)* | MySQL password. |
| `JWT_SECRET` | *(required, no default)* | HMAC signing key for access tokens. |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address. |
| `EUREKA_URL` | `http://localhost:8761/eureka` | Eureka `defaultZone`. |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` | Hostname this instance registers under. |

Fixed (not env-driven): `server.port = 8081`, `jwt.access-token-expiration = 900000` (15 min),
`jwt.refresh-token-expiration = 604800000` (7 days), `spring.jpa.hibernate.ddl-auto = update`.

Nothing here for email/SMTP — password reset isn't built.
