# Bookmark Service

A **standalone Spring Boot application** (Java, Maven, per
[ADR-0007](../../00-infrastructure/adr/0007-backend-build-and-gateway-tooling.md)) — its own
codebase, build, JVM process, and container, with its own MySQL database
([ADR-0008](../../00-infrastructure/adr/0008-mysql-as-database-engine.md)) and a Redis cache in
front of it.

## What this service is, in one sentence

Lets a Registered User save a **snapshot** of a diagnosis record they care about, and view/manage
that saved list later — fully self-contained, no live dependency on Diagnosis Service once
something's bookmarked.

## Responsibilities

- Validate a record exists (via a direct call to Diagnosis Service) and save a **snapshot** of
  it against the caller's account when they bookmark it.
- List a user's own bookmarks.
- Remove a bookmark.
- Cache a user's bookmark list in Redis so repeated views don't hit MySQL every time.

## What it does *not* do

- **No Kafka** — same reasoning as Diagnosis Service: every interaction here needs an immediate
  answer (bookmark saved? here's my list? removed?). See [`messaging.md`](./messaging.md).
- **Doesn't re-fetch from Diagnosis Service to display bookmarks.** The whole point of storing a
  snapshot is that `GET /bookmarks` never needs Diagnosis Service (or the external API behind
  it) to be up — see [ADR-0014](../../00-infrastructure/adr/0014-bookmark-stores-snapshot-not-reference.md).
- **No duplicate bookmarks.** Bookmarking an already-bookmarked record is a no-op success, not a
  new row or an error — see [`api-contract.md`](./api-contract.md).

## Docs in this folder

- [`data-model.md`](./data-model.md) — the `bookmarks` table.
- [`api-contract.md`](./api-contract.md) — its three routes, all protected.
- [`messaging.md`](./messaging.md) — why no Kafka, and the direct call to Diagnosis Service.
- [`flows.md`](./flows.md) — bookmark, view, and remove, step by step.
- [`config-env.md`](./config-env.md) — what it needs to run.

## How it fits the whole system

See [`../../ARCHITECTURE.md`](../../ARCHITECTURE.md). Reached via the API Gateway like every
other service (identity via `X-User-Id`, per
[ADR-0012](../../00-infrastructure/adr/0012-gateway-forwards-identity-via-headers.md)), and calls
Diagnosis Service directly — the one real service-to-service call in this system — only at
bookmark-creation time.
