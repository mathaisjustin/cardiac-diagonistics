# Key Flows

## Bookmark creation (via Kafka, not an API call)

```mermaid
sequenceDiagram
    participant K as Kafka (bookmark.created)
    participant B as Bookmark Service
    participant DB as bookmark_db (MongoDB)
    participant R as Redis

    K->>B: deliver event (userId, diagnosisId, payload snapshot)
    B->>DB: findByUserIdAndDiagnosisId
    alt already exists
        B->>B: log + skip (idempotent)
    else new
        B->>DB: insert bookmark document
        B->>R: evict bookmarks:<userId>
    end
```

This is the only way a bookmark ever comes to exist — see [`messaging.md`](./messaging.md) and
[Diagnosis Service's flows](../diagnosis-service/flows.md) for what triggers the event.

## View bookmarks — `GET /bookmarks`

```mermaid
sequenceDiagram
    actor U as Caller
    participant B as Bookmark Service
    participant R as Redis
    participant DB as bookmark_db (MongoDB)

    U->>B: GET /bookmarks (X-User-Id header)
    B->>R: get bookmarks:<userId>
    alt cache hit
        R-->>B: cached list
    else cache miss
        B->>DB: find all by userId
        DB-->>B: bookmarks
        B->>R: set bookmarks:<userId> (5min TTL)
    end
    B-->>U: 200 + bookmarks (or empty array)
```

Never calls Diagnosis Service — the snapshot data is enough on its own.

## Remove a bookmark — `DELETE /bookmarks/{id}`

```mermaid
sequenceDiagram
    actor U as Caller
    participant B as Bookmark Service
    participant DB as bookmark_db (MongoDB)
    participant R as Redis

    U->>B: DELETE /bookmarks/{id} (X-User-Id header)
    B->>DB: find by (id, userId)
    alt no match (wrong id, or belongs to someone else)
        B-->>U: 404
    else match
        B->>DB: delete document
        B->>R: evict bookmarks:<userId>
        B-->>U: 200
    end
```

A cross-user delete attempt (right id, wrong `userId` header) returns the same `404` as a
non-existent id — a caller can't use the response to probe whether an id belongs to someone else.
