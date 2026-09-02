# Key Flows

## Profile creation (via Kafka, not an API call)

```mermaid
sequenceDiagram
    participant K as Kafka (user.registered)
    participant UP as UserProfile Service
    participant DB as profiles_db (MySQL)

    K->>UP: deliver event (userId, email, firstName, lastName, contactNumber, department)
    UP->>DB: existsById(userId)?
    alt already exists
        UP->>UP: log + skip (idempotent)
    else new
        UP->>DB: insert profile row (userId, firstName, lastName, contact, department)
    end
```

This is the only way a `profiles` row ever comes to exist. It happens independently of, and
slightly after, the client being told "registered successfully" by Authentication.

## View profile — `GET /profile`

```mermaid
sequenceDiagram
    actor U as Caller
    participant UP as UserProfile Service
    participant DB as profiles_db (MySQL)

    U->>UP: GET /profile (X-User-Id, X-User-Email headers)
    UP->>DB: find by userId
    alt found
        UP-->>U: 200 {email from header, rest from DB row}
    else not found
        UP-->>U: 404
    end
```

The 404 case is expected to be transient: it only happens in the short window between a
successful login and this service having consumed the `user.registered` event for that user.

## Update profile — `PUT /profile`

```mermaid
sequenceDiagram
    actor U as Caller
    participant UP as UserProfile Service
    participant DB as profiles_db (MySQL)

    U->>UP: PUT /profile (X-User-Id header, body)
    UP->>UP: validate fields (@NotBlank)
    alt validation fails
        UP-->>U: 400
    else profile row doesn't exist
        UP-->>U: 404
    else valid + exists
        UP->>DB: update row for userId
        UP-->>U: 200 + updated profile
    end
```
