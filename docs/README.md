# personal-finance-manager — Docs Governance Hub

> This file defines the layering rules, naming conventions, and maintenance triggers for project documentation.
> GenAI Agents MUST read this file before documentation tasks, and when `AGENTS.md` Docs Impact checks point here.

---

## SDD Document Classification

| Category | Directory | Naming Format | Trigger |
| --- | --- | --- | --- |
| SA (System Analysis) | `docs/analysis/` | `SA-{3-digit}_{desc}.md` | Milestone or major architecture change |
| ADR (Architecture Decision) | `docs/adr/` | `ADR-{3-digit}_{desc}.md` | Cross-module either/or tech decision |
| SPEC (Interface Contract) | `docs/spec/` | `SPEC-{3-digit}_{desc}.md` | API addition or behavior change (**mandatory**) |
| INFRA (Infrastructure) | `docs/infra/` | `INFRA-{3-digit}_{desc}.md` | Deployment topology or CI change |

- Naming regex: `^(SA|ADR|SPEC|INFRA)-\d{3}_[a-z0-9-]+\.md$`

---

## Source of Truth

SPEC is the primary reference for development and GenAI. Update SPEC directly on every interface addition or change, and track changes in the Changelog section of each SPEC.

**Minimum maintenance rule**: Every PR involving interface or behavior changes MUST update the SPEC content and its Changelog.

---

## ADR Trigger Conditions

- ✅ Needs ADR: auth scheme design (e.g., adding a refresh token flow, switching to OAuth2), either/or technology decisions (e.g., message queue selection, caching layer), cross-module shared pattern introduction, any decision that would be expensive to reverse.
- ❌ No ADR needed: adding a CRUD endpoint, changing a query parameter name, simple bug fixes, CSS/UI-only changes.

---

## SA Trigger Conditions

> SA is a system-level snapshot. Unlike SPEC (mandatory on every interface change), SA is created on-demand when global understanding is lacking.

- ✅ Suggest SA: entering this codebase without an architecture overview, cross-module boundaries shifting (3+ modules restructured), team onboarding with repeated "how does this work?" questions, major feature additions spanning multiple domains.
- ❌ No SA needed: routine feature work within a single domain, adding endpoints, bug fixes.
- ⚠️ AI action: **Propose** the SA topic and scope — do NOT silently generate. Wait for human confirmation before using the SA Prompt Pack.

---

## AI Auto-Trigger Heuristics (Zero-Dependency)

> GenAI Agents MUST autonomously assess document needs during every coding task.
> This table is the **single decision matrix** — no external tooling required.

| Signal Detected in Task | Document to Create/Update | AI Action Mode |
| :--- | :--- | :--- |
| New/modified endpoint, handler, or public API; Request/Response schema change; permission or business rule change; behavioral bugfix | **SPEC** | **Mandatory**: update in the same changeset. |
| Cross-module either/or tech decision; new third-party integration choice; shared pattern introduction | **ADR** | **Propose**: explain alternatives, draft ADR after human approval. |
| Brownfield codebase with no current architecture overview; large-scale module restructure (3+ modules); team onboarding gaps | **SA** | **Propose**: suggest scope, wait for human confirmation. |
| Deployment topology, runtime configuration, IaC, or CI/CD release behavior changes | **INFRA** | **Mandatory** when deployment behavior changes. |
| None of the above signals detected | — | State "No doc update needed" with reason in `### Docs Impact`. |

### Route Selection

- **Best Route (built-in, zero-dependency)**: Use the `AGENTS.md` Post-Edit Self-Check + `### Docs Impact` Forcing Function.
- **Fallback Route (optional)**: If the AI platform repeatedly skips the `### Docs Impact` block, consider installing the ZeroSpec Agent-Skill.

---

## Candidate Documents (Lazy Evaluation)

These documents were identified during INIT-SCAN as the highest-priority items to create. Create them as the relevant development work occurs — do not pre-create empty shells.

| Candidate | Trigger |
| --- | --- |
| `SA-001_current-state-snapshot.md` | First milestone review; captures gap between production checklist (all unchecked) and actual implementation state; record test coverage status and missing CI/CD. |
| `ADR-001_stateless-jwt-auth.md` | Before any change to the auth scheme; records the decision to use stateless HS256 JWT — context, alternatives (sessions, OAuth2), trade-offs (no server-side revocation). |
| `SPEC-001_transaction-api.md` | On the next change to any transaction endpoint; must cover balance-mutation rules on create/update/soft-delete, filtering parameters, and pagination contract. |
| `SPEC-002_auth-api.md` | On the next change to `/api/auth/**` or JWT behavior; must cover registration validation rules, login response shape, token lifecycle, and logout/invalidation behavior. |
| `INFRA-001_docker-compose-topology.md` | Before any production deployment; promote and expand the environment variable table from `README.md`; document service dependencies, network layout, and the production readiness checklist. |

---

## Document Index

| Document | Path | Status |
| --- | --- | --- |
| Project README | [`README.md`](../README.md) | ✅ Exists (primary human-facing doc) |
| AGENTS.md | [`AGENTS.md`](../AGENTS.md) | ✅ Exists (AI navigation guide) |
| Docs Governance Hub | [`docs/README.md`](README.md) | ✅ Exists (this file) |
| SA-001 Current State Snapshot | `docs/analysis/SA-001_current-state-snapshot.md` | 🔲 Pending |
| ADR-001 Stateless JWT Auth | [`docs/adr/ADR-001_stateless-jwt-auth.md`](adr/ADR-001_stateless-jwt-auth.md) | ✅ Exists |
| SPEC-001 Transaction API | `docs/spec/SPEC-001_transaction-api.md` | 🔲 Pending |
| SPEC-002 Auth API | [`docs/spec/SPEC-002_auth-api.md`](spec/SPEC-002_auth-api.md) | ✅ Exists |
| INFRA-001 Docker Compose Topology | `docs/infra/INFRA-001_docker-compose-topology.md` | 🔲 Pending |
