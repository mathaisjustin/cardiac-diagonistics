# Diagnosis Service

A **standalone Spring Boot 4 application** (`cardiac-diagnosis-service`, Java 17, Maven), its own
container, port `8083`. Registers with Eureka as `cardiac-diagnosis-service`.

## What this service is, in one sentence

A stateless request/response layer over the **real external Diagnosis API**
(`stackroutenew/diagnosisapi`, port `3232`) — it fetches the full dataset live on every call,
filters/aggregates it in memory, and never stores anything of its own; it also owns the
**bookmark-creation route**, since this is where the diagnostic data actually lives.

## Responsibilities

- Serve diagnosis records — list, single-record detail, advanced search, treatment analysis — by
  calling out to the external Diagnosis API.
- Own `POST /diagnosis/{id}/bookmark`: resolve the record, snapshot it, and publish it to Kafka
  for [Bookmark Service](../bookmark-service/README.md) to consume and store.

## What it does *not* do

- **No database.** See [ADR-0003](../../00-infrastructure/adr/0003-diagnosis-service-stateless-no-db.md).
- **No direct call to Bookmark Service, in either direction.** The two services only ever
  communicate through the `bookmark.created` Kafka topic — this service is publish-only, never a
  consumer.
- **No write route on the external API.** Nothing in this system creates diagnosis data.

## Docs in this folder

- [`api-contract.md`](./api-contract.md) — its five routes.
- [`messaging.md`](./messaging.md) — the `bookmark.created` event it publishes.
- [`flows.md`](./flows.md) — browse, search, analysis, and bookmark creation.
- [`config-env.md`](./config-env.md) — what it needs to run.

## How it fits the whole system

There's no API Gateway yet — clients call this service directly on port `8083`. Browsing routes
(`GET /diagnosis`, `GET /diagnosis/{id}`) work with or without an `X-User-Id` header (detail
level changes accordingly); search, analysis, and bookmark creation require it. See
[`api-contract.md`](./api-contract.md).
