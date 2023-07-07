@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-audit-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59140 ^
  -Ddebug=59141 ^
  -Dquarkus.profile="%PROFILES%"
