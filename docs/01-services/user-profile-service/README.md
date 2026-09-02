# UserProfile Service

A **standalone Spring Boot 4 application** (`user-profile-service`, Java 17, Maven), its own
MySQL database (`profiles_db`). Registers with Eureka as `cardiac-user-profile-service`.

## What this service is, in one sentence

It consumes the `user.registered` Kafka event and saves it as a profile record — everything else
(viewing and updating that record) is CRUD built on top of that.

## Responsibilities

- Consume `user.registered` (published by [Authentication Service](../authentication-service/README.md))
  and create the profile row. **This is the only way a profile ever comes to exist** — there is
  no API route to create one.
- Let the caller view their own profile (`GET /profile`).
- Let the caller update their own profile (`PUT /profile`).

## What it does *not* do

- **No creation or deletion route.** Profiles are created only via Kafka.
- **No listing/search route.** A caller only ever sees their own profile.
- **Does not own login or credentials** — that's Authentication.
- **Does not store `email`.** It's echoed back on every response straight from the caller-supplied
  `X-User-Email` header, never persisted — see [`data-model.md`](./data-model.md).

## Docs in this folder

- [`data-model.md`](./data-model.md) — the `profiles` table.
- [`api-contract.md`](./api-contract.md) — both endpoints.
- [`messaging.md`](./messaging.md) — how it consumes Authentication's Kafka event.
- [`flows.md`](./flows.md) — profile creation, view, and update.
- [`config-env.md`](./config-env.md) — environment variables it reads.

## How it fits the whole system, today

There's no API Gateway yet. Callers hit this service directly on port `8080` and are expected to
send `X-User-Id` and `X-User-Email` headers themselves — the service reads and trusts them as-is,
with **no signature check, no token validation of any kind**. This is a stand-in for the header
contract [ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)
describes for once the Gateway exists and sets these headers itself after verifying a JWT. Until
then, this service's "auth" is only as strong as whatever sits in front of it — do not expose
this port publicly as-is.
