/** Populate a running local Shelf API with a small, repeatable demo catalog. */
const baseUrl = process.argv[2] || "http://localhost:8080";
const books = [
  ["The Pragmatic Programmer", "David Thomas & Andrew Hunt", "9780135957059"],
  ["Clean Code", "Robert C. Martin", "9780132350884"],
  [
    "Designing Data-Intensive Applications",
    "Martin Kleppmann",
    "9781449373320",
  ],
  ["Refactoring", "Martin Fowler", "9780134757599"],
  [
    "Head First Design Patterns",
    "Eric Freeman & Elisabeth Robson",
    "9781492078005",
  ],
  ["Effective Java", "Joshua Bloch", "9780134685991"],
];
for (const [title, author, isbn] of books) {
  const response = await fetch(`${baseUrl}/api/books`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title, author, isbn }),
  });
  if (response.status === 409) {
    console.log(`Already present: ${title}`);
  } else if (response.status === 201) {
    console.log(`Added: ${title}`);
  } else {
    throw new Error(
      `Could not add ${title}: ${response.status} ${await response.text()}`,
    );
  }
}
