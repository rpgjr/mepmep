# mepmep (backend)

Spring Boot 3.5 / Java 25 API for the mepmep movie site: JWT login, TMDB proxy,
favorites, to-watch list and continue-watching. Database: MySQL.
The React app lives in the sibling repo `mepmep-ui`.

## Run locally

1. MySQL running. The `mepmep` database and tables are created on first start.
2. Settings:
   ```
   cp .env.example .env     # fill TMDB_TOKEN and DB_PASS. Never commit .env
   set -a; source .env; set +a
   mvn spring-boot:run
   ```
   Listens on `PORT` (default 8080).

## Test

`mvn test` (in-memory H2, no MySQL needed)

## Deploy

`Dockerfile` builds a container. Set the variables from `.env.example` on the host.
`CORS_ORIGINS` must be the URL of the deployed `mepmep-ui`.
