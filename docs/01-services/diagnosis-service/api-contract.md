# API Contract

Base path: `/diagnosis`, port `8083`. No API Gateway yet — called directly. `X-User-Id` is a
plain header, trusted as-is (no signature check).

| Route | Auth |
|---|---|
| `GET /diagnosis` | Public |
| `GET /diagnosis/{id}` | Optional — response shape changes if present |
| `GET /diagnosis/search` | Requires `X-User-Id` |
| `GET /diagnosis/analysis` | Requires `X-User-Id` |
| `POST /diagnosis/{id}/bookmark` | Requires `X-User-Id` |

Raw field names from the external API: `id`, `gender`, `age` (int), `bp` (int), `cholesterol`
(int), `diabetic` (String), `smoking_status`, `pain_type`, `treatment` (String). Valid genders:
`Male`, `Female`. Valid pain types: `Typical Angina`, `Atypical Angina`, `Non-anginal Pain`
(lowercase "a" — the external dataset's actual casing), `Asymptomatic`. Gender/pain-type matching
is case-insensitive throughout, so client casing doesn't have to match exactly.

## `GET /diagnosis`

Public, no auth. Returns list-view fields only: `id, gender, age, cholesterol, diabetic,
smoking_status, pain_type, treatment` — **`bp` is not included** in list view.

**Success — `200 OK`**: array of records.

**Errors**: `503` if the external API doesn't respond.

## `GET /diagnosis/{id}`

**With `X-User-Id`**: returns the full record (adds `bp` and `treatment` beyond list view).
**Without it**: returns a public-detail subset — `id, gender, age, bp, cholesterol, diabetic,
smoking_status, pain_type` (no `treatment`).

**Errors**: `404` if no record with that id; `503` if the external API doesn't respond.

## `GET /diagnosis/search`

**Requires** `X-User-Id` — `401` if missing.

**Query params** (all optional individually, **≥1 required**): `gender`, `painType`, `ageMin`,
`ageMax`, `bpMin`, `bpMax` (ints).

**Validation — `400`**: no filter given at all; `gender` not a recognized value; `painType` not a
recognized value; `ageMin > ageMax`; `bpMin > bpMax`.

**Success — `200 OK`**: array of full-detail records matching all given filters (range filters
inclusive, gender/painType exact case-insensitive match). Empty array is a normal response.

**Errors**: `503` if the external API doesn't respond.

## `GET /diagnosis/analysis?by=age|gender|painType`

**Requires** `X-User-Id` — `401` if missing. `by` required — `400` if missing/invalid.

Always runs against the entire dataset — current search filters don't apply.

**Success — `200 OK`**

```json
{
  "characteristic": "age",
  "totalRecords": 303,
  "overallTreatmentCounts": { "Medication": 120, "Lifestyle Changes": 90, "...": 0 },
  "overallTreatmentPercentages": { "Medication": 39.6, "...": 0 },
  "breakdown": [
    {
      "value": "40-49",
      "count": 55,
      "treatmentCounts": { "Medication": 30, "...": 0 },
      "treatmentPercentages": { "Medication": 54.5, "...": 0 },
      "dominantTreatment": "Medication"
    }
  ]
}
```

`age` is grouped into decade buckets (`"40-49"`, sorted naturally); `gender`/`painType` groups are
sorted by descending count. Percentages are rounded to 1 decimal.

**Errors**: `503` if the external API doesn't respond.

## `POST /diagnosis/{id}/bookmark`

**Requires** `X-User-Id` — `401` if missing.

Resolves the record, builds a snapshot, and publishes it to Kafka (`bookmark.created`) for
Bookmark Service to consume and store — see [`messaging.md`](./messaging.md). This route itself
has no database of its own; it does not wait for Bookmark Service to actually save anything.

**Success — `202 Accepted`**

```json
{ "message": "Bookmark request submitted", "diagnosisId": "af5b" }
```

**Errors**

| Status | When |
|---|---|
| `404` | No record with that id. |
| `503` | The Kafka publish failed — `{message:"Bookmarking is temporarily unavailable"}`. |

## Error response shape

`{ "status": <int>, "message": "...", "timestamp": "..." }` for every handled exception; `500`
generic message for anything unhandled.
