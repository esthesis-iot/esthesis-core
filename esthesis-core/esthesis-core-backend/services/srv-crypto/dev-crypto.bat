@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-crypto-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59040 ^
  -Ddebug=59041 ^
  -Dquarkus.profile="%PROFILES%"
