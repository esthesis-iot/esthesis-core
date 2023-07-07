@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-public-access-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59160 ^
  -Ddebug=59161 ^
  -Dquarkus.profile="%PROFILES%"
