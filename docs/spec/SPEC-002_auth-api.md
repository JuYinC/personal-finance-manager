# SPEC-002 — Auth API

**Version**: 1.0
**Status**: Current
**Last Updated**: 2026-06-02
**Related ADR**: [ADR-001 Stateless JWT Auth](../adr/ADR-001_stateless-jwt-auth.md)

---

## Overview

The Auth API handles user registration, login, and logout. All tokens are stateless HS256 JWTs with a 24-hour expiry. Revocation is handled via a per-user `tokenVersion` counter.

Base path: `/api/auth` (public — no JWT required, except `/logout`)

---

## Endpoints

### POST /api/auth/register

Create a new user account.

**Request**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "SecurePass123"
}
```

| Field | Type | Rules |
|---|---|---|
| `name` | string | Required, non-blank |
| `email` | string | Required, valid email format, must be unique |
| `password` | string | Required, min 8 chars |

**Response `201 Created`**
```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "jane@example.com",
  "name": "Jane Doe"
}
```

**Errors**
| Status | Condition |
|---|---|
| `400` | Validation failure (missing field, invalid email, weak password) |
| `400` | Email already registered |

---

### POST /api/auth/login

Authenticate and receive a JWT.

**Request**
```json
{
  "email": "jane@example.com",
  "password": "SecurePass123"
}
```

**Response `200 OK`**
```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "jane@example.com",
  "name": "Jane Doe"
}
```

**Errors**
| Status | Condition |
|---|---|
| `401` | Invalid email or password |

---

### POST /api/auth/logout

Invalidate all existing tokens for the current user by incrementing `tokenVersion`.

**Requires**: `Authorization: Bearer <jwt>`

**Request**: no body.

**Response `204 No Content`**: no body.

**Behaviour**
- `tokenVersion` is incremented atomically in the DB.
- All previously issued tokens (including the one used for this request) are immediately rejected on their next use.
- The client MUST discard its locally stored token.
- Re-login is required to obtain a new token.

**Errors**
| Status | Condition |
|---|---|
| `401` | Missing or invalid/expired JWT |

---

## Token Lifecycle

| Event | tokenVersion | Session effect |
|---|---|---|
| Register / Login | unchanged (new token issued at current version) | New session starts |
| Logout | **+1** | All sessions invalidated immediately |
| Password change (`PUT /api/users/me/password`) | unchanged | Current session continues; new token returned; other sessions remain valid |
| Token expiry (24 h) | unchanged | Client must re-login |

---

## JWT Structure

| Claim | Value |
|---|---|
| `sub` | User UUID string (immutable) |
| `uid` | User UUID string |
| `tv`  | `tokenVersion` integer at time of issuance |
| `iat` | Issue timestamp (epoch seconds) |
| `exp` | Expiry timestamp (`iat` + 86 400 s) |

Algorithm: **HS256**. Key: configured via `JWT_SECRET` environment variable (minimum 256-bit / 32-character key required).

---

## Usage Pattern

```
Authorization: Bearer <jwt>
```

All non-auth endpoints require this header. Requests without a valid, non-expired, non-revoked token receive `401 Unauthorized`.

---

## Changelog

| Version | Date | Change |
|---|---|---|
| 1.0 | 2026-06-02 | Initial spec — register, login, logout; tokenVersion revocation; password change re-issues token |
