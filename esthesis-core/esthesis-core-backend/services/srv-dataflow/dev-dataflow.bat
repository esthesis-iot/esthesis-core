@echo off

set PROFILES=dev

if "%~1" neq "" (
  set PROFILES=%PROFILES%,%~1
  echo Activating profiles: %PROFILES%
)

cd srv-dataflow-impl || exit
mvnw.cmd quarkus:dev ^
  -Dquarkus.http.port=59060 ^
  -Ddebug=59061 ^
  -Dquarkus.profile="%PROFILES%"
