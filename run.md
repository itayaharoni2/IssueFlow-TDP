# Run Instructions

This document describes how to setup, run, and test the IssueFlow Ticket Management Backend Platform.

## Prerequisites
* **Java SDK**: Version 21 (or 25)
* **Maven**: Included via `./mvnw` wrapper
* **Docker / Docker Compose**: Required for starting the PostgreSQL database instance

## Database Startup
To start the PostgreSQL database instance:
```bash
docker compose up -d
```
The database will be available at `localhost:5432/issueflow` with user `issueflow` and password `issueflow`.

## Application Startup
To run the Spring Boot application:
```bash
./mvnw spring-boot:run
```
The server will start on port `8080`.

## Environment Variables
The application uses the default settings in `application.yaml`. You can customize them via the following environment variables if needed:
* `SPRING_DATASOURCE_URL` (Default: `jdbc:postgresql://localhost:5432/issueflow`)
* `SPRING_DATASOURCE_USERNAME` (Default: `issueflow`)
* `SPRING_DATASOURCE_PASSWORD` (Default: `issueflow`)
* `SERVER_PORT` (Default: `8080`)

## Running Tests
To run all automated tests (using H2 in-memory database):
```bash
./mvnw test
```

## Example Seed Users
Once the application starts, the database is seeded with default users for testing (defined in `data.sql`):
1. **Admin User**:
   * Username: `admin`
   * Password: `secret` (or customized password)
   * Role: `ADMIN`
2. **Developer User**:
   * Username: `jdoe`
   * Password: `secret`
   * Role: `DEVELOPER`
