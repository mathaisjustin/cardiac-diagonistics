# Bookmark Service stores a snapshot of a diagnosis record, not just its ID

When a user bookmarks a diagnosis record, Bookmark Service could store either a thin reference
(just the record's ID, re-fetched from Diagnosis Service on every view) or a snapshot (a copy of
the display fields — gender, age, bp, pain type, treatment — taken at bookmark time).

We're storing a **snapshot**. This isn't the same tradeoff as
[ADR-0013](./0013-email-never-duplicated-into-userprofile.md) (email is read live from a header
that arrives free on every request) — here, reading live would mean Bookmark Service calling
Diagnosis Service, which calls the external API, on **every single view** of a page that should
just work: viewing your own saved bookmarks (US-08). A thin reference would make that core,
frequently-used feature dependent on Diagnosis Service (and the external API behind it) being up.

A snapshot avoids that dependency entirely, at the cost of the bookmarked data being able to
drift from the live record — an acceptable tradeoff since the underlying dataset is static/seeded
(see [ADR-0003](./0003-diagnosis-service-stateless-no-db.md)), not something that actually
changes in practice. Diagnosis Service still stays live and un-cached for browsing, searching,
and analysis, where reflecting the current dataset is the entire point — this only applies to
what a user has explicitly chosen to save.
