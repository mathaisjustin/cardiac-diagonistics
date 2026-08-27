# Diagnosis Service stays stateless — no local database

We considered mirroring the external Diagnosis API's data into a local database so search and
treatment-analysis wouldn't repeatedly hit that API. We're rejecting that: the external API runs
as a local Docker container on the same network, not a rate-limited or slow third party, so
calling it directly on every search/analysis request is cheap and avoids the sync-staleness
problem a local copy would introduce. Diagnosis Service fetches live from the external API
(filtered search via its query params where possible, full-dataset fetch + in-memory
aggregation for analysis) and persists nothing of its own. This matches the case study's
original architecture diagram, which never gave Diagnosis a database. Revisit only if the
external source stops being a cheap local call (e.g. becomes a real third-party API).
