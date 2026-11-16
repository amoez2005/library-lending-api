@echo off
setlocal
cd /d "%~dp0.."
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto java_ready
for /f "delims=" %%J in ('where javac 2^>nul') do (
    for %%D in ("%%~dpJ..") do set "JAVA_HOME=%%~fD"
    goto java_ready
)
:java_ready
if not defined JAVA_HOME (
    echo Install JDK 21 or newer and set JAVA_HOME.
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
call mvnw.cmd spring-boot:run
exit /b %ERRORLEVEL%
