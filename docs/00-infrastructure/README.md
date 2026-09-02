# Infrastructure

For the whole-system picture (client → API Gateway → services → databases, with the diagram),
see [`../ARCHITECTURE.md`](../ARCHITECTURE.md) at the root of the docs.

This folder holds the build/run detail for each shared infrastructure piece one level deeper
than that diagram:

- [`eureka-service/`](./eureka-service/README.md) — service discovery, **built and running**;
  every backend service registers with it.
- [`api-gateway/`](./api-gateway/README.md) — routing, JWT validation, route protection map,
  CORS. **Not built yet** — this doc is still the plan for what comes next, not running code.
  Until it exists, every service is called directly on its own port and trusts identity headers
  (`X-User-Id`) at face value, with no signature check — see the trust-model note in each
  service's own README.
- `kafka-message-bus/` — not yet drafted as a standalone doc; each service's own `messaging.md`
  is the source of truth for its topics until this is written.
- `redis-cache/` — not yet drafted; Bookmark Service's own docs cover its cache usage.
- [`adr/`](./adr/) — Architecture Decision Records: short write-ups of *why* a system-shape
  decision was made. Referenced from `ARCHITECTURE.md` wherever a diagram needs the reasoning
  behind it.
