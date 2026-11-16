# Shelf · Library Lending API

A working library desk built around a Java REST service. Manage books, search the catalog, borrow a title and return it through the browser, Swagger or Postman.

[Team board](docs/team-board.md) · [API guide](docs/api-guide.md) · [Design and requirements](docs/design.md) · [Testing and demo](docs/testing.md) · [Contribution policy](CONTRIBUTING.md)

## Start in two commands

Install **JDK 21 or newer**. Java 21 is the CI baseline; the project was also verified locally with JDK 25. Maven is downloaded automatically by the checked-in wrapper. The first build needs internet access.

Windows PowerShell:

```powershell
.\scripts\start.cmd
```

In a second terminal, optionally populate six demonstration titles:

```powershell
node scripts/seed-demo.mjs
```

Linux/macOS (with `JAVA_HOME` pointing to a JDK):

```sh
./mvnw spring-boot:run
```

The optional seed command needs Node.js 18+. The API itself needs only a JDK. The `.cmd` starter also works when local PowerShell scripts are disabled.

Open:

| Address | Purpose |
| --- | --- |
| http://localhost:8080 | Browser catalog |
| http://localhost:8080/api | Hypermedia entry point |
| http://localhost:8080/swagger-ui.html | Swagger UI with Try it out |
| http://localhost:8080/v3/api-docs | Live OpenAPI JSON |

The default **H2 file database** lives in `data/` and survives restarts. No external database setup or login is required. This is an educational local demo without authentication: all clients can manage all titles. Stop the application with Ctrl+C.

## Verify

```powershell
# Ensure JAVA_HOME points to JDK 21+ (not an old Java runtime).
.\mvnw.cmd verify
npm ci
npm run format:check
# Keep the application running for this command:
npm run test:api
```

On Linux/macOS, use `./mvnw verify`. `verify` runs unit, repository, HTTP and Cucumber tests, checks Java formatting and generates JavaDoc and coverage reports. `npm run format:check` checks frontend/config formatting. `npm run test:api` runs the Postman collection with Newman and cleans up its test book.

Generated reports:

- `target/surefire-reports/`: JUnit results.
- `target/cucumber/report.html`: acceptance scenarios.
- `target/site/jacoco/index.html`: code coverage.
- `target/reports/apidocs/index.html`: generated JavaDoc.
- `artifacts/newman.json`: Postman CLI results.

GitHub Actions runs both verification and the Postman suite and publishes the reports and runnable JAR as downloadable artifacts. See [Actions](https://github.com/amoez2005/library-lending-api/actions).

## API at a glance

| Method | Resource | Behavior |
| --- | --- | --- |
| GET | `/api` | Discover resources |
| GET | `/api/books?q=clean&page=0&size=12` | Search and page through titles |
| POST | `/api/books` | Create book; 201 with Location |
| GET | `/api/books/{id}` | Read; ETag and conditional 304 |
| PUT | `/api/books/{id}` | Replace bibliographic details |
| DELETE | `/api/books/{id}` | Delete available book; 204 |
| POST | `/api/books/{id}/borrow` | Borrow available book |
| POST | `/api/books/{id}/return` | Return borrowed book |

Example request body:

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884"
}
```

The API is stateless, uses standard HTTP semantics and returns HAL links including the next valid lending action. GET representations can be cached with mandatory revalidation (`Cache-Control: no-cache`, ETag and `If-None-Match`). Mutations use `no-store`. Errors use RFC 9457 problem details. ISBN is a unique 13-digit identifier; ISBN checksum validation is outside this demo's scope.

The assignment calls the highest Richardson stage “level 4.” The published model numbers its **four stages 0–3**; this implementation targets **level 3 (hypermedia controls)**. See [Martin Fowler's explanation](https://martinfowler.com/articles/richardsonMaturityModel.html).

## Team and branches

| Member | Assigned roles | Branch responsibility |
| --- | --- | --- |
| [Ahmed Abdul Moez · amoez2005](https://github.com/amoez2005) | Developer + analyst + documentation | `feature/library-api` |
| [Muhammad Jawad Hassan · Jawad1271](https://github.com/Jawad1271) | Developer + QA | `qa/acceptance-suite` |

`master` contains the merged deliverable. Each account has separately attributed commits, pushed through authenticated access. The initial implementation, tests and documentation are **AI-assisted**; account attribution records the authorized delivery workflow and does not claim that either student manually wrote the generated code. Each student should review, understand and demonstrate their assigned area.

Tasks and owners are tracked on the [online team board](https://github.com/amoez2005/library-lending-api/blob/master/docs/team-board.md) and [GitHub issues](https://github.com/amoez2005/library-lending-api/issues). Manual GUI demonstration and student review remain explicit open tasks.

## PostgreSQL and Docker (optional)

The default H2 setup is sufficient for the assignment. To use PostgreSQL instead:

```powershell
$env:DB_PASSWORD = 'choose-a-local-password'
docker compose up --build
```

Compose starts PostgreSQL and the application. Alternatively run the JAR with `SPRING_PROFILES_ACTIVE=postgres`, `DB_URL`, `DB_USER` and `DB_PASSWORD`. Docker/PostgreSQL are optional paths and are not claimed as locally executed unless recorded in the testing report.

## Repository layout

```text
src/main/java/.../book/        Domain, repository, service, HTTP controller, HAL mapper
src/main/java/.../api/         Entry point, errors, OpenAPI, ETag configuration
src/main/resources/static/    Browser catalog
src/test/                     Unit, database, HTTP and Cucumber acceptance tests
postman/                      Importable collection and local environment
docs/                         Schema, OpenAPI snapshot, design and demonstration guide
.github/workflows/ci.yml      Build, documentation and Postman CI
```
