# Repo Folder Structure

This is what the repo will look like once code exists — documented here rather than as actual
empty folders in the repo, so the repo root only holds real, working content plus `docs/`.

## Root layout (target)

```
cardiac-diagonistics/
├── docker-compose.yml        # brings up all 7 built services + Kafka/Redis/MySQL, one command
├── docs/                     # this documentation
│
├── frontend/                 # React (Vite + TanStack Query/Router + MUI)
├── eureka-discovery/         # Spring Boot — service discovery
├── api-gateway/              # Spring Boot (Spring Cloud Gateway) — routing, JWT, route auth
├── authentication-service/   # Spring Boot — registration, login, JWT issuance
├── user-profile-service/     # Spring Boot — profile data
├── diagnosis-service/        # Spring Boot — stateless, proxies the external Diagnosis API
└── bookmark-service/         # Spring Boot — bookmarked records, Redis-backed
```

Seven folders we build ourselves — one per custom service, including the frontend. Kafka, Redis,
and MySQL are off-the-shelf images referenced in `docker-compose.yml`, not folders here.

Each service folder will hold its own Spring Boot (or Vite, for `frontend/`) project once code
starts — build files, source, its own `Dockerfile`. None of that exists yet; see each service's
doc under [`01-services/`](./01-services/) (and [`00-infrastructure/`](./00-infrastructure/) for
`eureka-discovery/` and `api-gateway/`) for what's actually planned per service.

## Why documented, not scaffolded

Creating the empty folders for real, before any code exists, made the repo root look like a
half-built project and buried `docs/` among a pile of near-empty directories. Keeping this as a
doc means the repo root stays exactly what it is right now — documentation only — until a
service actually has code to put in its folder.
