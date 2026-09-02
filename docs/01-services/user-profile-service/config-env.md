# Config & Environment

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host for `profiles_db` (auto-created if missing). |
| `DB_PORT` | `3306` | MySQL port. |
| `DB_USER` | `root` | MySQL username. |
| `DB_PASSWORD` | `rootpassword` | MySQL password. |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address. |
| `SERVER_PORT` | `8080` | Port this service listens on. |
| `EUREKA_URL` | `http://localhost:8761/eureka` | Eureka `defaultZone`. |
| `EUREKA_INSTANCE_HOSTNAME` | `localhost` | Hostname this instance registers under. |

No `JWT_SECRET` — this service never validates a token, it trusts `X-User-Id`/`X-User-Email`
headers as-is (see the trust-model note in [`README.md`](./README.md)).

Fixed (not env-driven): consumer `group-id = user-profile-service`,
`spring.jpa.hibernate.ddl-auto = update`.
