# Infrastructure

For the whole-system picture (client → API Gateway → services → databases, with the diagram),
see [`../ARCHITECTURE.md`](../ARCHITECTURE.md) at the root of the docs.

This folder holds the build/run detail for each shared infrastructure piece one level deeper
than that diagram:

- `eureka-discovery/` — service discovery
- `api-gateway/` — routing, JWT validation, CORS
- `kafka-message-bus/` — the message bus and every topic on it (source of truth for messaging)
- `redis-cache/` — what's cached and why

🚧 Not yet drafted — coming next.
