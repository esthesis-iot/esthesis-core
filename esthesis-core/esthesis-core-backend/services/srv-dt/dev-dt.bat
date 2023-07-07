@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-dt-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59130 ^
  -Ddebug=59131 ^
  -Dquarkus.profile="%PROFILES%"
