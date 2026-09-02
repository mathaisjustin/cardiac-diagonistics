# Authentication Service

A **standalone Spring Boot 4 application** (`cardiac-auth-service`, Java 17, Maven), its own
MySQL database (`auth_db`), its own container. Owns identity: registration, login, refresh
tokens, logout, and password change. Registers with Eureka as `cardiac-auth-service`.

## Responsibilities

- Register new users (validates input, hashes the password, stores the credential).
- Authenticate login attempts and issue access + refresh tokens.
- Rotate and revoke refresh tokens (`/refresh`, `/logout`).
- Let a logged-in user change their password (`/change-password`), which also revokes their
  existing refresh token.
- Publish the one-way registration event `user.registered` to Kafka, consumed by
  [UserProfile Service](../user-profile-service/README.md) to create the profile record.

## What it does *not* do

- Does not own profile data (name, contact number, department) beyond what's needed to publish
  the registration event — those fields live in UserProfile Service's own table, not here.
- Does not validate an `Authorization` header itself for downstream services — there is no API
  Gateway yet (see [`docs/00-infrastructure/api-gateway/README.md`](../../00-infrastructure/api-gateway/README.md),
  currently a plan, not running code). Until the Gateway exists, `change-password` is the only
  route in this service that requires a JWT, and it's validated by this service's own
  `JwtAuthenticationFilter` directly — not by a gateway.
- Does not maintain a server-side blocklist beyond the single-row-per-user refresh token table —
  logout works by marking that row `revoked`, not by tracking a list of dead access tokens (access
  tokens simply expire in 15 minutes).

## Docs in this folder

- [`data-model.md`](./data-model.md) — the `users` and `refresh_tokens` tables.
- [`api-contract.md`](./api-contract.md) — all 5 endpoints.
- [`security.md`](./security.md) — password hashing, JWT contents/signing/expiry, refresh token
  mechanics, logout.
- [`messaging.md`](./messaging.md) — the `user.registered` Kafka event.
- [`flows.md`](./flows.md) — registration, login, refresh, logout, change-password.
- [`config-env.md`](./config-env.md) — environment variables it reads.

## How it fits the whole system

See [`../../ARCHITECTURE.md`](../../ARCHITECTURE.md). Today, without a Gateway yet, clients call
this service directly on port `8081`. `POST /api/auth/register`, `/login`, `/refresh`, and
`/logout` are all public routes (no token required); `POST /api/auth/change-password` requires a
valid access token in the `Authorization: Bearer <token>` header. Authentication reaches
UserProfile Service only asynchronously, via Kafka — never a direct call.
