# System Inputs and Restrictions

This document lists every input the IssueFlow system receives from users, the current validation constraints applied to them, and recommendations for additional security restrictions.

## 1. System Inputs & Current Restrictions

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
| **CreateCommentRequest** | `authorId` | Long | `@NotNull`, `@Max(50)` |
| | `content` | String | `@NotBlank`, `@Size(max=3000)` |
| **UpdateCommentRequest** | `content` | String | `@NotBlank`, `@Size(max=3000)` |
| **CreateDependencyRequest**| `blockedBy` | Long | `@NotNull`, `@Max(100)` |
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

## 2. Recommended Security Restrictions

While basic validation is implemented correctly, the following additions are highly recommended for enterprise security:

### A. Injection & XSS (Cross-Site Scripting) Prevention
> [!WARNING]
> Text inputs (`content`, `description`, `title`, `name`, `fullName`) currently allow arbitrary strings, including potential HTML or Javascript payloads.
* **Recommendation**: Implement a global HTML sanitizer (like OWASP AntiSamy or Jsoup) or encode data at the presentation layer. You can also add custom annotations (e.g., `@SafeHtml` or regex patterns rejecting `<script>` tags) to block malicious XSS injections.

### B. Password Complexity
> [!IMPORTANT]
> The current `password` only requires 8 to 128 characters.
* **Recommendation**: Require stronger passwords. Add a `@Pattern` annotation to enforce at least one uppercase letter, one lowercase letter, one number, and one special character.
  ```java
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message="Password must meet complexity requirements")
  ```

### C. File Upload Security
> [!CAUTION]
> The attachment system validates MIME types, but checking the file extension or HTTP header is not enough.
* **Recommendation**: 
  1. Inspect the "magic bytes" (file signature) of uploaded files using Apache Tika to prevent users from renaming a `.exe` to `.png`.
  2. For the CSV Import feature, implement strict validation to prevent **CSV Injection** (where cell data starts with `=, +, -, @` executing formulas in spreadsheet apps).

### D. Missing Pagination Constraints
> [!TIP]
> The `UserController` pagination endpoint misses the `@Max` constraint on `pageSize`.
* **Recommendation**: Add `@Max(100)` to `pageSize` in `UserController` to prevent potential Denial of Service (DoS) attacks where a user requests millions of records in a single page.

### E. IDOR (Insecure Direct Object Reference) Protection
> [!NOTE]
> Request DTOs supply IDs like `authorId` or `ownerId`.
* **Recommendation**: Avoid relying on client-provided IDs for the authenticated user context. For instance, instead of trusting `authorId` from the client payload during `CreateCommentRequest`, extract the authenticated user's ID directly from the JWT Security Context.

### F. Rate Limiting & Brute Force Protection
> [!IMPORTANT]
> Public endpoints like `POST /auth/login` and `POST /users` are highly vulnerable to brute-force or spam attacks.
* **Recommendation**: Implement API Rate Limiting (e.g., using Resilience4j or Bucket4j) for login attempts to prevent credential stuffing.
