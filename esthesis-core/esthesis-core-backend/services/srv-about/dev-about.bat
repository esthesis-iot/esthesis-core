@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-about-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59120 ^
  -Ddebug=59121 ^
  -Dquarkus.profile="%PROFILES%"

