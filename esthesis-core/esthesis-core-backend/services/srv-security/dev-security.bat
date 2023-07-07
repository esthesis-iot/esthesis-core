@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-security-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59170 ^
  -Ddebug=59171 ^
  -Dquarkus.profile="%PROFILES%"
