# Key Flows

## Browse / view a record — `GET /diagnosis`, `GET /diagnosis/{id}`

```mermaid
sequenceDiagram
    actor U as Guest or Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant D as Diagnosis Service
    participant EXT as External Diagnosis API

    U->>FE: opens diagnosis list / clicks a record
    FE->>GW: GET /diagnosis (or /diagnosis/{id})
    Note over GW: public route — no token needed
    GW->>D: forward
    D->>EXT: fetch live
    EXT-->>D: records
    D-->>FE: 200 + data
```

Both routes are public — Guest and Registered Users alike (US-04). Basic filtering across the
list the frontend already has is done client-side; not a backend concern.

## Advanced search — `GET /diagnosis/search` (Protected)

```mermaid
sequenceDiagram
    actor U as Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant D as Diagnosis Service
    participant EXT as External Diagnosis API

    U->>FE: sets filters (pain type, age, bp, gender)
    FE->>GW: GET /diagnosis/search?... (with JWT)
    GW->>GW: validate JWT
    GW->>D: forward
    D->>EXT: fetch (filtered where the external API supports it)
    EXT-->>D: matching records (or none)
    D-->>FE: 200 + results (possibly empty)
    FE->>U: results, or "no results" message
```

Registered-only (US-05, deliberately restricted from the original "Guest or Registered" spec —
see [ADR-0009](../../00-infrastructure/adr/0009-route-level-authorization-at-gateway.md) for how
the Gateway enforces this). An empty result set is a normal 200 response, not an error.

## Treatment analysis — `GET /diagnosis/analysis` (Protected)

```mermaid
sequenceDiagram
    actor U as Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant D as Diagnosis Service
    participant EXT as External Diagnosis API

    U->>FE: selects a characteristic (age / gender / pain type)
    FE->>GW: GET /diagnosis/analysis?by=... (with JWT)
    GW->>GW: validate JWT
    GW->>D: forward
    D->>EXT: fetch entire dataset
    EXT-->>D: all records
    D->>D: aggregate treatment counts by characteristic
    D-->>FE: 200 + breakdown
```

Registered-only per US-06. Always runs against the **full** dataset, not just current search
results — a fresh fetch + aggregation on every call, no caching (see
[`api-contract.md`](./api-contract.md)).

## Bookmark validation — the direct call

```mermaid
sequenceDiagram
    actor U as Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant B as Bookmark Service
    participant D as Diagnosis Service
    participant EXT as External Diagnosis API

    U->>FE: clicks "bookmark" on a record
    FE->>GW: POST /bookmarks (with JWT)
    GW->>B: forward
    Note over B,D: direct call, not through the Gateway —<br/>routed via Eureka lookup like any service-to-service call
    B->>D: GET /diagnosis/{id}
    D->>EXT: fetch live
    EXT-->>D: record, or not found

    alt record exists
        D-->>B: 200 + record data
        B->>B: save bookmark
        B-->>FE: 201 confirmed
    else record not found
        D-->>B: 404
        B-->>FE: error — can't bookmark, record doesn't exist
    end
```

This is the one real direct service-to-service call in this system (see
[`messaging.md`](./messaging.md) for why it isn't Kafka). Exact request/response shape for
Bookmark's own side — snapshot vs. thin reference, exactly what `POST /bookmarks` looks like —
is deferred to Bookmark Service's own docs.
