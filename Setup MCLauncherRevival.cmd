@echo off
setlocal
cd /d "%~dp0"
title MCLauncherRevival Setup

call :DetectWindows

echo MCLauncherRevival setup
echo ========================
echo.
echo This setup hub helps choose the right launcher path for this PC.
echo It does not install Minecraft game files and it will not ask for a raw Microsoft password.
echo.

if defined IS_XP (
  echo Detected Windows XP / Server 2003 style system.
  echo Recommended path: XP Offline / Classic.
) else (
  echo Detected Windows version: %WINDOWS_VERSION_TEXT%
  echo Recommended path: Windows 7-11 normal launcher.
)

echo.
call :PackageNotice

:MENU
echo.
echo Choose an option:
echo.
echo   1. Auto-detect and start recommended mode
echo   2. Windows 7, 8, 10, 11 normal launcher
echo   3. Windows XP Offline / Classic mode
echo   4. Build MCLauncherRevival.jar from source
echo   5. Open README / setup notes
echo   Q. Quit
echo.
set "SETUP_CHOICE="
set /p "SETUP_CHOICE=Selection [1]: "
if "%SETUP_CHOICE%"=="" set "SETUP_CHOICE=1"

if /I "%SETUP_CHOICE%"=="1" goto AUTO
if /I "%SETUP_CHOICE%"=="2" goto WIN7
if /I "%SETUP_CHOICE%"=="3" goto XP
if /I "%SETUP_CHOICE%"=="4" goto BUILD
if /I "%SETUP_CHOICE%"=="5" goto NOTES
if /I "%SETUP_CHOICE%"=="Q" goto END

echo.
echo I did not understand "%SETUP_CHOICE%".
goto MENU

:AUTO
if defined IS_XP goto XP
goto WIN7

:WIN7
echo.
echo Starting Windows 7-11 setup/launch path...
echo This path can use the Java 8 dependency helper if Java is missing.
echo.
call "%~dp0Start MCLauncherRevival.cmd"
goto END

:XP
echo.
echo Starting Windows XP Offline / Classic path...
echo XP mode does not try modern Java or Minecraft HTTPS downloads by default.
echo Use prepared .minecraft version files for best results.
echo.
call "%~dp0Start MCLauncherRevival XP Offline.cmd"
goto END

:BUILD
echo.
if defined IS_XP (
  echo Building on XP is not the recommended release path.
  echo XP offline mode should run the included MCLauncherRevival.jar from the release ZIP.
  echo.
)
echo Starting build script...
echo.
call "%~dp0build-win7.cmd"
goto END

:NOTES
echo.
echo Opening README.md if Windows has a file association for Markdown files.
echo If it does not open, read README.md and docs\RELEASES.md in this folder.
echo.
if exist "%~dp0README.md" start "" "%~dp0README.md"
goto MENU

:PackageNotice
if exist "%~dp0MCLauncherRevival.jar" (
  echo Found MCLauncherRevival.jar in this folder.
  exit /b 0
)
echo MCLauncherRevival.jar was not found in this folder.
echo.
echo If you downloaded GitHub's green Code -^> Download ZIP or an auto-generated tag/source ZIP,
echo that archive may not include the runnable jar.
echo.
echo For normal play, download the attached GitHub Releases ZIP named like:
echo   MCLauncherRevival-v0.4.0-alpha.zip
echo.
echo Windows 7-11 can try to build the jar from source.
echo Windows XP Offline / Classic mode expects the jar to already be included.
exit /b 0

:DetectWindows
set "IS_XP="
set "WINDOWS_VERSION_TEXT=unknown"
for /f "tokens=*" %%V in ('ver') do set "WINDOWS_VERSION_TEXT=%%V"
echo %WINDOWS_VERSION_TEXT% | find "5.1." >nul
if not errorlevel 1 set "IS_XP=1"
echo %WINDOWS_VERSION_TEXT% | find "5.2." >nul
if not errorlevel 1 set "IS_XP=1"
exit /b 0

:END
echo.
echo Setup finished.
endlocal
