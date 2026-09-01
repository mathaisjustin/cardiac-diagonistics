# Messaging

UserProfile is the **consumer** in the one Kafka flow in this system — see Authentication's
[`messaging.md`](../authentication-service/messaging.md) for the publisher side, and
[ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md)
for why registration works this way at all.

## What it consumes

**Event**: the registration event Authentication publishes after a new user is created —
`userId`, `firstName`, `lastName`, `phone`. No `email` — see
[ADR-0013](../../00-infrastructure/adr/0013-email-never-duplicated-into-userprofile.md) for why
UserProfile never stores its own copy; it reads email live from the `X-User-Email` header on
every request instead.

**On receiving it**: creates a new row in the `profiles` table (see
[`data-model.md`](./data-model.md)), keyed on `userId`. This is the **only** way a profile
record ever gets created — there is no API route for it.

**UserProfile never publishes anything back.** No round trip — Authentication doesn't know or
care whether/when this consumption happens.

## Timing: this is why the "smaller race" exists

Authentication already responded "registered successfully" to the client before this consumer
even runs — the user can log in immediately. But their profile record might not exist yet for a
short window, until this service actually processes the event. Per
[ADR-0010](../../00-infrastructure/adr/0010-registration-owned-by-auth-single-direction-kafka.md),
this is the deliberate, smaller tradeoff that replaced the old login race. See
[`flows.md`](./flows.md) for how `GET /profile` handles hitting that window.

## If this service is down when Authentication publishes

Nothing is lost — the event sits in Kafka for up to 7 days (topic retention) until this service
is back up and consumes it. See Authentication's
[`messaging.md`](../authentication-service/messaging.md) for the full reliability story
(including why Authentication waits for a producer ack before responding, per
[ADR-0011](../../00-infrastructure/adr/0011-registration-waits-for-kafka-producer-ack.md)).
