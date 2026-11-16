# API exploration

Start at `GET http://localhost:8080/api` and follow its `books` link. Collection bodies have `items`, `page`, `size`, `totalElements`, `totalPages` and `_links`. Empty searches return an empty `items` array. Page indexes start at 0, page size is 1–100, and search phrases are limited to 160 characters. Search matches a title case-insensitively.

## Swagger UI

Open http://localhost:8080/swagger-ui.html. Expand `POST /api/books`, click **Try it out**, enter a body and click **Execute**. Observe the 201 status and Location header. Use the ID to GET, borrow, return and delete the book. Deliberately borrow twice to observe 409.

## Swagger Editor

1. Open https://editor.swagger.io/.
2. Choose **File → Import file** and select `docs/openapi.json`.
3. Confirm the contract loads and the server is `http://localhost:8080`.
4. Execute create/read/borrow/return/delete requests and inspect responses.

The API allows the Swagger Editor origin. Modern browsers may additionally prompt for local network access or restrict HTTPS-to-localhost requests. If the browser blocks the Editor, record that limitation and use the local Swagger UI to execute requests; the contract can still be inspected in Editor. Do not claim a GUI execution without performing it.

The live contract at `/v3/api-docs` is the authoritative generated schema. `docs/openapi.json` is an exported snapshot with the local server URL and explicit response codes added for offline exploration. Refresh it after changing endpoints.

## Postman

Import `postman/Shelf.postman_collection.json` and `postman/Local.postman_environment.json`. Select **Shelf Local**, then run the collection in its numbered order. The first request creates a unique ISBN variable; the create response stores the resource ID; the read response stores the ETag. Later requests test caching, validation, loan transitions and cleanup.

Newman's equivalent execution is:

```sh
npm ci
npm run test:api
```

The application must be running. To use a different port, change the environment's `baseUrl`. Run collections sequentially; simultaneous runs sharing the same Postman variables are outside this demo workflow.

## Responses and errors

| Status | Meaning |
| --- | --- |
| 200 | Successful retrieval, update or transition |
| 201 | Book created; inspect Location |
| 204 | Book deleted; no response body |
| 304 | Cached GET representation still current |
| 400 | Malformed JSON, invalid fields or invalid pagination |
| 404 | Book ID does not exist |
| 409 | Duplicate ISBN, invalid lending state or concurrent modification |
| 405 / 415 | Unsupported HTTP method / unsupported request media type |

Domain errors return `application/problem+json` with status, title, detail and instance. Field validation adds an `errors` array. Client inputs are never returned as raw SQL errors. The API validates ISBN length/digits and uniqueness, not its checksum or real-world registration.

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Book is already borrowed.",
  "instance": "/api/books/1/borrow"
}
```

## Conditional GET example (PowerShell)

```powershell
$first = Invoke-WebRequest http://localhost:8080/api/books
$first.Headers['ETag']
# A matching validator produces 304; some PowerShell versions report 304 as an exception.
curl.exe -i http://localhost:8080/api/books -H "If-None-Match: $($first.Headers['ETag'])"
```
