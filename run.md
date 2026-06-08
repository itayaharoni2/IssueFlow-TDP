# Run Instructions

This document describes how to setup, run, and test the IssueFlow Ticket Management Backend Platform.

## Prerequisites
* **Java SDK**: Version 21 (or 25)
* **Maven**: Included via wrapper (`mvnw` / `mvnw.cmd`)
* **Docker & Docker Compose**: Required for starting the PostgreSQL database instance

## Database Startup
To start the PostgreSQL database instance in the background:
```bash
docker compose up -d
```
The database will be available at `localhost:5432/issueflow` with user `issueflow` and password `issueflow`.

## Build the Project & Install Dependencies
To fetch all required Maven dependencies and compile the project into an executable JAR:

* **Windows (PowerShell/CMD)**:
  ```powershell
  .\mvnw.cmd clean install -DskipTests
  ```
* **macOS / Linux (Bash/Zsh)**:
  ```bash
  ./mvnw clean install -DskipTests
  ```

## Application Startup
To run the Spring Boot application locally:

* **Windows (PowerShell)**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
* **Windows (CMD)**:
  ```cmd
  mvnw.cmd spring-boot:run
  ```
* **macOS / Linux (Bash/Zsh)**:
  ```bash
  ./mvnw spring-boot:run
  ```

The server starts on port `8080` by default.


## Running Tests
To run all automated integration and unit tests (uses isolated H2 database):

* **Windows (PowerShell)**:
  ```powershell
  .\mvnw.cmd test
  ```
* **Windows (CMD)**:
  ```cmd
  mvnw.cmd test
  ```
* **macOS / Linux (Bash/Zsh)**:
  ```bash
  ./mvnw test
  ```

## Example Seed Users
Upon application startup, the PostgreSQL database is automatically seeded with default users for testing (defined in `data.sql`):
1. **Admin User**:
   * Username: `admin`
   * Password: `secret` (Encoded using BCrypt)
   * Role: `ADMIN`
2. **Developer User**:
   * Username: `jdoe`
   * Password: `secret` (Encoded using BCrypt)
   * Role: `DEVELOPER`

