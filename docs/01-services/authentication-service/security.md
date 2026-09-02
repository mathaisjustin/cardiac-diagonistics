# Security

## Password hashing

Passwords are hashed with **BCrypt** via Spring Security's `PasswordEncoder`. Plaintext exists
only for the instant it's being checked (login) or hashed (registration) — never stored or
logged. There is currently **no enforced password policy** (no minimum length, no character-class
requirement) beyond `@NotBlank`.

## Access token (JWT)

| Claim | Value |
|---|---|
| `sub` | The user's `user_id` (UUID string) — **not** the email. |
| `email` | The user's email, as a custom claim. |
| `iat` / `exp` | Issued-at / expiry. |

Signed with HMAC (`Keys.hmacShaKeyFor(JWT_SECRET.getBytes())`, jjwt 0.13.0 — the exact HS
algorithm is selected automatically from the secret's byte length). **Expiry: 15 minutes**
(`jwt.access-token-expiration = 900000` ms).

## Refresh token

**Not a JWT** — an opaque random string (32 random bytes, base64url, no padding). Only its
SHA-256 hash is stored server-side (see [`data-model.md`](./data-model.md)). **Expiry: 7 days**
(`jwt.refresh-token-expiration = 604800000` ms). Each user has at most one active refresh token
row — a new login or refresh deletes the previous row. `POST /api/auth/refresh` rotates it: the
old token is deleted and consumed, a new access + refresh token pair is issued.

## Validation filter

`JwtAuthenticationFilter` runs once per request, reads `Authorization: Bearer <token>`, and if the
signature and expiry check out, sets the authenticated principal to the token's `sub` (userId) —
no authorities/roles attached, since this system has no role distinction. An invalid/missing token
doesn't get rejected by the filter itself; it just leaves no authentication set, and
`anyRequest().authenticated()` (Spring Security) is what actually rejects the request, returning
`401 {"message":"Authentication required"}` via `JwtAuthenticationEntryPoint`.

Security config: CSRF disabled, session policy **STATELESS**. Public (`permitAll`): `/register`,
`/login`, `/refresh`, `/logout`, `/error`. Everything else (in practice, just `change-password`)
requires a valid access token.

## Logout

**Server-side revocation**, not purely client-side: `POST /api/auth/logout` marks the caller's
refresh token row `revoked = true`. The **access token itself is not revoked** — it's a stateless
JWT and remains cryptographically valid until its natural 15-minute expiry, but it can no longer
be refreshed once the refresh token is revoked, so the session dies within 15 minutes at most.

## Out of scope for this phase

Password reset — not built.
