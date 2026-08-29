# Key Flows

## Bookmark a record — `POST /bookmarks`

```mermaid
sequenceDiagram
    actor U as Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant B as Bookmark Service
    participant DB as Bookmark DB (MySQL)
    participant R as Redis
    participant D as Diagnosis Service

    U->>FE: clicks "bookmark" on a record
    FE->>GW: POST /bookmarks (with JWT)
    GW->>GW: validate JWT, extract userId
    GW->>B: forward with X-User-Id header

    B->>DB: already bookmarked by this user?

    alt already bookmarked
        DB-->>B: existing row
        B-->>FE: 200 success (no-op, no duplicate)
    else not yet bookmarked
        B->>D: GET /diagnosis/{id} (direct call, via Eureka)
        D-->>B: record data, or 404

        alt record not found
            B-->>FE: error — can't bookmark
        else record found
            B->>DB: save snapshot (gender, age, bp, painType, treatment)
            B->>R: invalidate this user's cached bookmark list
            B-->>FE: 201 + created bookmark
        end
    end
```

1. Registered User clicks "bookmark" on a diagnosis record.
2. Request reaches Bookmark Service via the Gateway (protected route) with `X-User-Id` set.
3. Bookmark Service checks whether this user already bookmarked this record.
   - **Already bookmarked**: returns success without touching anything else — idempotent.
   - **Not yet bookmarked**: calls Diagnosis Service directly to confirm the record exists and
     get its display fields.
     - Not found → error, nothing saved.
     - Found → saves the snapshot, invalidates the cached list, returns the new bookmark.

## View bookmarks — `GET /bookmarks`

```mermaid
sequenceDiagram
    actor U as Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant B as Bookmark Service
    participant R as Redis
    participant DB as Bookmark DB (MySQL)

    U->>FE: opens bookmarks page
    FE->>GW: GET /bookmarks (with JWT)
    GW->>B: forward with X-User-Id header
    B->>R: look up cached list for userId

    alt cache hit
        R-->>B: cached bookmarks
    else cache miss
        B->>DB: query bookmarks for userId
        DB-->>B: bookmarks (snapshot data)
        B->>R: populate cache for userId
    end

    B-->>FE: 200 + bookmarks (or empty array)
```

1. Registered User opens their bookmarks page.
2. Request reaches Bookmark Service via the Gateway with `X-User-Id` set.
3. Bookmark Service checks Redis first.
   - **Hit**: returns the cached list.
   - **Miss**: reads from MySQL, populates the cache, returns the list.
4. Never calls Diagnosis Service — the snapshot data is enough on its own (US-08).
5. Empty list → frontend shows the empty state (US-08), not a special API response.

## Remove a bookmark — `DELETE /bookmarks/{id}`

```mermaid
sequenceDiagram
    actor U as Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant B as Bookmark Service
    participant DB as Bookmark DB (MySQL)
    participant R as Redis

    U->>FE: clicks remove on a bookmark
    FE->>GW: DELETE /bookmarks/{id} (with JWT)
    GW->>B: forward with X-User-Id header
    B->>DB: delete row (scoped to this userId)
    B->>R: invalidate this user's cached bookmark list
    B-->>FE: 200 success
    FE->>U: bookmark disappears from the list immediately
```

1. Registered User removes a bookmark.
2. Request reaches Bookmark Service via the Gateway with `X-User-Id` set.
3. Bookmark Service deletes the row — scoped to the caller's own `userId`, so nobody can delete
   another user's bookmark by guessing an ID.
4. Invalidates the cached list so the next `GET /bookmarks` reflects the removal.
5. Frontend removes it from the displayed list immediately (US-08).
