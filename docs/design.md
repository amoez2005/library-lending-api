# Design and assignment mapping

## Scope and rules

Shelf models a small library catalog. A book has a generated ID, lock version, title, author, unique ISBN-13 and a borrowed flag. A title starts available. Borrowing an available book makes it borrowed; returning it makes it available. Repeated transitions and deleting an active loan return 409. PUT replaces bibliographic data while preserving the loan state.

`docs/database.sql` describes the relational schema. JPA creates or updates the local schema. H2 is a real embedded relational DBMS with a persistent file; a separate PostgreSQL profile is included. Tests use isolated in-memory H2 databases. Test code never connects to the default file database.

There are no user accounts, borrower identities, due dates or fines in this bounded demo. The public catalog contains bibliographic data only. The API has no login/session requirement and never stores a client workflow in an HTTP session.

## Request path and SOLID

HTTP controller → `BookService` → `BookRepository` → H2/PostgreSQL. `BookModelAssembler` maps domain data into HAL representations. `ApiExceptionHandler` maps safe domain failures to HTTP problem details. `Book` owns the lending state transitions.

- **Single responsibility:** controllers handle HTTP; services orchestrate transactions and rules; repositories handle persistence; the assembler owns links.
- **Open/closed:** Spring Data supplies the repository implementation; alternate persistence implementations can satisfy the same interface without changing HTTP behavior.
- **Liskov substitution:** tests substitute a Mockito repository while preserving the service's persistence contract; actual JPA behavior is verified separately.
- **Interface segregation:** consumers use the persistence abstraction and concrete focused operations; there is no large application-wide service interface.
- **Dependency inversion:** business orchestration receives the repository abstraction through constructor injection. Domain transitions do not depend on HTTP.

An entity version prevents silent lost updates from simultaneous transactions. A database unique constraint protects ISBN uniqueness even if two creates pass the initial check. These conflicts are translated to 409. Sequential PUT requests use last-write-wins semantics; the API does not require `If-Match` for writes. A retry after an optimistic lock conflict must reread the current resource.

## REST and caching

Resources use noun URIs, standard methods/status codes and a uniform JSON/HAL representation. `/api` links to the collection. Collections link to creation and page navigation. Every book links to itself, its collection, update and its currently legal lending transition. HAL relations make the next loan action discoverable; link `type` describes its media type, and the OpenAPI contract defines the HTTP methods for the relations. The service uses a client/server design and does not depend on a direct deployment topology.

The Richardson model defines levels 0, 1, 2 and 3. The assignment's “level 4” is interpreted as its fourth stage: **level 3, hypermedia controls**. This correction is documented rather than claiming a nonexistent numbered stage. Reference: [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html).

API GET responses use `Cache-Control: no-cache`: caches may store a representation but must revalidate it before reuse. A response-body ETag makes identical GETs conditional; matching `If-None-Match` returns 304. A mutation changes the representation and its ETag. Collection responses also get representation ETags. Writes and explicitly mapped errors use `no-store`.

The shallow ETag filter reduces transfer size, not database work: it computes the representation before validating. This deliberately favors correctness and simplicity for a small assignment. There is no hidden session or cross-request application cache.

Swagger Editor origin `https://editor.swagger.io` is allowed for `/api/**`. Same-origin Swagger UI requires no CORS configuration. No wildcard credentialed CORS is enabled.

## Requirements checklist

| Requirement | Evidence |
| --- | --- |
| Two members with mixed roles | README team table and assigned issues |
| Online task board | `docs/team-board.md` on GitHub, linked assigned issues |
| Branches, commits and master merge | Feature branch, QA branch, pull requests and Git history |
| DBMS and schema | H2 file store, JPA entity, `database.sql`, PostgreSQL profile |
| Fully functional REST service | CRUD, search, pagination and lending endpoints |
| Cacheable, stateless, highest RMM stage | Conditional HTTP tests, no session cookies, HAL transition links |
| Swagger Editor / Swagger UI | OpenAPI snapshot, live contract, CORS, demo instructions |
| Postman | Importable collection, environment and Newman execution |
| Frontend | HTML/CSS/JavaScript catalog served by Spring Boot |
| Automated acceptance testing | Cucumber feature scenarios and Postman assertions |
| Formatting and naming | Google Java Format enforced by Spotless; Java conventions |
| Tests for application components | Domain/service/controller/assembler/config/error unit tests, repository test, HTTP/context tests |
| Documentation and generated JavaDoc | Class/method comments; JavaDoc bound to `verify` |
| SOLID | Layer responsibilities and constructor-injected repository |

The executable bootstrap is validated by starting the packaged application and by Spring context tests; it has no business logic to duplicate in a unit test. The repository interface is verified through its generated JPA implementation. Manual GUI evidence and individual student understanding cannot be replaced by automated tests; they stay on the board until completed.
