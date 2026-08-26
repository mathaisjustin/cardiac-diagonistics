# Registration confirms on Kafka publish-ack; a too-early login just gets a generic error

Following ADR-0001 (registration is async via Kafka), UserProfile Service marks registration
"successful" as soon as its Kafka publish is acknowledged — it does not wait for Authentication
to actually consume and store the credential. This leaves a small window where a user could
attempt to log in before Authentication has processed the event. We're not building special
handling for that window: Authentication returns the same generic "invalid credentials" error it
would for any bad login, rather than a distinct "account still processing" message. The window is
normally milliseconds, and a distinct error would leak whether an account exists.
