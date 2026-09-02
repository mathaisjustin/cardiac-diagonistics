# Route-level authorization is enforced centrally at the API Gateway

Following ADR-0002/0005 (the Gateway validates JWTs statelessly), we're going further: the
Gateway also decides *which* routes require a valid token at all, not just whether a token is
valid when one is present. The Gateway holds the public/protected route map (e.g. viewing and
searching diagnosis data is public for Guest Users; bookmarking, bookmark management, and profile
routes require a valid token) and rejects an unauthenticated request to a protected route before
it ever reaches a downstream service. This means individual services don't each need to
re-implement "is this route protected, and is the caller allowed to hit it" — they can trust that
anything reaching them already passed that check, avoiding repeated auth-guard code across
UserProfile, Diagnosis, and Bookmark.
