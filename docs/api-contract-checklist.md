# IssueFlow — API Contract Checklist

> **Source of Truth**: `README.md` API tables.
> **Requirements Cross-Reference**: `TDP_issueflow_requirements.pdf`
> **Phase 1 Status**: Entities and enums created. No controllers/services yet.

---

## 1. Full API Endpoint Checklist

### 1.1 Authentication APIs

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| A1 | POST | `/auth/login` | `{ username, password }` | `{ accessToken, tokenType, expiresIn }` | No | None | `AuthController` | `AuthService` | ✅ Complete |
| A2 | POST | `/auth/logout` | — | — | Yes | Any | `AuthController` | `AuthService` | ✅ Complete |
| A3 | GET | `/auth/me` | — | User object | Yes | Any | `AuthController` | `AuthService` | ✅ Complete |

**Notes**:
- `expiresIn` in the README example shows `3600` (seconds), but our config uses 86400000ms. Return `expiresIn` in **seconds** (`86400`).
- Logout uses an in-memory deny-list (`ConcurrentHashMap.newKeySet()`).

---

### 1.2 Users APIs

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| U1 | GET | `/users` | — | `[{ id, username, email, fullName, role }]` | Yes | Any | `UserController` | `UserService` | ✅ Complete |
| U2 | GET | `/users/:userId` | — | `{ id, username, email, fullName, role }` | Yes | Any | `UserController` | `UserService` | ✅ Complete |
| U3 | POST | `/users` | `{ username, email, fullName, role, password? }` | `{ id, username, email, fullName, role }` | No | None | `UserController` | `UserService` | ✅ Complete |
| U4 | POST | `/users/update/:userId` | `{ fullName?, role? }` | — (200 OK) | Yes | Any | `UserController` | `UserService` | ✅ Complete |
| U5 | DELETE | `/users/:userId` | — | — (200 OK) | Yes | Any | `UserController` | `UserService` | ✅ Complete |
| U6 | GET | `/users/:userId/mentions` | — (query: `page`, `pageSize`) | `{ data: [...], total, page }` | Yes | Any | `UserController` | `CommentMentionService` | ✅ Complete |

**⚠️ CRITICAL MISMATCH — `email` field**:
- README response body includes `email` in every User response: `{ id, username, email, fullName, role }`
- Requirements PDF mentions `username, email, full_name, role` for registration
- **Current `User` entity is MISSING the `email` field** → must add before Phase 2

**Notes**:
- `POST /users` (U3): `email` must be stored. No auth required (registration is public like login).
- `POST /users/update/:userId` (U4): NOT a PATCH — follow README exactly.
- U6 pagination: response envelope `{ data: [...], total: N, page: N }`.
- Password: not returned in any response; only stored (bcrypt-hashed).

---

### 1.3 Projects APIs

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| P1 | GET | `/projects` | — | `[{ id, name, description, ownerId }]` | Yes | Any | `ProjectController` | `ProjectService` | ✅ Complete |
| P2 | GET | `/projects/:projectId` | — | `{ id, name, description, ownerId }` | Yes | Any | `ProjectController` | `ProjectService` | ✅ Complete |
| P3 | POST | `/projects` | `{ name, description, ownerId }` | `{ id, name, description, ownerId }` | Yes | Any | `ProjectController` | `ProjectService` | ✅ Complete |
| P4 | PATCH | `/projects/:projectId` | `{ name?, description? }` | — (200 OK) | Yes | Any | `ProjectController` | `ProjectService` | ✅ Complete |
| P5 | DELETE | `/projects/:projectId` | — | — (200 OK) | Yes | Any | `ProjectController` | `ProjectService` | ✅ Complete |
| P6 | GET | `/projects/deleted` | — | `[{ id, name, description, ownerId }]` | Yes | **ADMIN** | `ProjectController` | `ProjectService` | ✅ Complete |
| P7 | POST | `/projects/:projectId/restore` | — | — (200 OK) | Yes | **ADMIN** | `ProjectController` | `ProjectService` | ✅ Complete |
| P8 | GET | `/projects/:projectId/workload` | — | `[{ userId, username, openTicketCount }]` | Yes | Any | `ProjectController` | `ProjectService` | ✅ Complete |

**Notes**:
- `GET /projects` (P1): must filter out soft-deleted projects (`deletedAt IS NULL`).
- `GET /projects/deleted` (P6): ADMIN only — Spring Security `@PreAuthorize("hasRole('ADMIN')")`.
- ⚠️ **Route order risk**: `/projects/deleted` must be mapped BEFORE `/projects/:projectId` in the controller, or Spring will interpret `deleted` as a projectId.

---

### 1.4 Tickets APIs

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| T1 | GET | `/tickets?projectId=` | — | `[{ id, title, description, status, priority, type, projectId, assigneeId, dueDate, isOverdue }]` | Yes | Any | `TicketController` | `TicketService` | ✅ Complete |
| T2 | GET | `/tickets/:ticketId` | — | `{ id, title, description, status, priority, type, projectId, assigneeId, dueDate, isOverdue }` | Yes | Any | `TicketController` | `TicketService` | ✅ Complete |
| T3 | POST | `/tickets` | `{ title, description, status, priority, type, projectId, assigneeId?, dueDate? }` | Full ticket object | Yes | Any | `TicketController` | `TicketService` | ✅ Complete |
| T4 | PATCH | `/tickets/:ticketId` | `{ title?, description?, status?, priority?, assigneeId?, dueDate? }` | — (200 OK) | Yes | Any | `TicketController` | `TicketService` | ✅ Complete |
| T5 | DELETE | `/tickets/:ticketId` | — | — (200 OK) | Yes | Any | `TicketController` | `TicketService` | ✅ Complete |
| T6 | GET | `/tickets/deleted?projectId=` | — | `[{ id, title, status, priority, type, projectId }]` | Yes | **ADMIN** | `TicketController` | `TicketService` | ✅ Complete |
| T7 | POST | `/tickets/:ticketId/restore` | — | — (200 OK) | Yes | **ADMIN** | `TicketController` | `TicketService` | ✅ Complete |
| T8 | GET | `/tickets/export?projectId=` | — | CSV file (Content-Type: `text/csv`) | Yes | Any | `TicketController` | `TicketService` | ✅ Complete |
| T9 | POST | `/tickets/import` | `multipart/form-data`: `file` (CSV) + `projectId` (form field) | `{ created, failed, errors }` | Yes | Any | `TicketController` | `TicketService` | ✅ Complete |

**Notes**:
- T1, T2: both include `dueDate` and `isOverdue` in response — these are already in the `Ticket` entity ✅.
- T3: `status` is included in create request but requirements say default is `TODO`. Can accept it to allow caller to set an initial status.
- T4 PATCH request in README does NOT include `version`. Requirements say optimistic locking via `@Version` — mismatch needs a decision (see §3).
- ⚠️ **Route order risk**: `/tickets/deleted` and `/tickets/export` must be mapped BEFORE `/tickets/:ticketId`.
- T8: Response must set `Content-Disposition: attachment; filename="tickets.csv"` and `Content-Type: text/csv`.
- T9: `projectId` comes as a **form field** (not JSON body).

---

### 1.5 Comments APIs

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| C1 | GET | `/tickets/:ticketId/comments` | — | `[{ id, ticketId, authorId, content, mentionedUsers: [{id, username, fullName}] }]` | Yes | Any | `CommentController` | `CommentService` | ✅ Complete |
| C2 | POST | `/tickets/:ticketId/comments` | `{ authorId, content }` | `{ id, ticketId, authorId, content, mentionedUsers: [...] }` | Yes | Any | `CommentController` | `CommentService` | ✅ Complete |
| C3 | PATCH | `/tickets/:ticketId/comments/:commentId` | `{ content }` | — (200 OK) | Yes | Any | `CommentController` | `CommentService` | ✅ Complete |
| C4 | DELETE | `/tickets/:ticketId/comments/:commentId` | — | — (200 OK) | Yes | Any | `CommentController` | `CommentService` | ✅ Complete |

**Notes**:
- Every comment response (C1, C2) must include `mentionedUsers` — requires join to `CommentMention` + `User`.
- Optimistic locking on `Comment` (`@Version`) → catch `ObjectOptimisticLockingFailureException` → return 409.
- `authorId` in request body: the PDF says "content and authorId" — keep it as-is (caller supplies authorId explicitly).

---

### 1.6 Audit Log APIs

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| AL1 | GET | `/audit-logs` | — (query: `entityType?`, `entityId?`, `action?`, `actor?`) | `[{ id, action, entityType, entityId, performedBy, actor, timestamp }]` | Yes | Any | `AuditLogController` | `AuditLogService` | ✅ Complete |

**⚠️ MISMATCH — `performedBy` type in README**:
- README example shows: `"performedBy": 2` (a numeric user ID)
- Our `AuditLog` entity stores `performedBy` as `String` (username)
- **Decision needed**: Store as Long userId OR String username? README shows a number. Store as `Long performedByUserId` instead.

---

### 1.7 Ticket Dependencies APIs

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| D1 | POST | `/tickets/:ticketId/dependencies` | `{ blockedBy: 42 }` | — (200 OK) | Yes | Any | `DependencyController` | `DependencyService` | ✅ Complete |
| D2 | GET | `/tickets/:ticketId/dependencies` | — | `[{ id, title, status }]` | Yes | Any | `DependencyController` | `DependencyService` | ✅ Complete |
| D3 | DELETE | `/tickets/:ticketId/dependencies/:blockerId` | — | — (200 OK) | Yes | Any | `DependencyController` | `DependencyService` | ✅ Complete |

**Notes**:
- D2 response is a list of **blocker ticket** objects (just `id, title, status`).
- Both tickets must be in the same project (validated in service).

---

### 1.8 Attachments APIs

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| AT1 | POST | `/tickets/:ticketId/attachments` | `multipart/form-data`: `file` | `{ id, ticketId, filename, contentType }` | Yes | Any | `AttachmentController` | `AttachmentService` | ✅ Complete |
| AT2 | DELETE | `/tickets/:ticketId/attachments/:attachmentId` | — | — (200 OK) | Yes | Any | `AttachmentController` | `AttachmentService` | ✅ Complete |

**Notes**:
- AT1 response does NOT include the file binary — just metadata.
- Validate: content type must be `image/png`, `image/jpeg`, `application/pdf`, `text/plain`.
- Validate: size ≤ 10MB (Spring multipart already configured).

---

### 1.9 Mentions API

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| M1 | GET | `/users/:userId/mentions` | — (query: `page?`, `pageSize?`) | `{ data: [comments with mentionedUsers], total, page }` | Yes | Any | `UserController` | `CommentMentionService` | ✅ Complete |

---

### 1.10 Workload API

| # | Method | Path | Request Body | Response Body | Auth Required? | Role Required? | Controller | Service | Status |
|---|--------|------|-------------|--------------|----------------|----------------|------------|---------|--------|
| W1 | GET | `/projects/:projectId/workload` | — | `[{ userId, username, openTicketCount }]` | Yes | Any | `ProjectController` | `ProjectService` | ✅ Complete |

---

## 2. Entity / Configuration Mismatch Report

### 🚨 Critical Fixes Required Before Phase 2

| # | Entity/Config | Issue | Fix Required |
|---|--------------|-------|-------------|
| **E1** | `User` entity | **Missing `email` field** — README response includes `email` in every user object; requirements PDF lists `email` as a registration field | Add `@Column(nullable=false, unique=true) String email;` |
| **E2** | `AuditLog` entity | `performedBy` is `String` but README response shows it as a **number** (`"performedBy": 2`) | Change `performedBy` from `String` to `Long performedByUserId` |
| **E3** | `Ticket` entity | `dueDate` is `LocalDate` but README body uses ISO-8601 datetime `"2026-04-01T00:00:00Z"` (not just a date) | Change to `LocalDateTime dueDate` OR keep as `LocalDate` and accept/return just the date portion — pick one and document it |

### ⚠️ Design Decisions Needed

| # | Topic | Question | Recommended Decision |
|---|-------|----------|---------------------|
| **Q1** | Password on POST /users | README create-user request body does NOT include `password`. Requirements PDF doesn't mention it either. But `/auth/login` requires a password. | Add optional `password` field to create request. Default to `"secret"` if absent. Bcrypt-hash before storing. **Never return it.** |
| **Q2** | Optimistic locking exposure | PATCH /tickets and PATCH /comments use `@Version`. Should the request body include `version`? Neither README nor requirements mention it. | Do NOT expose `version` in request/response DTOs. Catch `ObjectOptimisticLockingFailureException` at exception handler level → return 409. This keeps the API clean while satisfying the requirement. |
| **Q3** | `expiresIn` unit | Our config sets `expiration-ms: 86400000` (24h in ms). README example shows `"expiresIn": 3600` (looks like seconds). | Return `expiresIn` in **seconds** (divide by 1000). |
| **Q4** | `GET /auth/me` response body | README table shows no response body example for this endpoint. | Return same User DTO as other user endpoints: `{ id, username, email, fullName, role }`. |
| **Q5** | Auto-assignment: "DEVELOPER users in the project" | Requirements PDF says "queries all DEVELOPER" and "If no DEVELOPER users are linked to the project". No explicit project membership table. | Query `DEVELOPER` role users whose `id` appears as `assignee_id` in at least one non-deleted ticket in the project, OR (simpler) just query ALL users with `role=DEVELOPER`. Pick all DEVELOPERs globally — simpler and PDF says "linked to the project" which may just mean the system. |
| **Q6** | Route collision `/tickets/deleted` vs `/tickets/:id` | Spring will try to match `deleted` as a Long ticketId and throw a 400. | Map `/tickets/deleted` in the controller BEFORE `/tickets/:ticketId` to avoid ambiguity. |
| **Q7** | `POST /users` — is auth required? | README lists user creation without auth but all other endpoints are protected. | Make `POST /users` and `POST /auth/login` the only public (no-auth) endpoints. |

---

## 3. Specific Questions Answered

### 3.1 Does user registration need a password?
**Yes.** `POST /auth/login` requires a password. Add an optional `password` field to the create-user body. Default: `"secret"`. Hash with BCrypt before storing. **Never return it in any response.**

### 3.2 Which endpoints must be ADMIN-only?
| Endpoint | ADMIN only |
|----------|-----------|
| `GET /tickets/deleted` | ✅ Yes (from PDF 3.5) |
| `POST /tickets/:id/restore` | ✅ Yes (from PDF 3.5) |
| `GET /projects/deleted` | ✅ Yes (from PDF 3.5) |
| `POST /projects/:id/restore` | ✅ Yes (from PDF 3.5) |
| All others | No specific role required |

### 3.3 How should logout invalidate JWT?
Use an **in-memory deny-list** (`Set<String>` backed by `ConcurrentHashMap.newKeySet()`). On logout, extract the JWT from the `Authorization` header and add it to the deny-list. In `JwtAuthFilter`, reject any token found in the deny-list.
> Caveat: deny-list is lost on restart. Acceptable for this assignment.

### 3.4 How should optimistic locking be exposed in the API?
**Do NOT expose `version` in request/response DTOs.** The `@Version` field on `Ticket` and `Comment` is used internally by Hibernate. Catch `ObjectOptimisticLockingFailureException` in `GlobalExceptionHandler` and return `409 Conflict` with a clear message.

### 3.5 Does README define project membership?
**No explicit project membership table.** The workload endpoint (`GET /projects/:id/workload`) shows "all users in the project", which implies users who have been assigned tickets in that project. Auto-assignment should pick **all users with role=DEVELOPER** globally, computing workload as non-DONE tickets assigned to each developer in the target project. Developers with 0 tickets in that project are still candidates.

### 3.6 Are soft-deleted tickets/projects hidden from standard GETs?
**Yes.** `deletedAt IS NULL` filter must be applied in all standard list/get queries. Soft-deleted records only appear in the `GET /tickets/deleted` and `GET /projects/deleted` endpoints (ADMIN only).

### 3.7 Which endpoints need multipart/form-data?
| Endpoint | Form |
|----------|------|
| `POST /tickets/import` | `file` (CSV) + `projectId` (form field) |
| `POST /tickets/:id/attachments` | `file` only |

### 3.8 Which endpoints need CSV responses?
| Endpoint | Content-Type |
|----------|-------------|
| `GET /tickets/export?projectId=` | `text/csv` with `Content-Disposition: attachment; filename="tickets.csv"` |

### 3.9 Which response DTOs must include special fields?
| DTO | Special fields |
|-----|---------------|
| `TicketResponse` | `isOverdue`, `dueDate`, `projectId`, `assigneeId` |
| `CommentResponse` | `mentionedUsers: [{ id, username, fullName }]` |
| `UserResponse` | `email` ← **must add to entity first** |
| `AuditLogResponse` | `performedBy` as numeric userId |
| `LoginResponse` | `accessToken`, `tokenType: "Bearer"`, `expiresIn` (seconds) |
| `MentionsResponse` | paginated envelope: `{ data, total, page }` |

---

## 4. Dependency & Configuration Checklist

| Item | Status | Notes |
|------|--------|-------|
| `spring-boot-starter-web` | ✅ Present | |
| `spring-boot-starter-data-jpa` | ✅ Present | |
| `spring-boot-starter-validation` | ✅ Present | Used for `@NotBlank`, `@NotNull` in DTOs |
| `postgresql` driver | ✅ Present | |
| `lombok` | ✅ Present | |
| `spring-boot-starter-security` | ✅ Added in Phase 1 | |
| `jjwt-api/impl/jackson 0.12.6` | ✅ Added in Phase 1 | |
| `commons-csv 1.10.0` | ✅ Present | |
| `h2` (test scope) | ✅ Present | |
| `multipart max-file-size: 10MB` | ✅ Configured in application.yaml | |
| `ddl-auto: update` | ✅ Configured | |
| `schema.sql` conflict with `ddl-auto: update` | ⚠️ Minor | With `ddl-auto: update`, Spring will still run `schema.sql` due to `sql.init.mode: always`. Since it's now empty, this is fine. |
| `data.sql` with `ddl-auto: update` | ⚠️ Minor | Hibernate creates tables first; then Spring runs `data.sql`. Seed inserts must use `ON CONFLICT DO NOTHING` to be idempotent. |
| JWT secret in `application.yaml` | ⚠️ Note | Secret is in plain-text config. Acceptable for this assignment. Do NOT commit real secrets. |
| Initial ADMIN user seeding plan | ⚠️ Pending | Must add to `data.sql` in Phase 7. Use bcrypt hash of "secret" for the password. |

---

## 5. Required Fixes Before Phase 2 (Ordered by Priority)

| # | Priority | Fix | File(s) |
|---|----------|-----|---------|
| 1 | 🔴 Critical | Add `email` field to `User` entity | `User.java` |
| 2 | 🔴 Critical | Change `AuditLog.performedBy` to `Long performedByUserId` to match README's numeric `performedBy` | `AuditLog.java` |
| 3 | 🟡 Important | Decide on `dueDate` type (`LocalDate` vs `LocalDateTime`) and document it | `Ticket.java` |
| 4 | 🟡 Important | Verify test `application.yaml` disables scheduler and uses H2 | `src/test/resources/application.yaml` |
| 5 | 🟢 Low | Add `@EnableJpaRepositories` note — not needed but confirm auto-detection works | `IssueFlowApplication.java` |

---

## 6. Controller-to-Route Mapping Plan (Route Order Matters)

```
GET  /users                            → UserController.getAll()
GET  /users/{userId}                   → UserController.getById()
POST /users                            → UserController.create()          [PUBLIC]
POST /users/update/{userId}            → UserController.update()
DELETE /users/{userId}                 → UserController.delete()
GET  /users/{userId}/mentions          → UserController.getMentions()

POST /auth/login                       → AuthController.login()           [PUBLIC]
POST /auth/logout                      → AuthController.logout()
GET  /auth/me                          → AuthController.me()

GET  /projects                         → ProjectController.getAll()
GET  /projects/deleted                 → ProjectController.getDeleted()   [ADMIN] ← BEFORE /{projectId}
GET  /projects/{projectId}             → ProjectController.getById()
POST /projects                         → ProjectController.create()
PATCH /projects/{projectId}            → ProjectController.update()
DELETE /projects/{projectId}           → ProjectController.softDelete()
POST /projects/{projectId}/restore     → ProjectController.restore()      [ADMIN]
GET  /projects/{projectId}/workload    → ProjectController.workload()

GET  /tickets                          → TicketController.getByProject()  (?projectId=)
GET  /tickets/deleted                  → TicketController.getDeleted()    [ADMIN] ← BEFORE /{ticketId}
GET  /tickets/export                   → TicketController.export()        ← BEFORE /{ticketId}
POST /tickets/import                   → TicketController.import()        ← BEFORE /{ticketId}
GET  /tickets/{ticketId}               → TicketController.getById()
POST /tickets                          → TicketController.create()
PATCH /tickets/{ticketId}              → TicketController.update()
DELETE /tickets/{ticketId}             → TicketController.softDelete()
POST /tickets/{ticketId}/restore       → TicketController.restore()       [ADMIN]

GET  /tickets/{ticketId}/comments      → CommentController.getAll()
POST /tickets/{ticketId}/comments      → CommentController.add()
PATCH /tickets/{ticketId}/comments/{commentId} → CommentController.update()
DELETE /tickets/{ticketId}/comments/{commentId} → CommentController.delete()

POST /tickets/{ticketId}/dependencies  → DependencyController.add()
GET  /tickets/{ticketId}/dependencies  → DependencyController.list()
DELETE /tickets/{ticketId}/dependencies/{blockerId} → DependencyController.remove()

POST /tickets/{ticketId}/attachments   → AttachmentController.upload()
DELETE /tickets/{ticketId}/attachments/{attachmentId} → AttachmentController.delete()

GET  /audit-logs                       → AuditLogController.getLogs()
```

---

## 7. Test Application YAML Check

The test `application.yaml` must:
- Use H2 in-memory database (not PostgreSQL)
- Disable the escalation scheduler
- Use `ddl-auto: create-drop`
