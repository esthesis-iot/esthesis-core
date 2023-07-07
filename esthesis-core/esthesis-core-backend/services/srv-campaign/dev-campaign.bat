@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-campaign-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59150 ^
  -Ddebug=59151 ^
  -Dquarkus.profile="%PROFILES%"
