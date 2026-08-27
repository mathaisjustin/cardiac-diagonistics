# Cardiac Diagnosis System

## What this is

The Cardiac Diagnosis System is a microservices-based application for managing and analyzing
cardiac diagnosis data. It gives healthcare professionals a way to browse patient diagnosis
records, search and filter them, and see how treatment recommendations relate to patient
characteristics — supporting better, faster decision-making.

The system pulls diagnosis data from an external Diagnosis API, lets users save records they
care about (bookmarking), and gives every user a personal account and profile.

## Who uses it

- **Registered Users** — full access: browse and search diagnosis data, bookmark records,
  manage their bookmarks, and update their own profile.
- **Guest Users** — can browse and search diagnosis data without creating an account, but are
  prompted to register or log in for anything personal (bookmarking, profile).

## What it does (high-level features)

| Feature | Who | Summary |
|---|---|---|
| Registration & Login | Guest → Registered | Create an account, log in/out securely, reset a forgotten password. |
| View Diagnosis Data | Everyone | Browse records pulled from the external Diagnosis API. |
| Advanced Search | Everyone | Filter records by pain type, age, blood pressure, and gender — individually or combined. |
| Treatment Analysis | Registered | See how treatment recommendations break down by patient characteristics (age, gender, pain type). |
| Bookmarking | Registered | Save a record for later, view all saved records, remove ones no longer needed. |
| Profile Management | Registered | View and update personal details. |

See [`05-backlog/product-backlog.md`](./05-backlog/product-backlog.md) for the full set of user
stories and acceptance criteria behind these features.

## How it's built (tech stack, at a glance)

| Layer | Choice | Why |
|---|---|---|
| Frontend | Vite + React + TanStack Query/Router + MUI | Pure SPA behind the Gateway — no SSR needed; MUI gives ready table/form components for a data-heavy app. |
| Backend services | Java + Spring Boot, built with Maven | One service per business responsibility (see below). |
| Database | MySQL (one instance per service) | One consistent engine across UserProfile, Authentication, and Bookmark. |
| Service discovery | Eureka | Services find each other by name instead of hardcoded addresses. |
| Edge/routing | Spring Cloud Gateway | Single entry point for the frontend; validates JWTs and enforces which routes need auth, routes to the right service. |
| Async messaging | Kafka | Decouples services that need to react to events (e.g. a new registration) without calling each other directly. |
| Caching | Redis | Speeds up frequently-read data (e.g. bookmarks). |
| Auth | JWT (jjwt), validated centrally at the Gateway | Stateless, signed tokens issued at login; the Gateway checks them once instead of every service repeating that logic. |
| Deployment | Docker Compose | One command brings up every service together for local/dev use. |

Full reasoning behind these choices lives in [`ARCHITECTURE.md`](./ARCHITECTURE.md) and the ADRs
under [`00-infrastructure/adr/`](./00-infrastructure/adr/).

## Map of the docs

This repo is documentation-only. Each section below is being written bottom-up — foundational
pieces first, so later docs can build on ones already agreed. Links go live as each doc lands.

- **[ARCHITECTURE.md](./ARCHITECTURE.md)** — the whole-system picture: client → API Gateway →
  services → databases, with a diagram. Start here before any individual doc.
- **[00-infrastructure](./00-infrastructure/README.md)** — build/run notes for each shared
  infrastructure piece: Eureka, API Gateway, Kafka, Redis.
- **01-services** — one folder per business service: User Profile, Authentication, Diagnosis,
  Bookmark.
- **02-frontend** — the React app.
- **03-cross-cutting** — concerns that touch every service: security/JWT, logging, testing
  strategy.
- **04-deployment** — the Docker Compose setup that brings every service up with one command.
- **[05-backlog/product-backlog.md](./05-backlog/product-backlog.md)** — the user stories and
  acceptance criteria this system is being built against.

## Status

🚧 Living document. This README will be filled in and re-linked as each section below is
drafted and reviewed.
