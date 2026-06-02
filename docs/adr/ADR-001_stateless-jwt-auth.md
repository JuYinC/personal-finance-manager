# ADR-001 — Stateless JWT Authentication with Per-User Token Versioning

| Field | Value |
|---|---|
| **Status** | Accepted |
| **Date** | 2026-06-02 |
| **Author** | finance-manager team |

---

## Context

personal-finance-manager needs an authentication mechanism for its REST API. The API is stateless (STATELESS session policy) and served to a Vue 3 SPA. The following requirements apply:

- Users must be able to log in and have their identity verified on every request.
- Users must be able to log out and have their session immediately invalidated.
- Changing a password must not disrupt the current session (seamless continuity).
- No external infrastructure (Redis, session store) should be required in the initial release.

## Decision

Use **stateless HS256 JWT** with a **per-user `tokenVersion`** counter stored in the `users` table.

### Token structure
| Claim | Value | Purpose |
|---|---|---|
| `sub` | User UUID string | Immutable identity (not email, which is mutable) |
| `uid` | User UUID string | Explicit fast accessor |
| `tv`  | Integer (`tokenVersion`) | Version counter for soft revocation |
| `iat` | Issue timestamp | Standard |
| `exp` | Expiry timestamp (24 h) | Standard |

### Revocation mechanism
- **Logout**: increment `token_version` in DB → all tokens with an older version are rejected on the next request.
- **Password change**: do NOT increment `token_version`. Instead, re-issue a new token for the current session. The frontend replaces its stored token. Other existing sessions remain valid (intended for a single-user personal finance app).

## Alternatives Considered

| Alternative | Reason Rejected |
|---|---|
| **Session-based auth** | Requires sticky sessions or shared session store; conflicts with STATELESS policy and SPA architecture |
| **OAuth2 / OpenID Connect** | Significant complexity overhead for a personal single-tenant app; can be adopted later |
| **JWT + refresh token (dual-token)** | Adds complexity (refresh endpoint, refresh token storage/revocation); out of scope for initial release |
| **Token blocklist (Redis/DB)** | Requires additional infrastructure; per-user `tokenVersion` achieves equivalent revocation for the logout use case with only a DB column |

## Trade-offs

| | Impact |
|---|---|
| No server-side revocation of individual tokens | A stolen access token remains valid until expiry (24 h) or until the user logs out |
| Password change does not revoke other sessions | Other devices stay logged in after a password change; explicit logout is required to revoke them |
| One DB lookup per request | Required to compare `tokenVersion`; acceptable at this scale; can be mitigated with short-lived in-process cache if needed |

## Consequences

- `users` table has a `token_version INTEGER NOT NULL DEFAULT 0` column.
- `POST /api/auth/logout` is a new authenticated endpoint.
- `PUT /api/users/me/password` returns `200 OK` with a new `AuthResponse` (previously `204 No Content`).
- JWT `sub` is a UUID string — email-based token lookups are no longer used.
- If a future requirement demands instant revocation of stolen tokens, introduce a short-lived token blocklist (Redis TTL = token expiry window) as a follow-up ADR.
