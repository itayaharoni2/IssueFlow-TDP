# System Inputs and Restrictions

This document lists every input the IssueFlow system receives from users, the current validation constraints applied to them, and recommendations for additional security restrictions.

## System Inputs & Current Restrictions

The table below describes the inputs received via REST endpoints (Request Bodies, Query Parameters, and File Uploads) along with their current validation rules. Path variables (like `userId`, `projectId`, `ticketId`) are generally restricted to `Long` values and checked for existence at the service layer.

| Input Location / API | Field Name | Data Type | Current Restrictions / Validations |
|---|---|---|---|
| **CreateUserRequest** | `username` | String | `@NotBlank`, `@Size(min=3, max=25)`, `@Pattern("^[a-zA-Z0-9_]+$")` |
| | `email` | String | `@NotBlank`, `@Email`, `@Size(max=50)` |
| | `fullName` | String | `@NotBlank`, `@Size(min=1, max=50)` |
| | `password` | String | `@NotBlank`, `@Size(min=8, max=128)`, `@Pattern` (at least 1 letter and 1 number) |
| **UpdateUserRequest** | `fullName` | String | `@Size(min=1, max=50)` (Optional) |
| | `role` | Enum (Role) | Must be valid enum value (`ADMIN` or `DEVELOPER`) |
| **LoginRequest** | `username` | String | `@NotBlank`, `@Size(max=25)` |
| | `password` | String | `@NotBlank`, `@Size(max=128)` |
| **CreateProjectRequest** | `name` | String | `@NotBlank`, `@Size(min=1, max=200)` |
| | `description` | String | `@Size(max=1000)` (Optional) |
| | `ownerId` | Long | `@NotNull` |
| **UpdateProjectRequest** | `name` | String | `@Size(min=1, max=200)` (Optional) |
| | `description` | String | `@Size(max=1000)` (Optional) |
| **CreateTicketRequest** | `title` | String | `@NotBlank`, `@Size(min=1, max=300)` |
| | `description` | String | `@Size(max=2000)` (Optional) |
| | `status` | Enum | Valid enum (defaults to `TODO`) |
| | `priority` | Enum | `@NotNull`, valid enum (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) |
| | `type` | Enum | `@NotNull`, valid enum (`BUG`, `FEATURE`, `TECHNICAL`) |
| | `projectId` | Long | `@NotNull` |
| | `assigneeId` | Long | Optional (Triggers auto-assignment if null) |
| | `dueDate` | OffsetDateTime| `@FutureOrPresent` (Optional) |
| **UpdateTicketRequest** | `title` | String | `@Size(min=1, max=300)` (Optional) |
| | `description` | String | `@Size(max=2000)` (Optional) |
| | `status`, `priority` | Enum | Valid enum values (Optional) |
| | `assigneeId` | Long | Optional |
| | `dueDate` | OffsetDateTime| `@FutureOrPresent` (Optional) |
| **CreateCommentRequest** | `authorId` | Long | `@NotNull` |
| | `content` | String | `@NotBlank`, `@Size(max=3000)` |
| **UpdateCommentRequest** | `content` | String | `@NotBlank`, `@Size(max=3000)` |
| **CreateDependencyRequest**| `blockedBy` | Long | `@NotNull` |
| **File Attachments** | `file` | MultipartFile | Max Size: 10MB (via Spring Config), Allowed types: `image/png`, `image/jpeg`, `application/pdf`, `text/plain` |
| **CSV Import** | `file` | MultipartFile | CSV format, handles commas/quotes |
| | `projectId` | Long | Valid project ID |
| **Pagination Parameters** | `page` | Integer | `@Min(1)`, Default = 1 |
| | `size` / `pageSize` | Integer | `@Min(1)`, `@Max(100)` (Note: missing `@Max` limit on `UserController`) |
| **Audit Log Parameters** | `entityType` | String | `@Size(max=50)` |
| | `entityId` | Long | None |
| | `action` | Enum | Valid enum value |
| | `actor` | String | `@Size(max=10)` |

---
