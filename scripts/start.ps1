param([int]$Port = 8080)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $compiler = Get-Command javac -ErrorAction SilentlyContinue
    if ($compiler) { $env:JAVA_HOME = Split-Path (Split-Path $compiler.Source -Parent) -Parent }
}
if (-not $env:JAVA_HOME) { throw 'Install a JDK 21 or newer and set JAVA_HOME.' }
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:PORT = "$Port"
& .\mvnw.cmd spring-boot:run
exit $LASTEXITCODE
