# System Architecture — Overview

This is the single picture of the whole system: every piece from the client in the browser
down to the databases each service owns. Read this first before opening any individual
service doc — those go one level deeper into a single box on this diagram.

## Diagram

```mermaid
flowchart TD
    Client["React Frontend<br/>(Client)"]

    subgraph Edge["Edge"]
        Gateway["API Gateway<br/>routing · JWT check · CORS"]
    end

    subgraph Infra["Shared Infrastructure"]
        Eureka["Eureka<br/>Service Discovery"]
        Kafka["Kafka<br/>Message Bus"]
        Redis[("Redis<br/>Cache")]
    end

    subgraph Services["Backend Services"]
        UserProfile["UserProfile Service"]
        Auth["Authentication Service"]
        Diagnosis["Diagnosis Service"]
        Bookmark["Bookmark Service"]
    end

    subgraph Data["Databases (one per service)"]
        UserProfileDB[("UserProfile DB")]
        AuthDB[("Auth DB")]
        BookmarkDB[("Bookmark DB")]
    end

    ExternalAPI["External Diagnosis API<br/>(json-server, port 3232)"]

    Client -- "REST calls" --> Gateway
    Gateway -- "routes request" --> UserProfile
    Gateway -- "routes request" --> Auth
    Gateway -- "routes request" --> Diagnosis
    Gateway -- "routes request" --> Bookmark

    UserProfile -. "registers with" .-> Eureka
    Auth -. "registers with" .-> Eureka
    Diagnosis -. "registers with" .-> Eureka
    Bookmark -. "registers with" .-> Eureka
    Gateway -. "looks up services via" .-> Eureka

    UserProfile -- "publishes new-user credentials" --> Kafka
    Kafka -- "delivers credentials to" --> Auth

    UserProfile --> UserProfileDB
    Auth --> AuthDB
    Bookmark --> BookmarkDB
    Bookmark -- "read/write cache" --> Redis

    Diagnosis -- "fetches records" --> ExternalAPI
```

## What each piece is for

| Component | Role |
|---|---|
| **React Frontend** | The only thing the end user sees. Talks to the backend exclusively through the API Gateway — never calls a service directly. |
| **API Gateway** | Single front door for every request. Validates the JWT on incoming calls, applies CORS rules, and routes each request to the right service by asking Eureka where that service currently lives. |
| **Eureka (Service Discovery)** | A directory of "who's alive and where." Every service registers itself on startup; the Gateway (and services that call each other) look up addresses here instead of hardcoding them. |
| **Kafka (Message Bus)** | Carries events between services that shouldn't call each other directly. Today's only flow: UserProfile publishes new-user credentials when someone registers, and Authentication consumes them to create the login record. |
| **Redis (Cache)** | Speeds up reads for data that's requested often and changes rarely — currently a user's bookmark list, so the Bookmark Service doesn't hit its database on every page load. |
| **UserProfile Service** | Owns a user's personal/profile data. Handles registration and profile updates. |
| **Authentication Service** | Owns credentials and sessions. Handles login/logout, password reset, and issues/validates JWTs. |
| **Diagnosis Service** | Fetches and serves diagnosis records from the external Diagnosis API — it doesn't own its own database, it's a pass-through/aggregation layer over that external source. |
| **Bookmark Service** | Owns which diagnosis records a user has saved. |
| **External Diagnosis API** | Not part of this system — a separate data source (json-server) the Diagnosis Service calls out to. |

## How services talk to each other

- **Client → backend**: always synchronous, always through the API Gateway. The frontend never
  knows a service's real address — only the Gateway's.
- **Service → service (direct)**: synchronous calls between services, when one needs an
  immediate answer from another, are routed the same way — via the Gateway/Eureka lookup, not
  hardcoded URLs.
- **Service → service (event)**: asynchronous, via Kafka, for anything that doesn't need an
  immediate response. Today that's just the registration hand-off from UserProfile to
  Authentication — full topic-by-topic detail lives in the Kafka doc once written.
- **Service → cache**: only the Bookmark Service talks to Redis today.
- **Service → external system**: only the Diagnosis Service talks outward, to the external
  Diagnosis API.

## Data ownership

Each service owns its own data — no service reaches into another service's database directly.
If a service needs data it doesn't own, it asks for it (via the Gateway/Eureka, or via Kafka),
never queries another service's DB.

| Service | Owns |
|---|---|
| UserProfile Service | User profile/personal details |
| Authentication Service | Credentials, sessions, JWT state |
| Bookmark Service | Bookmarked records, cached in Redis |
| Diagnosis Service | Nothing persisted — reads live from the external Diagnosis API |

## Key decisions

These were settled during architecture review — see the linked ADR for the reasoning behind
each:

- [ADR-0001](./adr/0001-async-registration-via-kafka.md) — registration hands off from
  UserProfile to Authentication via Kafka, not a direct call.
- [ADR-0002](./adr/0002-centralized-jwt-validation-at-gateway.md) — JWT validation happens once,
  at the Gateway, not in every service.
- [ADR-0003](./adr/0003-diagnosis-service-stateless-no-db.md) — Diagnosis Service stays
  stateless, no local database; it calls the external API live.
- [ADR-0004](./adr/0004-registration-race-handled-by-generic-login-error.md) — a login attempted
  before registration finishes processing just gets a generic "invalid credentials" error.
- [ADR-0005](./adr/0005-stateless-jwt-validation-at-gateway.md) — the Gateway validates JWTs
  itself with a shared key, no network call to Authentication per request.

Bookmark Service's Redis cache is invalidated (not updated) on every add/remove, so the next read
repopulates it — full detail lands in the Redis doc once written.

## Where to go deeper

- [`00-infrastructure/`](./00-infrastructure/README.md) — build/run notes for Eureka, API
  Gateway, Kafka, and Redis individually.
- `01-services/` — one folder per backend service, each with its own responsibilities, data,
  and key flows.
- `02-frontend/` — the React app.
- `04-deployment/` — the Docker Compose setup that brings all of this up with one command.

## Status

✅ The system-wide shape above (service boundaries, sync vs. async communication, auth flow,
data ownership) has been through architecture review — see **Key decisions**. Deeper,
service-specific decisions (exact DB technology per service, Kafka topic/payload schemas, API
contracts) are still open and will be grilled when each service's own doc is drafted.
