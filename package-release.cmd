@echo off
setlocal
cd /d "%~dp0"

set "RELEASE_VERSION=%~1"
if "%RELEASE_VERSION%"=="" set "RELEASE_VERSION=v0.4.0-alpha"

set "PACKAGE_NAME=MCLauncherRevival-%RELEASE_VERSION%"
set "RELEASE_ROOT=%~dp0..\release"
set "STAGE_DIR=%RELEASE_ROOT%\%PACKAGE_NAME%"
set "ZIP_PATH=%RELEASE_ROOT%\%PACKAGE_NAME%.zip"

echo Preparing %PACKAGE_NAME%
echo.

if not exist "%RELEASE_ROOT%" mkdir "%RELEASE_ROOT%"

echo Building launcher jar...
call build-win7.cmd
if errorlevel 1 (
  echo Build failed. Release package was not created.
  pause
  exit /b 1
)

if not exist "%~dp0MCLauncherRevival.jar" (
  echo MCLauncherRevival.jar is missing after build.
  echo Release package cannot be created without the runnable launcher jar.
  pause
  exit /b 1
)

if exist "%STAGE_DIR%" rmdir /s /q "%STAGE_DIR%"
if exist "%ZIP_PATH%" del /q "%ZIP_PATH%"
mkdir "%STAGE_DIR%"

echo Staging release files...
copy /y "MCLauncherRevival.jar" "%STAGE_DIR%\" >nul
copy /y "README.md" "%STAGE_DIR%\" >nul
copy /y "CHANGELOG.md" "%STAGE_DIR%\" >nul
copy /y "LICENSE" "%STAGE_DIR%\" >nul
copy /y "NOTICE.md" "%STAGE_DIR%\" >nul
copy /y "Setup MCLauncherRevival.cmd" "%STAGE_DIR%\" >nul
copy /y "Start MCLauncherRevival.cmd" "%STAGE_DIR%\" >nul
copy /y "Start MCLauncherRevival XP Offline.cmd" "%STAGE_DIR%\" >nul
copy /y "run-win7.cmd" "%STAGE_DIR%\" >nul
copy /y "build-win7.cmd" "%STAGE_DIR%\" >nul
copy /y "run-linux.sh" "%STAGE_DIR%\" >nul
copy /y "build-linux.sh" "%STAGE_DIR%\" >nul
copy /y "run-macos.sh" "%STAGE_DIR%\" >nul
copy /y "build-macos.sh" "%STAGE_DIR%\" >nul

if exist "%STAGE_DIR%\docs" rmdir /s /q "%STAGE_DIR%\docs"
if exist "%STAGE_DIR%\resources" rmdir /s /q "%STAGE_DIR%\resources"
if exist "%STAGE_DIR%\tools" rmdir /s /q "%STAGE_DIR%\tools"

xcopy /e /i /y "docs" "%STAGE_DIR%\docs" >nul
xcopy /e /i /y "resources" "%STAGE_DIR%\resources" >nul
mkdir "%STAGE_DIR%\tools"
copy /y "tools\download-temurin8-jdk.ps1" "%STAGE_DIR%\tools\" >nul

(
  echo MCLauncherRevival %RELEASE_VERSION%
  echo.
  echo This is the runnable release package.
  echo Do not confuse this package with GitHub source-code or tag ZIP archives.
  echo.
  echo Recommended first run:
  echo   Setup MCLauncherRevival.cmd
  echo.
  echo Start on Windows 7-11:
  echo   Start MCLauncherRevival.cmd
  echo.
  echo Start on Windows XP offline/classic mode:
  echo   Start MCLauncherRevival XP Offline.cmd
  echo.
  echo This project is unofficial alpha software and is not affiliated with Mojang, Microsoft, Xbox, or Minecraft.
) > "%STAGE_DIR%\RELEASE_INFO.txt"

echo Creating ZIP...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Compress-Archive -Path '%STAGE_DIR%' -DestinationPath '%ZIP_PATH%' -Force"
if errorlevel 1 (
  echo Failed to create ZIP package.
  pause
  exit /b 1
)

echo.
echo Final ZIP contents:
powershell -NoProfile -ExecutionPolicy Bypass -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [IO.Compression.ZipFile]::OpenRead('%ZIP_PATH%'); try { $zip.Entries | ForEach-Object { $_.FullName } } finally { $zip.Dispose() }"
if errorlevel 1 (
  echo Failed to inspect ZIP package.
  pause
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [IO.Compression.ZipFile]::OpenRead('%ZIP_PATH%'); try { if (-not ($zip.Entries | Where-Object { ($_.FullName -replace '\\','/') -match '(^|/)MCLauncherRevival\.jar$' })) { exit 2 } } finally { $zip.Dispose() }"
if errorlevel 1 (
  echo Release ZIP does not include MCLauncherRevival.jar.
  echo Release package verification failed.
  pause
  exit /b 1
)

echo.
echo Release ZIP verified: MCLauncherRevival.jar is included.
echo Created:
echo   %ZIP_PATH%
pause
exit /b 0
