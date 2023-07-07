@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-tag-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59020 ^
  -Ddebug=59021 ^
  -Dquarkus.profile="%PROFILES%"
