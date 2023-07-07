@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-agent-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59070 ^
  -Ddebug=59071 ^
  -Dquarkus.profile="%PROFILES%"
