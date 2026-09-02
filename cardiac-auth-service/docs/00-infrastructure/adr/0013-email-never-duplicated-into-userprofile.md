# Email is never duplicated into UserProfile — read live from the Gateway header instead

We initially had Authentication publish `email` into the registration Kafka event so UserProfile
could store its own copy for display. That's unnecessary: per
[ADR-0012](./0012-gateway-forwards-identity-via-headers.md), the Gateway already decodes the JWT
and forwards `X-User-Email` on *every* authenticated request UserProfile receives — including
every `GET /profile` call. There's no moment UserProfile needs to show an email without also
having that header available.

Storing a second copy would only create a value that could drift from Authentication's (the
actual source of truth) with no mechanism to keep it in sync — Authentication has no reason to
ever notify UserProfile of an email change, since UserProfile was never supposed to care. Reading
it live from the header instead means there's exactly one copy of `email` in the whole system,
in Authentication's database, with UserProfile never storing it at all.

The registration Kafka event now carries only `userId`, `firstName`, `lastName`, `phone` — see
Authentication's [`messaging.md`](../../01-services/authentication-service/messaging.md).
