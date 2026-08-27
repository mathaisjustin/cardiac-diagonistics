# Gateway validates JWTs statelessly — no per-request call to Authentication

Following ADR-0002 (JWT validation is centralized at the Gateway), the Gateway checks a token's
signature and expiry itself using a key shared with Authentication Service (Authentication signs
tokens, Gateway verifies them), rather than calling Authentication to check each token. This
means no network hop to Authentication on every request, and existing sessions keep working even
if Authentication is temporarily down — only login and new-token issuance depend on it being up.
