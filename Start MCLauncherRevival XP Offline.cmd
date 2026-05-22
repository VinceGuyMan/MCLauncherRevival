@echo off
setlocal
cd /d "%~dp0"
title MCLauncherRevival XP Offline
set "MCLAUNCHER_XP_MODE=1"
set "MCLAUNCHER_JAVA_OPTS=-Dmclauncher.xpMode=true -Dhttps.protocols=TLSv1.2,TLSv1.1,TLSv1"
call "%~dp0scripts\run-win7.cmd"
set "LAUNCH_EXIT=%ERRORLEVEL%"
if not "%LAUNCH_EXIT%"=="0" (
  echo.
  echo MCLauncherRevival XP Offline exited with code %LAUNCH_EXIT%.
  echo Press any key to close this window.
  pause >nul
)
endlocal & exit /b %LAUNCH_EXIT%
