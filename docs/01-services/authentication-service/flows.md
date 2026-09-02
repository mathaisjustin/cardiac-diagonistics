# Key Flows

## Registration

```mermaid
sequenceDiagram
    actor U as Guest User
    participant Auth as Authentication Service
    participant DB as Auth DB (MySQL)
    participant K as Kafka

    U->>Auth: POST /api/auth/register
    Auth->>Auth: validate fields (all @NotBlank)
    alt validation fails
        Auth-->>U: 400
    else email already registered
        Auth-->>U: 409
    else valid
        Auth->>Auth: hash password (BCrypt)
        Auth->>DB: save user (email, password_hash, generated UUID id)
        Auth->>K: publish user.registered (blocks for producer ack)
        alt no ack
            Auth->>DB: roll back transaction (user row undone)
            Auth-->>U: 503
        else ack received
            Auth-->>U: 201
        end
    end
```

UserProfile Service consumes `user.registered` independently and asynchronously afterward — see
[`messaging.md`](./messaging.md) and [UserProfile's flows](../user-profile-service/flows.md).

## Login

```mermaid
sequenceDiagram
    actor U as Registered User
    participant Auth as Authentication Service
    participant DB as Auth DB (MySQL)

    U->>Auth: POST /api/auth/login {email, password}
    Auth->>DB: look up user by email
    Auth->>Auth: check password against BCrypt hash
    alt no match
        Auth-->>U: 401 "Invalid email or password"
    else match
        Auth->>DB: delete any existing refresh token row for this user
        Auth->>DB: create new refresh token row (hashed, 7-day expiry)
        Auth->>Auth: issue access token (JWT, sub=userId, 15min)
        Auth-->>U: 200 {accessToken, refreshToken}
    end
```

## Refresh

```mermaid
sequenceDiagram
    actor U as Client
    participant Auth as Authentication Service
    participant DB as Auth DB (MySQL)

    U->>Auth: POST /api/auth/refresh {refreshToken}
    Auth->>DB: look up by hash(refreshToken)
    alt not found / revoked / expired
        Auth-->>U: 401
    else valid
        Auth->>DB: delete old row, create new row (rotated)
        Auth->>Auth: issue new access token
        Auth-->>U: 200 {accessToken, refreshToken}
    end
```

## Logout

```mermaid
sequenceDiagram
    actor U as Client
    participant Auth as Authentication Service
    participant DB as Auth DB (MySQL)

    U->>Auth: POST /api/auth/logout {refreshToken}
    Auth->>DB: look up by hash(refreshToken)
    alt not found
        Auth-->>U: 401
    else found
        Auth->>DB: set revoked = true
        Auth-->>U: 204
    end
```

The access token already issued keeps working until its own 15-minute expiry — logout only
prevents it from being refreshed again.

## Change password

```mermaid
sequenceDiagram
    actor U as Registered User
    participant Auth as Authentication Service
    participant DB as Auth DB (MySQL)

    U->>Auth: POST /api/auth/change-password<br/>Authorization: Bearer <accessToken><br/>{oldPassword, newPassword}
    Auth->>Auth: validate access token (JwtAuthenticationFilter)
    alt no/invalid token
        Auth-->>U: 401
    else token valid
        Auth->>DB: look up user by sub claim
        Auth->>Auth: check oldPassword against stored hash
        alt mismatch
            Auth-->>U: 400 "Old password is incorrect"
        else match
            Auth->>DB: update password_hash
            Auth->>DB: revoke existing refresh token
            Auth-->>U: 200 "Password changed successfully"
        end
    end
```
