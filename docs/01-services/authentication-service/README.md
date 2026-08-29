# Authentication Service

Owns identity: who a user is, their credentials, and proving that identity to the rest of the
system via JWTs. Since [ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md),
this service also owns **registration** — it's the entry point for creating a new user, not
just for logging an existing one in.

## Scope of this phase

Only **registration** and **login** are being built and documented right now. Password reset
(backlog story US-03) is explicitly **deferred** — see [`backlog.md`](./backlog.md).

## Responsibilities

- Accept new-user registration: validate input, create the user, store the credential.
- Authenticate login attempts and issue a JWT.
- Own the shared secret the API Gateway uses to validate JWTs statelessly (see
  [ADR-0005](../../00-infrastructure/adr/0005-stateless-jwt-validation-at-gateway.md)).
- Publish the one-way registration event UserProfile Service consumes to build the profile
  record (see [ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md)).

## What it does *not* do

- It does not own profile data (name, personal details) — that's UserProfile Service, created
  asynchronously after registration.
- It does not validate routes or decide what's protected — that's the Gateway
  ([ADR-0009](../../00-infrastructure/adr/0009-route-level-authorization-at-gateway.md)). Auth
  only issues and can be asked to sign tokens; it doesn't gate access itself.
- It does not keep a server-side session or token blocklist — see [`security.md`](./security.md)
  for why logout is purely client-side.

## Docs in this folder

- [`data-model.md`](./data-model.md) — what Authentication stores.
- [`api-contract.md`](./api-contract.md) — its endpoints.
- [`security.md`](./security.md) — password hashing, JWT contents/signing/expiry, logout.
- [`messaging.md`](./messaging.md) — the Kafka event it publishes after registration.
- [`flows.md`](./flows.md) — registration and login, step by step.
- [`config-env.md`](./config-env.md) — what it needs to run.
- [`backlog.md`](./backlog.md) — what's deliberately deferred, and why.

## How it fits the whole system

See [`../../ARCHITECTURE.md`](../../ARCHITECTURE.md) for the system-wide picture. In short:
the client reaches Authentication only through the API Gateway; Authentication reaches
UserProfile only asynchronously, via Kafka, never a direct call.
