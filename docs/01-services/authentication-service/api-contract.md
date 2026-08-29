# API Contract

Both endpoints are reached through the API Gateway, not called directly. Per
[ADR-0009](../../00-infrastructure/adr/0009-route-level-authorization-at-gateway.md), both are
**public** routes — no token required to hit them (you can't have a token before you log in).

## `POST /auth/register`

Creates a new user.

**Request**: email, password, and whatever basic profile fields the registration form collects
(exact set TBD when UserProfile Service is grilled — e.g. name).

**Behavior**:
- Rejects the request if the email is already registered (US-01 acceptance criteria).
- Rejects the request if the password doesn't meet the password policy (see
  [`security.md`](./security.md)).
- On success: creates the user record, stores the bcrypt hash, publishes the registration event
  (see [`messaging.md`](./messaging.md)), and returns success. The frontend then sends the user
  to the login page (US-01) — registration does **not** log the user in automatically.

**Response**: success confirmation (no token — registration and login are separate steps).

## `POST /auth/login`

Authenticates an existing user and issues a token.

**Request**: email, password.

**Behavior**:
- Looks up the user by email, checks the password against the stored bcrypt hash.
- On success: returns a JWT (see [`security.md`](./security.md) for contents/expiry).
- On failure — wrong password **or** unknown email — returns the same generic "invalid
  credentials" error either way. Never reveal which field was wrong (US-02 acceptance
  criteria) — a distinct "no such email" error would let someone enumerate registered emails.

**Response**: JWT + its expiry.

## No `/auth/logout` endpoint

Logout is purely client-side — the frontend deletes the token from local storage. There is
nothing for the server to do, so there's no endpoint. See [`security.md`](./security.md) for why.
