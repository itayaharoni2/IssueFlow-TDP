# IssueFlow — Implementation Rules

READ THIS FILE BEFORE EVERY IMPLEMENTATION PROMPT.

---

## 1. Current Project State

- **Framework**: Spring Boot 3.4.2, Java 21, Maven (via `./mvnw`)
- **Database**: PostgreSQL via `docker compose up -d` (localhost:5432, user/pass/db = `issueflow`)
- **Existing code**: Only `IssueFlowApplication.java` (empty starter) and a placeholder test
- **Existing resources**: `application.yaml` (configured), stub `schema.sql` / `data.sql` (to be replaced)
- **Dependencies already in pom.xml**: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, postgresql, lombok, h2 (test), commons-csv
- **Still needed in pom.xml**: spring-boot-starter-security, jjwt-api, jjwt-impl, jjwt-jackson

---

## 2. Architecture

Layered architecture under base package `com.att.tdp.issueflow`:

```
controller  →  service  →  repository  →  entity
```

Sub-packages:
- `entity/` + `entity/enums/` — JPA entities and enum types
- `repository/` — Spring Data JPA interfaces
- `service/` + `service/scheduler/` — Business logic
- `controller/` — REST endpoints
- `dto/` (sub-packages: `auth`, `user`, `project`, `ticket`, `comment`) — Request/Response DTOs
- `config/` — SecurityConfig, JwtProvider, JwtAuthFilter, SchedulerConfig
- `exception/` — GlobalExceptionHandler, ErrorResponse, custom exceptions

---

## 3. Implementation Steps (Small Changes)

### Phase 1 — Setup & Database
1. Add `spring-boot-starter-security` + JJWT deps to `pom.xml`
2. Create all enums: `Role`, `TicketStatus`, `TicketPriority`, `TicketType`, `AuditAction`
3. Create `User` entity (with `password` field, `@Version`)
4. Create `Project` entity (with `deletedAt` for soft-delete)
5. Create `Ticket` entity (with `deletedAt`, `isOverdue`, `dueDate`, `@Version`)
6. Create `Comment` entity (with `@Version`)
7. Create `Attachment` entity (`@Lob byte[] data`)
8. Create `TicketDependency` entity (composite unique on ticketId + blockedById)
9. Create `AuditLog` entity
10. Create `CommentMention` join entity (commentId + userId)
11. Clean `schema.sql` and `data.sql` (remove placeholder `task` table)
12. Update `application.yaml` (set `ddl-auto: update`)

### Phase 2 — Auth & Security
13. Create `JwtProvider` (generate, validate, parse JWT)
14. Create `JwtAuthFilter` (OncePerRequestFilter, reads Bearer token)
15. Create `SecurityConfig` (filter chain, permit `/auth/login`, ADMIN-only paths)
16. Create `LoginRequest` / `LoginResponse` DTOs
17. Create `AuthService` (login, logout with in-memory deny-list, getCurrentUser)
18. Create `AuthController` (POST /auth/login, POST /auth/logout, GET /auth/me)

### Phase 3 — Core CRUD
19. Create all repositories (User, Project, Ticket, Comment, AuditLog)
20. Create exception classes (GlobalExceptionHandler, ResourceNotFoundException, BadRequestException, ErrorResponse)
21. Create User DTOs + UserService + UserController
22. Create AuditLogService (reusable `log()` method called by all mutations)
23. Create Project DTOs + ProjectService + ProjectController (incl. soft-delete, restore, workload)
24. Create Ticket DTOs + TicketService + TicketController (incl. soft-delete, restore)
25. Create Comment DTOs + CommentService + CommentController

### Phase 4 — Ticket Business Rules
26. Add lifecycle transition guard to TicketService (forward-only status)
27. Add DONE-ticket update rejection to TicketService
28. Create TicketDependencyRepository + DependencyService + DependencyController
29. Add blocker check to TicketService (cannot DONE if unresolved blockers)
30. Add auto-assignment logic to TicketService (on create, no assigneeId)
31. Create EscalationScheduler (background @Scheduled task)
32. Add @EnableScheduling to config

### Phase 5 — Extended Features
33. Create AttachmentRepository + AttachmentService + AttachmentController
34. Add @mention parsing to CommentService (regex, case-insensitive)
35. Create CommentMentionRepository
36. Add GET /users/:userId/mentions endpoint (paginated)
37. Add CSV export to TicketService/Controller
38. Add CSV import to TicketService/Controller
39. Create AuditLogController (GET /audit-logs with filters)

### Phase 6 — Tests
40. Create test application.yaml (H2, disable scheduler)
41. Write auth tests
42. Write ticket lifecycle tests
43. Write auto-assignment tests
44. Write auto-escalation tests
45. Write attachment validation tests

### Phase 7 — Documentation
46. Finalize run.md with exact commands
47. Finalize prompts.md with all prompts used
48. Seed data.sql with example users

---

## 4. Required API Endpoints

### Auth
| Method | Path | Notes |
|--------|------|-------|
| POST | `/auth/login` | Body: `{ username, password }` → returns `{ accessToken, tokenType, expiresIn }` |
| POST | `/auth/logout` | Invalidates current JWT |
| GET | `/auth/me` | Returns current authenticated user |

### Users
| Method | Path | Notes |
|--------|------|-------|
| GET | `/users` | List all users |
| GET | `/users/:userId` | Get user by ID |
| POST | `/users` | Create user (add `password` field) |
| POST | `/users/update/:userId` | Update user (NOTE: POST, not PATCH) |
| DELETE | `/users/:userId` | Delete user |
| GET | `/users/:userId/mentions` | Paginated mentions (`?page=&pageSize=`) |

### Projects
| Method | Path | Notes |
|--------|------|-------|
| GET | `/projects` | List non-deleted projects |
| GET | `/projects/:projectId` | Get project by ID |
| POST | `/projects` | Create project |
| PATCH | `/projects/:projectId` | Update project |
| DELETE | `/projects/:projectId` | Soft-delete project |
| GET | `/projects/deleted` | ADMIN only — list soft-deleted |
| POST | `/projects/:projectId/restore` | ADMIN only — restore |
| GET | `/projects/:projectId/workload` | Returns `[{ userId, username, openTicketCount }]` |

### Tickets
| Method | Path | Notes |
|--------|------|-------|
| GET | `/tickets?projectId=` | List non-deleted tickets for project |
| GET | `/tickets/:ticketId` | Get ticket by ID |
| POST | `/tickets` | Create ticket (auto-assign if no assigneeId) |
| PATCH | `/tickets/:ticketId` | Update ticket (lifecycle rules apply) |
| DELETE | `/tickets/:ticketId` | Soft-delete ticket |
| GET | `/tickets/deleted?projectId=` | ADMIN only |
| POST | `/tickets/:ticketId/restore` | ADMIN only |
| GET | `/tickets/export?projectId=` | Returns CSV file |
| POST | `/tickets/import` | Multipart: `file` (CSV) + `projectId` (form field) |

### Comments
| Method | Path | Notes |
|--------|------|-------|
| GET | `/tickets/:ticketId/comments` | List comments for ticket |
| POST | `/tickets/:ticketId/comments` | Add comment (parses @mentions) |
| PATCH | `/tickets/:ticketId/comments/:commentId` | Update comment (re-evaluates mentions) |
| DELETE | `/tickets/:ticketId/comments/:commentId` | Delete comment |

### Dependencies
| Method | Path | Notes |
|--------|------|-------|
| POST | `/tickets/:ticketId/dependencies` | Body: `{ "blockedBy": 42 }` |
| GET | `/tickets/:ticketId/dependencies` | List blocker tickets |
| DELETE | `/tickets/:ticketId/dependencies/:blockerId` | Remove dependency |

### Attachments
| Method | Path | Notes |
|--------|------|-------|
| POST | `/tickets/:ticketId/attachments` | Multipart: `file` |
| DELETE | `/tickets/:ticketId/attachments/:attachmentId` | Delete attachment |

### Audit Logs
| Method | Path | Notes |
|--------|------|-------|
| GET | `/audit-logs` | Optional filters: `entityType`, `entityId`, `action`, `actor` |

---

## 5. Critical Rules to Remember While Coding

### Enums (exact values)
- **Role**: `ADMIN`, `DEVELOPER`
- **TicketStatus**: `TODO`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`
- **TicketPriority**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- **TicketType**: `BUG`, `FEATURE`, `TECHNICAL`
- **AuditAction**: `CREATE`, `UPDATE`, `DELETE`, `RESTORE`, `AUTO_ASSIGN`, `ESCALATE`

### Ticket Lifecycle
- Forward-only: `TODO → IN_PROGRESS → IN_REVIEW → DONE`
- Backward transitions REJECTED (400)
- Once status is `DONE`, NO updates allowed at all (400)
- Cannot transition to `DONE` if any dependency (blocker) is not `DONE`

### Ticket Dependencies
- Both tickets must exist and belong to the SAME project
- No self-references

### Auto-Assignment (on create only)
- Triggered ONLY when `assigneeId` is absent on `POST /tickets`
- Candidates: only `DEVELOPER` role users
- Workload = count of non-DONE tickets assigned to user in that project
- Tie-break: oldest registered user first
- If no DEVELOPERs available: leave `assigneeId = null`, no error
- Log with `actor=SYSTEM`, `action=AUTO_ASSIGN`
- NOT triggered on PATCH, only on POST

### Auto-Escalation (background scheduler)
- Promotes priority one level: `LOW → MEDIUM → HIGH → CRITICAL`
- Only for tickets with `dueDate` set, `dueDate < now()`, status != `DONE`, not soft-deleted
- When at `CRITICAL` and still overdue: set `isOverdue = true`
- Idempotent: `CRITICAL` tickets are never promoted further
- Manual priority PATCH resets `isOverdue` to `false`
- Escalation changes priority and `isOverdue` only, NOT status
- Log with `actor=SYSTEM`, `action=ESCALATE`

### Soft Delete
- Projects and tickets only (not users, not comments)
- Set `deletedAt` timestamp; filter out from standard queries
- ADMIN-only: listing deleted records and restoring them

### Optimistic Locking
- `@Version` on `Ticket` and `Comment` entities
- Catch `ObjectOptimisticLockingFailureException` → return 409 Conflict

### Attachments
- Store as `@Lob byte[]` in DB
- Max size: 10 MB
- Allowed types: `image/png`, `image/jpeg`, `application/pdf`, `text/plain`
- Reject all others with 400

### Mentions
- Parse `@username` from comment content using regex
- Case-insensitive username matching
- Re-evaluate on comment update (delete old mentions, insert new)
- Deduplicate mentions in same comment
- Unknown `@usernames` are silently ignored

### CSV Export/Import
- Export fields: `id, title, description, status, priority, type, assigneeId`
- Use Apache Commons CSV — handle commas and quotes correctly
- Import returns `{ "created": N, "failed": M, "errors": [...] }`

### Audit Log
- Append-only, read-only (no POST/PUT/DELETE on audit-logs endpoint)
- Records ALL state-changing actions: CREATE, UPDATE, DELETE, RESTORE
- Also records system actions: AUTO_ASSIGN, ESCALATE
- Fields: `action`, `entityType`, `entityId`, `performedBy`, `actor` (USER or SYSTEM), `timestamp`

### Response Codes (general)
- All success responses: `200 OK`
- Validation errors: `400 Bad Request`
- Auth failures: `401 Unauthorized`
- Role-based access denied: `403 Forbidden`
- Not found: `404 Not Found`
- Conflict (duplicate/optimistic lock): `409 Conflict`

### Design Decisions Made
- **Password**: Add `password` to `POST /users` body; default to `"secret"` if not provided; bcrypt-hash before storing
- **JWT logout**: In-memory deny-list (ConcurrentHashMap.newKeySet); acceptable for this assignment
- **Attachment storage**: `@Lob byte[]` in PostgreSQL; simple, self-contained
- **Project membership**: All DEVELOPERs are candidates for auto-assignment (no explicit project-member join table needed — query by assignees in same project)
- **DTO mapping**: Manual constructors / static factory methods, no MapStruct
- **Hibernate schema**: `ddl-auto: update` — let Hibernate manage table creation

### Note on user update endpoint
- The README specifies `POST /users/update/:userId` — NOT PATCH. Follow the README exactly.
