---
status: accepted
supersedes: ADR-0001, ADR-0004
---

# Registration is owned by Authentication Service; UserProfile only consumes

ADR-0001's flow had UserProfile publish credentials to Kafka, Authentication consume them and
generate a user ID, then publish that ID back for UserProfile to consume — both services ended up
as both producer and consumer of the same exchange, which duplicated messaging setup in both
services for no real benefit.

We're flattening this to one direction: the client submits registration directly to
**Authentication Service**. Auth validates it (duplicate email, password strength — checked
against its own credentials table), creates the user, generates the user ID, stores the
credential, and responds to the client — all synchronously, before anything touches Kafka. Auth
then publishes one event (user ID + profile fields) to Kafka. **UserProfile Service only
consumes** that event and creates the profile record; it never publishes anything back.

Two effects worth calling out:

- **Auth is publisher-only, UserProfile is consumer-only** for this flow — no more round trip.
- **This removes the race ADR-0004 was written for.** Since Auth now stores the credential
  synchronously before responding "registered," a user can never hit "invalid credentials" from
  registering moments earlier — that failure mode is gone. A smaller race takes its place:
  someone could view "my profile" before UserProfile has consumed the event and created the
  record. That's a lighter problem (a brief empty/loading state, not a failed login) and will be
  handled in UserProfile Service's own doc rather than needing its own ADR.
