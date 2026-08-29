# Key Flows

## Profile creation (via Kafka, not an API call)

```mermaid
sequenceDiagram
    participant K as Kafka
    participant UP as UserProfile Service
    participant DB as UserProfile DB (MySQL)

    Note over K: event published by Authentication<br/>after registration (see Authentication's messaging.md)
    K->>UP: deliver event (userId, firstName, lastName, phone)
    UP->>DB: create profile row, keyed on userId
```

This is the only way a `profiles` row ever comes to exist — see
[`messaging.md`](./messaging.md). It happens independently of, and slightly after, the user
being told "registered successfully" by Authentication.

## View profile — `GET /profile`

```mermaid
sequenceDiagram
    actor U as Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant UP as UserProfile Service
    participant DB as UserProfile DB (MySQL)

    U->>FE: opens profile page
    FE->>GW: GET /profile (with JWT)
    GW->>GW: validate JWT, extract userId + email
    GW->>UP: forward with X-User-Id, X-User-Email headers
    UP->>DB: look up profile by userId

    alt profile exists
        DB-->>UP: profile row (firstName, lastName, phone)
        UP-->>FE: 200 + profile data<br/>(email from header + DB fields)
    else profile not found yet
        Note over UP: registration happened, but the Kafka<br/>consumer hasn't processed it yet
        UP-->>FE: 404 (profile not ready)
        FE->>U: "setting up your profile…" + retry shortly
    end
```

1. Registered User opens their profile page.
2. Request reaches UserProfile Service via the Gateway (protected route) with `X-User-Id` and
   `X-User-Email` set.
3. UserProfile looks up the profile row by that ID.
4. **Normal case**: found → returns it, combining `email` straight from the header with
   `firstName`/`lastName`/`phone` from the database row (see [`data-model.md`](./data-model.md)
   and [ADR-0013](../../00-infrastructure/adr/0013-email-never-duplicated-into-userprofile.md)).
5. **Edge case — just registered**: a valid JWT only proves the user successfully logged in, not
   that their profile row exists yet (per the timing gap noted in
   [`messaging.md`](./messaging.md)). If nothing is found, UserProfile returns 404 rather than a
   generic server error, and the frontend treats this specific case as "still being set up" —
   showing a friendly loading state and retrying shortly — instead of an alarming error message.
   This should resolve within moments in practice; it's not expected to be a real 404 in any
   other case, since every user eventually gets a profile row.

## Update profile — `PUT /profile`

```mermaid
sequenceDiagram
    actor U as Registered User
    participant FE as Frontend
    participant GW as API Gateway
    participant UP as UserProfile Service
    participant DB as UserProfile DB (MySQL)

    U->>FE: edits first/last name or phone, saves
    FE->>GW: PUT /profile (with JWT + changed fields)
    GW->>GW: validate JWT, extract userId
    GW->>UP: forward with X-User-Id header
    UP->>UP: validate fields present/well-formed

    alt validation fails
        UP-->>FE: 400 error
    else validation passes
        UP->>DB: update profile row for userId
        UP-->>FE: 200 + updated profile
        FE->>U: confirmation message
    end
```

1. Registered User edits their name or phone number and saves.
2. Request reaches UserProfile Service via the Gateway (protected route) with `X-User-Id` set.
3. UserProfile validates the submitted fields (US-09 acceptance criteria).
   - Invalid → error returned, nothing saved.
4. Valid → updates the row for that `userId`.
5. Returns the updated profile; the frontend shows a confirmation message (US-09).
