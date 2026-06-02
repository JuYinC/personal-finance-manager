# AGENTS.md — personal-finance-manager AI Navigation Guide

> This file is the primary entry point for GenAI Agents to understand the personal-finance-manager project. Read this file completely before performing any code task.

---

## Project Summary

**personal-finance-manager** is a full-stack web application that helps individual users track income, expenses, budgets, and financial health through a secure REST API and a reactive SPA.

- **Architecture**: Monorepo — Spring Boot 3.2 REST API (`backend/`) + Vue 3 SPA (`frontend/`), orchestrated via Docker Compose.
- **Deployment**: Docker Compose (three services: `postgres`, `backend`, `frontend`). Production HTTPS/TLS is not yet configured.
- **Base package**: `com.finance.manager`
- **Frontend module root**: `finance-manager-frontend` (see `frontend/package.json`)
- **Version source of truth**: `backend/build.gradle` (Spring Boot 3.2, Java 17) · `frontend/package.json` (Vue 3.4, Vite 5.0)

---

## Quick Constraints

1. **At task start**: if the task spans 3+ Controllers/handlers or affects 2+ SPEC documents, suggest using the IMPL Prompt Pack (`prompts/IMPL.md`) before proceeding. **At task end**: assess whether `docs/spec/` needs updating (triggers: new endpoint, request/response schema change, permission change, business rule change, behavioral bugfix). If yes → update SPEC + Changelog in the same changeset. If no → state the reason explicitly.
2. Controllers MUST NOT contain business logic — delegate everything to the Service layer.
3. Services MUST NOT directly import Repository beans from a different domain; cross-domain reads must go through the owning Service.
4. ALL database schema changes MUST be added as a new Liquibase changeset in `db/changelog/changes/` — never use `ddl-auto: create` or `update`.
5. Every protected endpoint MUST be guarded by the JWT filter chain; never bypass `SecurityConfig` with `permitAll()` except for `/api/auth/**` and actuator paths.
6. API routes follow the pattern `/api/{resource}` (plural, kebab-case); nested resource actions use `/api/{resource}/{id}/{action}`.
7. Frontend state that is shared across views MUST live in a Pinia store; local-only state stays inside the component.
8. Append a `### Docs Impact` block at the end of every response containing code changes (see Post-Edit Self-Check below).

---

## Domain-to-Code Map

| Domain | Backend Entity | Controller | Service | DTO sub-package | Frontend Service | Pinia Store |
|---|---|---|---|---|---|---|
| Auth | `User.java` | `AuthController` | `AuthService` | `dto/auth` | `services/auth.js` | `stores/auth.js` |
| User | `User.java` | `UserController` | `UserService` | `dto/user` | `services/user.js` | — |
| Account | `Account.java` | `AccountController` | `AccountService` | `dto/account` | `services/account.js` | `stores/account.js` |
| Category | `Category.java` | `CategoryController` | `CategoryService` | `dto/category` | `services/category.js` | `stores/category.js` |
| Transaction | `Transaction.java` | `TransactionController` | `TransactionService` | `dto/transaction` | `services/transaction.js` | — `[unverified]` |
| Budget | `Budget.java` | `BudgetController` | `BudgetService` | `dto/budget` | `services/budget.js` | — `[unverified]` |
| Report | *(derived)* | `ReportController` | `ReportService` | `dto/report` | `services/report.js` | — |

---

## Code Generation Rules

### Architecture Hard Rules

- **Layer order**: Controller → Service → Repository. Never skip a layer or call downward more than one layer at a time.
- **No business logic in Controllers**: Controllers only validate input, call one service method, and map the result to a response DTO.
- **No cross-domain Repository injection**: `BudgetService` must not autowire `TransactionRepository` — use `TransactionService` instead.
- **Soft delete only for Transactions**: Use the `deleted` flag + balance adjustment; never issue a hard `DELETE` on the transactions table.
- **Entity ↔ DTO boundary**: Entities MUST NOT be exposed in controller responses; always map through a DTO.

### API Routing Conventions

- Base path: `/api/{resource}` (plural, lowercase, kebab-case), e.g., `/api/transactions`, `/api/budget-categories` `[unverified]`.
- ID-scoped: `/api/{resource}/{id}` — GET, PUT, DELETE.
- Query-scoped filtering: use `@RequestParam` (e.g., `?month=6&year=2026`).
- Auth routes: `/api/auth/register`, `/api/auth/login` (public, no JWT required).

### Auth & Security

- All non-auth endpoints require `Authorization: Bearer <jwt>` header validated by `JwtAuthenticationFilter`.
- JWT algorithm: HS256, 24 h expiry. No refresh endpoint exists — token must be re-issued via login.
- Ownership enforcement: Services MUST verify that the authenticated user owns the requested resource before any mutation.
- Passwords are BCrypt-hashed (strength 10); never log or return plaintext passwords.
- CORS allowed origins configured via `CORS_ALLOWED_ORIGINS` env var (see `application.yml`).

### Data Access Rules

- All schema changes via Liquibase only. File naming: `NNN-verb-noun-table.yml` (e.g., `007-add-recurring-flag-to-transactions.yml`).
- JPA `ddl-auto` is set to `validate` — Hibernate will NOT modify the schema.
- Use batch operations where available (`hibernate.jdbc.batch_size = 20` is configured).

### Naming Conventions

| Layer | Pattern | Example |
|---|---|---|
| Java classes | `PascalCase` + role suffix | `TransactionService` |
| Java packages | `lowercase.domain` | `com.finance.manager.service` |
| DB tables | `snake_case`, plural | `transactions` |
| Liquibase files | `NNN-verb-noun-table.yml` | `007-add-field.yml` |
| Frontend views | `PascalCase.vue` | `Transactions.vue` |
| Frontend services/stores | `camelCase.js` | `transaction.js` |
| Env variables | `UPPER_SNAKE_CASE` | `JWT_SECRET` |

---

## GenAI Docs Navigation

| What you want to do | Where to look |
|---|---|
| Understand the project overall | [`README.md`](README.md) |
| Set up the environment (Docker) | `README.md` → Quick Start |
| Configure environment variables | `README.md` → Environment Variables + [`.env.example`](.env.example) |
| Read API contracts (runtime) | `http://localhost:8080/swagger-ui.html` |
| Read API contracts (static) | `README.md` → API Documentation section |
| Understand the DB schema | `README.md` → Database Schema + [`backend/src/main/resources/db/changelog/changes/`](backend/src/main/resources/db/changelog/changes/) |
| App configuration reference | [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml) |
| Infrastructure topology | [`docker-compose.yml`](docker-compose.yml) |
| Docs governance rules | [`docs/README.md`](docs/README.md) |
| SPEC / ADR / SA documents | `docs/spec/` · `docs/adr/` · `docs/analysis/` (pending creation) |

---

## Common Commands

```bash
# === Backend ===

# Build
cd backend && ./gradlew clean build

# Run (local, no Docker)
cd backend && ./gradlew bootRun

# Test
cd backend && ./gradlew test

# Lint / static analysis
# Not configured — recommend adding Checkstyle or SpotBugs

# Type check
# Not applicable (Java — type errors surface at build time via `./gradlew build`)

# === Frontend ===

# Install dependencies
cd frontend && npm install

# Dev server
cd frontend && npm run dev

# Build (production bundle)
cd frontend && npm run build

# Lint
# Not configured — recommend adding ESLint

# Type check
# Not configured (plain JS) — recommend migrating to TypeScript + `vue-tsc`

# === Full Stack ===

# Start all services (Docker)
docker-compose up --build

# Stop all services
docker-compose down

# Rebuild a single service
docker-compose up --build backend
```

---

## Docs Maintenance Reminders

- Update **SPEC** on every new endpoint, request/response shape change, permission change, or behavioral bugfix.
- Propose an **ADR** for any either/or architecture decision (e.g., adding a refresh token flow, switching auth scheme, introducing a message queue).
- Propose an **SA** when cross-module boundaries shift or team onboarding gaps appear — wait for human confirmation before drafting.
- Update **INFRA** when deployment topology changes (new services, CI/CD pipeline, HTTPS setup).
- Docs governance rules: see [`docs/README.md`](docs/README.md).

---

## Post-Edit Self-Check

Before declaring work complete:
1. List changed files from the current diff.
2. Cross-reference every changed file with the Domain-to-Code Map and GenAI Docs Navigation above.
3. For each candidate doc, state `Update needed` or `No update needed` with a reason.
4. If interface, schema, permission, or business rules changed, update the relevant SPEC.
5. Run the applicable build/test command to confirm no regressions.

**Forcing Function**: AI agents MUST append a `### Docs Impact` block at the end of any response containing code changes:

### Docs Impact
- **SPEC**: [Updated SPEC-00x § ... / No update — reason]
- **ADR**: [Proposing ADR-00x for ... / No decision point]
- **SA**: [No structural change / Proposing SA scope: ...]
- **INFRA**: [Updated INFRA-001 / No infra change]
