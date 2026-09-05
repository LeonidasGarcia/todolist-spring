# todolist-spring

Reactive REST API for the TodoList project. Built with Spring Boot + WebFlux
(Java 21, Maven) and exclusive in-memory storage. Runs on `localhost:8080`.

## Prerequisites

- **JDK 21** (LTS) — required by Spring Boot 3.x and Java records
- **Maven 3.6.3+** (or use the Maven Wrapper `./mvnw`)

## Setup and Run

From this directory (`todolist-spring/`):

```bash
./mvnw spring-boot:run
```

or with a system Maven installation:

```bash
mvn spring-boot:run
```

The API is available at **http://localhost:8080** once started.

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