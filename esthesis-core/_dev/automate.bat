@echo off
setlocal enabledelayedexpansion
set "CMDER_ROOT=%CMDER_ROOT%"
echo CMDER_ROOT path in system variables
echo %CMDER_ROOT%
set PREFIX_PATH_BE= ..\esthesis-core-backend\services\
set PREFIX_PATH_UI= ..\esthesis-core-ui
set PREFIX_PATH_DOC= ..\esthesis-core-docs

echo Run esthesis back end services, ui and documentation
start "ConEmu" %CMDER_ROOT%\vendor\conemu-maximus5\ConEmu.exe ^
-runlist ^
cmd -cur_console:fn ^
cmd /k cd %PREFIX_PATH_BE%\srv-about ^&^& cmd /k dev-about.bat ^|^|^| cmd -cur_console:s1TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-agent ^&^& cmd /k dev-agent.bat ^|^|^| cmd -cur_console:s1THn ^
cmd /k cd %PREFIX_PATH_BE%\srv-application ^&^& cmd /k dev-application.bat ^|^|^| cmd -cur_console:s2THn ^
cmd /k cd %PREFIX_PATH_BE%\srv-campaign ^&^& cmd /k dev-campaign.bat ^|^|^| cmd -cur_console:s1THn ^
cmd /k cd %PREFIX_PATH_BE%\srv-audit ^&^& cmd /k dev-audit.bat ^|^|^| cmd -cur_console:s3THn ^
cmd /k cd %PREFIX_PATH_BE%\srv-command ^&^& cmd /k dev-command.bat ^|^|^| cmd -cur_console:s4THn ^
cmd /k cd %PREFIX_PATH_BE%\srv-crypto ^&^& cmd /k dev-crypto.bat ^|^|^| cmd -cur_console:s2THn ^
cmd /k cd %PREFIX_PATH_BE%\srv-dataflow ^&^& cmd /k dev-dataflow.bat ^|^|^| cmd -cur_console:s1TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-device ^&^& cmd /k dev-device.bat ^|^|^| cmd -cur_console:s2TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-dt ^&^& cmd /k dev-dt.bat ^|^|^| cmd -cur_console:s3TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-infrastructure ^&^& cmd /k dev-infrastructure.bat ^|^|^| cmd -cur_console:s4TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-kubernetes ^&^& cmd /k dev-kubernetes.bat ^|^|^| cmd -cur_console:s5TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-provisioning ^&^& cmd /k dev-provisioning.bat ^|^|^| cmd -cur_console:s6TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-public-access ^&^& cmd /k dev-public-access.bat ^|^|^| cmd -cur_console:s7TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-security ^&^& cmd /k dev-security.bat ^|^|^| cmd -cur_console:s8TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-settings ^&^& cmd /k dev-settings.bat ^|^|^| cmd -cur_console:s1TVn ^
cmd /k cd %PREFIX_PATH_BE%\srv-tag ^&^& cmd /k dev-tag.bat ^|^|^| cmd -cur_console:s2TVn ^
cmd /k cd %PREFIX_PATH_UI% ^&^& cmd /k npm start ^|^|^| cmd -cur_console:s3TVn ^
cmd /k cd %PREFIX_PATH_DOC% ^&^& cmd /k npm start
