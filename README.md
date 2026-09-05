# todolist-spring

Reactive REST API for the TodoList project. Built with Spring Boot + WebFlux
(Java 21, Maven) with **durable PostgreSQL storage accessed reactively (R2DBC)**.
Runs on `localhost:8080`.

## Prerequisites

- **JDK 21** (LTS) — required by Spring Boot 3.x and Java records
- **Maven 3.6.3+** (or use the Maven Wrapper `./mvnw`)
- **PostgreSQL** — persistence is database-backed. Easiest via Docker Compose
  (see below), or point `SPRING_R2DBC_URL`/`SPRING_R2DBC_USERNAME`/
  `SPRING_R2DBC_PASSWORD` at an existing instance.

## Setup and Run

From this directory (`todolist-spring/`):

```bash
docker compose up --build
```

or, against a locally running PostgreSQL on `localhost:5432`:

```bash
./mvnw spring-boot:run     # or: mvn spring-boot:run
```

The schema (`tasks` table) is created automatically at startup. The API is
available at **http://localhost:8080** once started. Data is **persisted** in
PostgreSQL and survives application restarts.

> Note: the `db` container also creates the `todolist_test` database used by
> the automated tests (see `db-init/init.sql`). Run tests with
> `docker compose up -d db` first, then `./mvnw test`.

## Quick Validation

```bash
# Create a task
curl -s -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Buy groceries"}'

# List all tasks
curl -s http://localhost:8080/api/todos

# Update a task (partial)
curl -s -X PATCH http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"status":"COMPLETED"}'

# Delete a task
curl -s -X DELETE -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/todos/1
```

## Run with Docker

No need for a local JDK/Maven: the build runs inside a container.

```bash
docker compose up --build
```

The API is available at **http://localhost:8080** once started. Stop it with:

```bash
docker compose down
```

To rebuild after code changes:

```bash
docker compose up --build
```

## Tests

```bash
./mvnw test
```

## CORS

The API allows requests from the dev frontend origin `http://localhost:5173`
for `/api/**` during development.