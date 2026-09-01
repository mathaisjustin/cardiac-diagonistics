# UserProfile Service

A **standalone Spring Boot application** (Java, Maven, per
[ADR-0007](../../00-infrastructure/adr/0007-backend-build-and-gateway-tooling.md)) — its own
codebase, build, JVM process, and container, with its own MySQL database
([ADR-0008](../../00-infrastructure/adr/0008-mysql-as-database-engine.md)).

## What this service is, in one sentence

Its main job is to **consume the registration event from Kafka and save it as a profile record**
— everything else (viewing and updating that record) is CRUD built on top of that.

## Responsibilities

- Consume the registration event Authentication publishes (see
  [`messaging.md`](./messaging.md)) and create the profile record — this is how every profile
  record comes to exist. **UserProfile never creates a profile in response to an API call.**
- Let the logged-in user view their own profile.
- Let the logged-in user update their own profile.

## What it does *not* do

- **No creation route.** Profiles are only ever created via the Kafka event, never via a direct
  API call — see [`api-contract.md`](./api-contract.md).
- **No deletion route.** Not in scope.
- **No listing/search route.** There's no "browse other users' profiles" — a user only ever sees
  their own.
- **Does not own login or credentials** — that's Authentication. It doesn't even store a copy of
  `email`; it's read live from the Gateway-forwarded `X-User-Email` header on every request — see
  [`data-model.md`](./data-model.md) and
  [ADR-0013](../../00-infrastructure/adr/0013-email-never-duplicated-into-userprofile.md).

## Docs in this folder

- [`data-model.md`](./data-model.md) — what UserProfile stores.
- [`api-contract.md`](./api-contract.md) — its endpoints (both protected).
- [`messaging.md`](./messaging.md) — how it consumes Authentication's Kafka event.
- [`flows.md`](./flows.md) — profile creation, view, and update, step by step.
- [`config-env.md`](./config-env.md) — what it needs to run.

## How it fits the whole system

See [`../../ARCHITECTURE.md`](../../ARCHITECTURE.md). In short: UserProfile is only ever reached
via the API Gateway, and the Gateway forwards the caller's identity as trusted headers
(`X-User-Id`, `X-User-Email`) rather than this service parsing a JWT itself — see
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md).
