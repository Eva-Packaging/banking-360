# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Repository Is

Banking 360 is a **project scaffold and documentation repository** for a 2-week junior developer capstone. It does not yet contain runnable source code. The docs define the architecture, backlog, and standards that developers implement from scratch. Claude's primary role here is backlog generation, documentation maintenance, and workflow automation — not running builds or tests.

## Project Key

**BF3** — used in Jira ticket numbers, branch names, and PR titles.

## Branch and PR Conventions

Branch names: `BF3-<number>/<short-description>`  
PR titles: must contain `BF3-<number>` anywhere, case-insensitive (enforced by `.github/workflows/pr-checks.yml`).

Valid PR title examples:
- `BF3-42 Implement customer registration API`
- `feat: BF3-42 customer registration`

## Architecture

Five Spring Boot microservices behind a Spring Cloud API Gateway, with a React frontend:

```
React Frontend (Vite + TypeScript)
    ↓
api-gateway          — JWT validation, CORS, routing to services
    ↓
user-service         — registration, login, JWT generation, roles
account-service      — accounts, balances, Redis summary cache
transaction-service  — transfers, transaction history, Kafka producer
notification-service — Kafka consumer, in-app notifications
    ↓
PostgreSQL (one DB per service)  |  Redis  |  Kafka (topic: transaction-events)
```

Gateway routes:
- `/api/auth/**` and `/api/users/**` → user-service
- `/api/accounts/**` → account-service
- `/api/transactions/**` → transaction-service
- `/api/notifications/**` → notification-service

JWT is validated at the gateway. Authenticated user context is forwarded to downstream services as `X-User-Id` and `X-User-Role` headers. Internal service-to-service routes (e.g. `PATCH /internal/accounts/{id}/balance`) are not exposed at the gateway.

## Backend Package Structure

Each Spring Boot service follows:
```
com.bank.<servicename>
  controller  — HTTP handlers, input validation, response mapping
  service     — business logic, validation rules, Kafka publishing
  repository  — Spring Data JPA interfaces
  entity      — JPA entities
  dto         — request/response objects (never expose entities directly)
  mapper      — entity ↔ DTO conversion
  exception   — custom exceptions + @RestControllerAdvice global handler
  config      — Security, Redis, Kafka, OpenFeign config
  client      — OpenFeign clients for inter-service calls
  cache       — Redis read/write/eviction helpers
```

Standard error response shape (all services):
```json
{ "status": 400, "error": "BAD_REQUEST", "message": "...", "path": "...", "timestamp": "..." }
```

## Frontend Structure

```
frontend/src/
  api/           — Axios clients per service (axiosClient.ts attaches JWT from localStorage)
  components/    — shared UI components
  features/      — one folder per domain (auth, dashboard, accounts, transactions, notifications)
  layouts/       — page shells
  routes/        — AppRouter, ProtectedRoute, AdminRoute
  types/         — shared TypeScript types
  utils/         — helpers
```

Forms use React Hook Form + Zod. API calls go through the shared Axios client (base URL: `http://localhost:8080/api`).

## Key Business Rules

- Transfers require: amount > 0, from ≠ to account, both accounts belong to the requesting customer, both ACTIVE, sufficient balance.
- On successful transfer: persist transaction + 2 entries (DEBIT/CREDIT), update balances via internal Account Service endpoint, publish `TRANSFER_COMPLETED` event to Kafka, evict Redis cache key `customer:{customerId}:account-summary`.
- Notification Service checks `existsByEventId` before inserting — idempotency guard against duplicate Kafka delivery.

## Backlog Generation

The repo uses a structured backlog generator. When asked to create Jira-compatible JSON:

1. Follow the spec in `.claude/backlog-generator.md` exactly.
2. Use the Story Creation Schema (not Project or Epic) when creating a single story.
3. Output files go to `docs/backlog/<filename>.json`.
4. In description fields: wrap file paths with `<path>`, wrap code blocks with plain triple backticks (no language tag — Jira upload requirement).

## Documentation Layout

| File | Purpose |
|------|---------|
| `README.md` | Full architecture, API reference, DB schema, tech stack |
| `docs/ROADMAP.md` | 10-day team implementation plan and milestones |
| `docs/DESIGN-SPEC.md` | Detailed design decisions |
| `docs/DATABASE-SCHEMA.md` | All table definitions |
| `docs/API-REFERENCE.md` | Full API contract |
| `docs/DEPLOYMENT.md` | Docker, Jenkins, AWS, Terraform setup |
| `docs/backlog/project.json` | Full Jira backlog (epics → stories → tasks) |
| `.claude/backlog-generator.md` | Schema spec and rules for generating backlog JSON |
| `.github/PULL_REQUEST_TEMPLATE.md` | PR checklist and section template |