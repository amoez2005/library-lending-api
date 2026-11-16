# Verification and demonstration

## Reproduce automated checks

Use JDK 21+ and run `./mvnw verify` (`.\mvnw.cmd verify` on Windows). Tests use in-memory H2, start HTTP servers on random ports and require no running application. Spotless validates Google Java Format; `./mvnw spotless:apply` fixes formatting.

Then start the application and run `npm ci` followed by `npm run test:api`. The collection creates a uniquely identified test book and removes it. Do not aim test runs at an unrelated database or service.

## Test coverage

| Layer | Evidence |
| --- | --- |
| Book and input DTO | Initial state, forbidden duplicate transitions, trimming, updates and null handling |
| Service | Create, ISBN conflict, missing ID, search, update, borrow, return and protected deletion |
| Repository | Generated ID/version, case-insensitive query, unique lookup and version increments |
| HTTP controller / HAL mapper | HTTP statuses, create Location and state-dependent links |
| API configuration / errors | Discovery, safe problem details and ETag configuration |
| Full application over HTTP | CRUD lifecycle, validation, pagination, concurrency, ETag/304, API discovery, frontend asset and OpenAPI endpoints |
| CORS | Swagger Editor POST preflight |
| Cucumber | Borrow/return, duplicate borrowing, protected delete and completed-loan deletion |
| Postman/Newman | 18 requests with 33 assertions including lifecycle, validation, hypermedia and caching |

The generated JaCoCo report is the source of truth for coverage percentages; no hand-written 100% claim is made. Application startup is verified by the real Spring context and packaged-JAR smoke check.

## Course demonstration (students)

1. Show the repository owner, collaborator, assigned issues, branch history and merged pull requests.
2. Run the service and populate demonstration titles with `scripts/seed-demo.ps1`.
3. Open the frontend, search for a title and create/edit a book.
4. In Swagger Editor, import `docs/openapi.json`. Inspect the schema and execute requests if the browser permits localhost access. The local Swagger UI is also available.
5. In Postman, import the collection and environment, run the numbered collection and show real status/body/header results.
6. Show `GET /api`, follow links to a book and point out how the borrow relation changes to return after borrowing.
7. Show ETag, resend `If-None-Match`, and explain the 304 response and mandatory cache revalidation.
8. Demonstrate an invalid ISBN, duplicate ISBN and double borrowing. Explain 400 versus 409.
9. Restart the service and show that the catalog persists.
10. Run automated verification and open the Cucumber, JavaDoc and coverage reports.
11. Explain each member's assigned components and the documented AI assistance.

Capture actual GUI screenshots during steps 4–5. No GUI screenshots are supplied by this delivery because a browser automation session was unavailable. HTTP/Newman execution does not imply that Postman GUI or Swagger Editor was manually exercised.

## Boundaries

The tested local path uses H2 and Java. Docker/PostgreSQL configuration is provided but is not part of the claimed local validation. The UI is served and its assets are checked over HTTP; visual and interactive browser QA remains a student review task. Fonts use Google Fonts with local sans-serif fallbacks; the API and catalog remain usable without that font download.

See `verification.json` for the recorded automated counts and environment. GitHub Actions artifacts provide independent CI logs and generated reports for each run.
