# Bookmark Service

A **standalone Spring Boot 4 application** (`cardiac-bookmark-service`, Java 17, Maven), its own
MongoDB database (`bookmark_db`) and a Redis cache in front of it. Registers with Eureka as
`cardiac-bookmark-service`.

## What this service is, in one sentence

Consumes bookmark-creation events published by [Diagnosis Service](../diagnosis-service/README.md)
over Kafka, stores a snapshot in MongoDB, and lets a user view/remove their own saved records —
fully self-contained on read, no live dependency on Diagnosis Service once something's bookmarked.

## Responsibilities

- Consume `bookmark.created` from Kafka and save a snapshot of the diagnosis record against the
  user it belongs to. **This is the only way a bookmark ever gets created** — there is no
  `POST /bookmarks` route.
- List a user's own bookmarks (`GET /bookmarks`).
- Remove a bookmark (`DELETE /bookmarks/{id}`).
- Cache a user's bookmark list in Redis so repeated views don't hit MongoDB every time.

## What it does *not* do

- **No creation route.** Bookmarking is entirely event-driven — see
  [`messaging.md`](./messaging.md).
- **No call to Diagnosis Service, ever, in either direction.** The snapshot already contains
  everything this service needs to display a bookmark; it never needs Diagnosis Service (or the
  external API behind it) to be up.
- **No duplicate bookmarks.** The Kafka consumer is idempotent on `(userId, diagnosisId)` — a
  repeat event for something already saved is logged and skipped.

## Docs in this folder

- [`data-model.md`](./data-model.md) — the `bookmarks` MongoDB collection.
- [`api-contract.md`](./api-contract.md) — its two routes.
- [`messaging.md`](./messaging.md) — how it consumes Diagnosis Service's Kafka event.
- [`flows.md`](./flows.md) — creation (via Kafka), view, and remove.
- [`config-env.md`](./config-env.md) — what it needs to run.

## How it fits the whole system

There's no API Gateway yet — clients call this service directly on port `8082`, with `X-User-Id`
as a plain trusted header (no signature check). It never talks to Diagnosis Service directly;
the only connection between the two is the one-way `bookmark.created` Kafka topic.
