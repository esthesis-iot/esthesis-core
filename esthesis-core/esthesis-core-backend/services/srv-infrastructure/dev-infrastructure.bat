@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-infrastructure-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59110 ^
  -Ddebug=59111 ^
  -Dquarkus.profile="%PROFILES%"
