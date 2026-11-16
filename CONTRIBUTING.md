# Team workflow

Use `master` for reviewed, merged deliverables. Work in a named branch and keep changes focused. The initial delivery uses `feature/library-api` and `qa/acceptance-suite`; future work can use `feature/<topic>` or `qa/<topic>`.

1. Pick an assigned [issue](https://github.com/amoez2005/library-lending-api/issues) and update the [team board](docs/team-board.md).
2. Create a branch from the current `master`.
3. Make and document the change; add meaningful tests for behavior changes.
4. Run `./mvnw spotless:apply` and `./mvnw verify` (Windows: `mvnw.cmd`). Run the Postman suite for API changes.
5. Commit using your own authorized GitHub identity; never invent another person's contribution.
6. Push the branch and open a pull request. Ask the other team member to review, then merge after checks pass.

The initial deliverable was generated and tested with AI assistance under the repository owner's instruction using the available authorized accounts. It is not evidence of independent student authorship. Preserve this disclosure and follow the course's AI-use policy. Students remain responsible for reviewing and explaining the implementation.

Never commit credentials, local databases, build output or personal tokens. `data/`, `target/`, `.env` and `node_modules/` are ignored.
