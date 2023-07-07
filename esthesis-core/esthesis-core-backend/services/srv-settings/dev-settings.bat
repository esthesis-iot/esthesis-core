@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-settings-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59030 ^
  -Ddebug=59031 ^
  -Dquarkus.profile="%PROFILES%"
