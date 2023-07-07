@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-command-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59080 ^
  -Ddebug=59081 ^
  -Dquarkus.profile="%PROFILES%"

