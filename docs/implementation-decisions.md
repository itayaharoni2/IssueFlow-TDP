# IssueFlow — Implementation Decisions

This file documents the key design and implementation decisions made during development.
These decisions are driven by the README API contract, the requirements PDF, and practical
constraints for the assignment deadline.

---

## 1. Password on POST /users

**Decision**: `POST /users` accepts an **optional** `password` field in the request body.

**Reasoning**:
- The README does not list `password` in the create-user request body.
- However, `POST /auth/login` requires a password to authenticate.
- A user cannot log in without a password stored in the system.

**Behaviour**:
- If `password` is provided → hash with BCrypt and store.
- If `password` is absent → default to the literal string `"secret"`, then hash with BCrypt.
- **Password is never returned** in any API response (excluded from all DTOs).

---

## 2. Auto-Assignment Candidates

**Decision**: Auto-assignment candidates are **all users with `role = DEVELOPER`** in the system — no per-project membership table.

**Reasoning**:
- The README does not define a project-membership join table.
- The requirements PDF states: "queries all DEVELOPER" and "If no DEVELOPER users are **linked to the project**" — but defines "linked" implicitly as having assigned tickets in that project.
- Simplest correct interpretation: all DEVELOPERs are globally eligible.

**Workload calculation**: count of non-DONE tickets currently assigned to each DEVELOPER in the **same project** (not globally). A DEVELOPER with 0 tickets in the project gets workload = 0 and is strongly preferred.

**Tie-break**: oldest `createdAt` (registration order) wins.

**If no DEVELOPERs exist**: ticket is created with `assigneeId = null`, no error.

---

## 3. dueDate Type

**Decision**: `dueDate` is stored and exposed as **`OffsetDateTime`**.

**Reasoning**:
- The README example uses ISO-8601 datetime with timezone: `"dueDate": "2026-04-01T00:00:00Z"`.
- `LocalDate` (date-only) would lose the time component and timezone.
- `OffsetDateTime` correctly handles `Z` (UTC) and other offset suffixes.

**Storage**: Hibernate maps `OffsetDateTime` to a `TIMESTAMP WITH TIME ZONE` column in PostgreSQL.

---

## 4. Soft Delete Behaviour

**Decision**: Soft-deleted tickets and projects are **always hidden** from standard GET endpoints.

- `GET /tickets?projectId=` — returns only `deletedAt IS NULL` tickets.
- `GET /tickets/:ticketId` — returns 404 if `deletedAt IS NOT NULL`.
- `GET /projects` — returns only `deletedAt IS NULL` projects.
- `GET /projects/:projectId` — returns 404 if soft-deleted.

Soft-deleted records are only visible through:
- `GET /tickets/deleted?projectId=` (ADMIN only)
- `GET /projects/deleted` (ADMIN only)

---

## 5. ADMIN-Only Endpoints

The following endpoints require the `ADMIN` role (enforced via `@PreAuthorize("hasRole('ADMIN')")`):

| Endpoint | Reason |
|----------|--------|
| `GET /tickets/deleted?projectId=` | Requirements PDF §3.5 |
| `POST /tickets/:id/restore` | Requirements PDF §3.5 |
| `GET /projects/deleted` | Requirements PDF §3.5 |
| `POST /projects/:id/restore` | Requirements PDF §3.5 |

All other endpoints require authentication but no specific role.

---

## 6. Optimistic Locking

**Decision**: `@Version` is used on `Ticket` and `Comment` entities. The `version` field is **not exposed** in request DTOs.

**Reasoning**:
- Neither the README nor the requirements PDF mentions `version` in request bodies.
- The requirement is just "two users can't update simultaneously" → catch `ObjectOptimisticLockingFailureException`.

**API behaviour**: When a concurrent-update conflict is detected, return `409 Conflict` with a descriptive error message from `GlobalExceptionHandler`.

---

## 7. JWT Logout Strategy

**Decision**: In-memory deny-list using `ConcurrentHashMap.newKeySet()`.

- On `POST /auth/logout`: extract the raw JWT from the `Authorization: Bearer <token>` header and add it to the deny-list.
- In `JwtAuthFilter`: reject any token found in the deny-list with 401.

**Caveat**: Deny-list is lost on application restart. Tokens issued before restart become valid again until they expire. This is acceptable for the assignment scope.

---

## 8. Response DTO: password never returned

`password` is a field on the `User` entity but **must never appear** in any response DTO.

- `UserResponse` DTO will include only: `id, username, email, fullName, role`.
- `AuthService.getCurrentUser()` returns a `UserResponse` (not the entity).

---

## 9. POST /auth/login and POST /users are public endpoints

These two endpoints are **excluded from JWT authentication**:
- `POST /auth/login` — the entry point for obtaining a token.
- `POST /users` — user registration.

All other endpoints require a valid JWT in the `Authorization: Bearer <token>` header.

---

## 10. expiresIn field in login response

**Decision**: `expiresIn` is returned in **seconds** (not milliseconds).

- Config: `jwt.expiration-ms = 86400000` (24 hours in ms).
- Response: `expiresIn = 86400` (seconds).
- README example shows `"expiresIn": 3600` which is 1 hour — the config value takes precedence.

---

## 11. Route Ordering (Collision Risk)

Spring MVC matches routes by specificity. Static path segments take priority over path variables, but to be safe the following routes **must be declared before their wildcard siblings** in the controller:

| Specific route | Sibling it would collide with |
|----------------|-------------------------------|
| `GET /tickets/deleted` | `GET /tickets/{ticketId}` |
| `GET /tickets/export` | `GET /tickets/{ticketId}` |
| `POST /tickets/import` | — (no collision, but keep together) |
| `GET /projects/deleted` | `GET /projects/{projectId}` |

---

## 12. AuditLog: performedBy is a userId (Long)

**Decision**: `AuditLog.performedByUserId` is `Long` (nullable for SYSTEM-initiated actions).

**Reasoning**: The README response example shows `"performedBy": 2` (a number, not a username string).

**Response DTO**: The JSON key will be `"performedBy"` (aliased from `performedByUserId` via the DTO field name).
