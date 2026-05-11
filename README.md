# SOQL Explorer

A SOQL query builder and Salesforce data explorer. Spring Boot (Java 21) backend
with a hexagonal package layout, React 18 (Vite + TypeScript) frontend.

## Repository layout

```
soql-explorer/
├── backend/
│   ├── domain/                      Pure-Java domain model (no Spring)
│   ├── application/                 Use cases + ports
│   ├── infrastructure-persistence/  JPA adapters + Flyway migrations
│   ├── infrastructure-salesforce/   Salesforce REST adapter (Step 2)
│   ├── infrastructure-cache/        Caffeine cache beans (Step 2)
│   └── web/                         Spring Boot app, controllers, security
├── frontend/                        Vite + React 18 + TS SPA
├── docker-compose.yml               Local Postgres
└── .github/workflows/ci.yml         Gradle + npm build pipeline
```

The dependency direction is enforced by Gradle: `web` and the infrastructure
modules depend on `application`, which depends on `domain`. Nothing in `domain`
or `application` imports Spring, JPA, or Jackson.

## Prerequisites

- JDK 21 (Temurin recommended)
- Node 20+
- Docker (for local Postgres and Testcontainers)

## Running locally

```bash
# 1. Start Postgres
docker compose up -d postgres

# 2. Configure env (optional — defaults work for local dev)
cp .env.example .env

# 3. Boot the backend
./gradlew :backend:web:bootRun
# → http://localhost:8080, Swagger UI at /swagger-ui.html

# 4. In a second terminal, boot the frontend
cd frontend
npm install
npm run dev
# → http://localhost:5173 (proxies /api → http://localhost:8080)
```

There is no seeded user yet — Step 1 ships the auth machinery but registration
is deferred. To smoke-test login, insert a user manually:

```sql
INSERT INTO app_user (id, email, password_hash, enabled, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'dev@example.com',
  -- bcrypt('password', 12)
  '$2a$12$dWZh.Wd0Xv9N8RhMK6sQ1eK1.4Y6KhTM2LZ8XKQyW6kPSf3rMm3Lq',
  TRUE,
  NOW(),
  NOW()
);
INSERT INTO app_user_role (user_id, role)
SELECT id, 'USER' FROM app_user WHERE email = 'dev@example.com';
```

## Build & test

```bash
# Full backend build (Spotless, Checkstyle, JUnit, Testcontainers)
./gradlew build

# Frontend lint + unit tests
cd frontend && npm run lint && npm test
```

## Architecture decisions

- **Hexagonal layering.** Domain and application stay framework-free; adapters
  fan out under `infrastructure-*`. This makes the application module unit-
  testable without Spring and keeps swap-in adapters (e.g. an in-memory
  Salesforce stub for tests) cheap.
- **Stateless JWT auth.** Access tokens (15 min) ride in the `Authorization`
  header; refresh tokens (7 days) ride in a `HttpOnly; Secure; SameSite=Strict`
  cookie. The SPA never touches the refresh token directly.
- **Flyway baseline.** All schema lives under
  `backend/infrastructure-persistence/src/main/resources/db/migration`.
  Migrations are append-only; never edit a file once it's been applied to any
  shared environment.
- **RFC 7807 errors.** Every error becomes a `ProblemDetail` with a stable
  `type` URI so the SPA can switch on `title` instead of free-form messages.

## License

Apache 2.0 — see [LICENSE](./LICENSE).
