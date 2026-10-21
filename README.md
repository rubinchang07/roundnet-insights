# Roundnet Insights

Spring Boot backend for match analytics on data exported from Roundnet Analyzer.

## Requirements

- Java 25
- Docker (for the local database and for tests)

## Run the tests

Tests start a throwaway PostgreSQL in Docker (Testcontainers), so nothing else needs to be running.

```bash
./mvnw test
```

## Run the app locally

```bash
docker compose up -d
./mvnw spring-boot:run
```

Flyway creates the schema from `src/main/resources/db/migration` on startup.

## Decisions

Architecture decisions are recorded in [docs/decisions](docs/decisions).
