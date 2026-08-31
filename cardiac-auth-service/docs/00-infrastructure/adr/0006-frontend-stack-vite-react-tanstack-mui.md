# Frontend: Vite + React + TanStack Query/Router + MUI — no Next.js

The frontend is a pure client-side SPA that talks to the backend only through the API Gateway —
there's no SEO requirement and nothing benefits from server-side rendering, so Next.js's routing
and rendering model would add ceremony without paying for itself here. We're using Vite for fast
dev/build tooling, TanStack Query for data fetching (it directly gives us the loading/error
states the backlog's acceptance criteria call for on every data-fetching story, instead of
hand-rolling that per component), TanStack Router for client-side routing, and Material UI (MUI)
for components — the app is fundamentally table/form/list-driven (diagnosis records, search
filters, bookmarks, profile forms), and MUI has ready components for exactly that instead of
building a filterable data table from scratch.
