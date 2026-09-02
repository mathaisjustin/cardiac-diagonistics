# Diagnosis Service

A **standalone Spring Boot application** (Java, Maven, per
[ADR-0007](../../00-infrastructure/adr/0007-backend-build-and-gateway-tooling.md)) — its own
codebase, build, JVM process, and container.

## What this service is, in one sentence

A stateless request/response layer over the **external Diagnosis API** — it fetches, filters,
and aggregates data live, on every call; it never stores anything of its own.

## Responsibilities

- Serve diagnosis records — list, single-record detail, advanced search, treatment analysis —
  by calling out to the external Diagnosis API (json-server, port 3232) on every request.
- Respond to a **direct call from Bookmark Service** confirming whether a given record ID
  actually exists, before Bookmark saves a reference to it.

## What it does *not* do

- **No database.** See [ADR-0003](../../00-infrastructure/adr/0003-diagnosis-service-stateless-no-db.md)
  — the external API is a cheap local call, not worth mirroring.
- **No Kafka.** Nothing here is fire-and-forget: every interaction this service has needs an
  immediate answer (a client waiting on search results, or Bookmark Service waiting to know if a
  record is valid before saving), so everything is a direct request/response call, not an event.
  See [`messaging.md`](./messaging.md) for why this file exists just to say that.
- **No write route.** The external API technically supports `POST /diagnosis`, but nothing in
  this system's backlog creates diagnosis data — not exposed here.

## Docs in this folder

- [`api-contract.md`](./api-contract.md) — its four routes, two public, two protected.
- [`messaging.md`](./messaging.md) — why this service doesn't use Kafka.
- [`flows.md`](./flows.md) — browse, search, analysis, and Bookmark's validation call.
- [`config-env.md`](./config-env.md) — what it needs to run.

## How it fits the whole system

See [`../../ARCHITECTURE.md`](../../ARCHITECTURE.md). Reached two ways: from the client via the
API Gateway (like every other service), and directly from Bookmark Service via a synchronous
call routed through Eureka — the one real example of a direct service-to-service call in this
system.
