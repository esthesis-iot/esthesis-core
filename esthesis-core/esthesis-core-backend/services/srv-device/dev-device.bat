@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-device-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59010 ^
  -Ddebug=59011 ^
  -Dquarkus.profile="%PROFILES%"
