# RoadGuard Admin Web

React + TypeScript admin console that uses the current Spring Boot endpoints. It supports sign-in, analytics, report search/filtering, map display, report review/status changes, and contractor directory/create flows.

## Run

1. Install Node.js LTS.
2. Copy `.env.example` to `.env.local` and set `VITE_API_BASE_URL=http://localhost:8080/roadguard/v1` for a local backend.
3. Run `npm install`, then `npm run dev`.

The backend must allow the Vite origin in CORS (typically `http://localhost:5173`). Configure this in Spring properties/code when you choose to; this frontend does not change backend files.

## Backend contract limits

- `/admin/contractors` currently returns contractor fields without the contractor ID, while assignment requires `contractorId`. The UI exposes ID-based assignment explicitly rather than guessing IDs.
- No user-management or database-manager API/role exists in the current backend. The UI never displays report submitter identity; only report details returned by the existing DTO are shown.
- `ADMIN` and `AUTHORITY` are the roles supported by the current backend admin endpoints. Database-manager-only identity lookup must wait for a backend role and endpoint.

