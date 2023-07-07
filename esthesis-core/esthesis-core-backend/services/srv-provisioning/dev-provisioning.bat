@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-provisioning-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59100 ^
  -Ddebug=59101 ^
  -Dquarkus.profile="%PROFILES%"
