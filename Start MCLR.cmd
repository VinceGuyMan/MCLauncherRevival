@echo off
setlocal
cd /d "%~dp0"
call "%~dp0scripts\run-win7.cmd" %*
set "LAUNCH_EXIT=%ERRORLEVEL%"
if not "%LAUNCH_EXIT%"=="0" (
  echo.
  echo MCLauncherRevival exited with code %LAUNCH_EXIT%.
  echo Press any key to close this window.
  pause >nul
)
endlocal & exit /b %LAUNCH_EXIT%
