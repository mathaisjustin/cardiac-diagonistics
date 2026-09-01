# Infrastructure

For the whole-system picture (client → API Gateway → services → databases, with the diagram),
see [`../ARCHITECTURE.md`](../ARCHITECTURE.md) at the root of the docs.

This folder holds the build/run detail for each shared infrastructure piece one level deeper
than that diagram:

- `eureka-discovery/` — service discovery
- [`api-gateway/`](./api-gateway/README.md) — routing, JWT validation, route protection map, CORS
- `kafka-message-bus/` — the message bus and every topic on it (source of truth for messaging)
- `redis-cache/` — what's cached and why
- [`adr/`](./adr/) — Architecture Decision Records: short write-ups of *why* a system-shape
  decision was made (e.g. why registration is async, why Diagnosis Service has no database).
  Referenced from `ARCHITECTURE.md` wherever a diagram needs the reasoning behind it.

🚧 Eureka, Kafka, and Redis docs not yet drafted — coming next.
