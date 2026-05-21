@echo off
setlocal
cd /d "%~dp0"
title MCLauncherRevival XP Offline
set "MCLAUNCHER_XP_MODE=1"
set "MCLAUNCHER_JAVA_OPTS=-Dmclauncher.xpMode=true -Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1"
call "%~dp0scripts\run-win7.cmd"