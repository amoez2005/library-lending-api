param([string]$BaseUrl = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'
$books = @(
    @{ title = 'The Pragmatic Programmer'; author = 'David Thomas & Andrew Hunt'; isbn = '9780135957059' },
    @{ title = 'Clean Code'; author = 'Robert C. Martin'; isbn = '9780132350884' },
    @{ title = 'Designing Data-Intensive Applications'; author = 'Martin Kleppmann'; isbn = '9781449373320' },
    @{ title = 'Refactoring'; author = 'Martin Fowler'; isbn = '9780134757599' },
    @{ title = 'Head First Design Patterns'; author = 'Eric Freeman & Elisabeth Robson'; isbn = '9781492078005' },
    @{ title = 'Effective Java'; author = 'Joshua Bloch'; isbn = '9780134685991' }
)
foreach ($book in $books) {
    try {
        $created = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/books" -ContentType 'application/json' -Body ($book | ConvertTo-Json)
        Write-Output "Added: $($created.title)"
    } catch {
        if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 409) {
            Write-Output "Already present: $($book.title)"
        } else { throw }
    }
}
