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

## CI/CD

`.github/workflows/ci.yml` runs on every pull request and on push to `main`:

| Job | What it does |
|---|---|
| `test` | `mvn -B verify` (compile and run all tests) |
| `docker` | builds the image, scans it with Trivy, pushes it to GHCR (push to `main` only) |
| `deploy` | calls the Render deploy hook (push to `main` only) |

`.github/workflows/security.yml` adds `secrets` (gitleaks scan of the whole history) and `codeql`
(static analysis of the Java code). It also runs every Monday.

GitHub secret `RENDER_DEPLOY_HOOK_URL` is the Render deploy hook. Without it, `deploy` is skipped.
Dependabot sends one grouped update PR per month (Maven, Docker, Actions).

## Environment variables

See `.env.example`. `TMDB_TOKEN`, `DB_PASS` and `JWT_SECRET` are secrets: set them on the host, never commit them.
