# Messaging

UserProfile is a **consumer only** — it never publishes.

## What it consumes

**Topic**: `user.registered` · **Consumer group**: `user-profile-service`

**Payload** (`UserProfileEvent`, deserialized from a raw JSON string via Jackson):

```json
{
  "userId": "6f1a2b3c-...",
  "email": "jane@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "contactNumber": "555-0100",
  "department": "Cardiology"
}
```

The event does carry `email`, but this consumer never persists it — see
[ADR-0013](../../00-infrastructure/adr/0013-email-never-duplicated-into-userprofile.md).

**On receiving it**: checks `profiles.existsById(userId)` first — **idempotent by userId**. If a
row already exists, the event is logged and skipped (not re-saved/updated). If new, creates a
`profiles` row (`userId`, `firstName`, `lastName`, `contact` ← `contactNumber`, `department`).

**Error handling**: a message that fails to parse throws `InvalidUserProfileEventException`,
registered as non-retryable — it fails fast, no reprocessing. A DB save failure is retried twice
(1s backoff) by `DefaultErrorHandler`, then gives up and logs — there's no dead-letter topic.

UserProfile never publishes anything back — no round trip.

## Timing

Authentication already responds to the client before this consumer runs. There's a short window
where a JWT is valid (login succeeded) but no profile row exists yet. `GET /profile` returns
`404` in that window — see [`flows.md`](./flows.md).

## If this service is down when Authentication publishes

Nothing is lost — the message sits in the topic (Kafka's default retention) until this service is
back up and consumes it.
