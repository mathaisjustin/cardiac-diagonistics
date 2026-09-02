# Key Flows

## Browse / view a record

```mermaid
sequenceDiagram
    actor U as Caller (guest or authenticated)
    participant D as Diagnosis Service
    participant EXT as External Diagnosis API

    U->>D: GET /diagnosis (or /diagnosis/{id})
    D->>EXT: GET /diagnosis (fetches full dataset live)
    EXT-->>D: records
    alt X-User-Id present (detail route only)
        D-->>U: 200, full record fields
    else no header
        D-->>U: 200, public-detail subset (no treatment)
    end
```

`GET /diagnosis` (list) never requires a header. `GET /diagnosis/{id}` changes response shape
depending on whether one is present.

## Advanced search

```mermaid
sequenceDiagram
    actor U as Caller
    participant D as Diagnosis Service
    participant EXT as External Diagnosis API

    U->>D: GET /diagnosis/search?... (X-User-Id required)
    alt no X-User-Id
        D-->>U: 401
    else header present
        D->>D: validate filters (≥1 required, ranges sane)
        alt invalid
            D-->>U: 400
        else valid
            D->>EXT: fetch full dataset
            D->>D: filter in-memory (case-insensitive gender/painType, inclusive ranges)
            D-->>U: 200 + matches (possibly empty)
        end
    end
```

## Treatment analysis

```mermaid
sequenceDiagram
    actor U as Caller
    participant D as Diagnosis Service
    participant EXT as External Diagnosis API

    U->>D: GET /diagnosis/analysis?by=... (X-User-Id required)
    D->>EXT: fetch full dataset
    D->>D: group by age-decade / gender / painType, compute treatment counts + percentages
    D-->>U: 200 + breakdown
```

Always the full dataset — current search filters never apply here.

## Bookmark creation

```mermaid
sequenceDiagram
    actor U as Caller
    participant D as Diagnosis Service
    participant EXT as External Diagnosis API
    participant K as Kafka
    participant B as Bookmark Service

    U->>D: POST /diagnosis/{id}/bookmark (X-User-Id required)
    alt no X-User-Id
        D-->>U: 401
    else header present
        D->>EXT: resolve record by id
        alt not found
            D-->>U: 404
        else found
            D->>D: build snapshot (gender, age, bp, painType, treatment)
            D->>K: publish bookmark.created (blocks for ack)
            alt publish fails
                D-->>U: 503
            else ack received
                D-->>U: 202 "Bookmark request submitted"
                Note over K,B: consumed asynchronously — this route<br/>doesn't wait for Bookmark Service to save it
                K->>B: deliver event
                B->>B: save bookmark
            end
        end
    end
```

Diagnosis Service never learns whether Bookmark Service's save actually succeeded — `202` only
confirms the event reached Kafka. See [Bookmark Service's flows](../bookmark-service/flows.md)
for what happens after.
