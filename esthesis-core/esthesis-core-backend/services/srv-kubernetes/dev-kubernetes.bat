@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-kubernetes-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59050 ^
  -Ddebug=59051 ^
  -Dquarkus.profile="%PROFILES%"
