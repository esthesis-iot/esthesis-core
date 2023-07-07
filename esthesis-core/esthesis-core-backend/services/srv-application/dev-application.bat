@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-application-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59090 ^
  -Ddebug=59091 ^
  -Dquarkus.profile="%PROFILES%"