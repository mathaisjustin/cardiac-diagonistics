# JWT validation happens once, at the API Gateway, not in every service

Every protected route in every service needs to check the caller is authenticated. Rather than
duplicating token-validation logic (and the shared secret/public key) into UserProfile,
Diagnosis, and Bookmark individually, we validate the JWT once in a Gateway filter. A request
that reaches a downstream service has already passed that check. Services trust the identity
claims the Gateway forwards and don't re-validate the token themselves. This keeps auth logic in
one place and means each service doesn't need network access to Authentication just to check a
token.
