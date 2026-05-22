@echo off
setlocal
set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
cd /d "%ROOT_DIR%"

set "RELEASE_VERSION=%~1"
if "%RELEASE_VERSION%"=="" set "RELEASE_VERSION=v0.5.7-alpha"

set "PACKAGE_NAME=MCLauncherRevival-%RELEASE_VERSION%"
set "RELEASE_ROOT=%ROOT_DIR%\..\release"
set "STAGE_PARENT=%RELEASE_ROOT%\_staging"
set "STAGE_DIR=%STAGE_PARENT%\%PACKAGE_NAME%"
set "ZIP_PATH=%RELEASE_ROOT%\%PACKAGE_NAME%.zip"

echo Preparing %PACKAGE_NAME%
echo.

if not exist "%RELEASE_ROOT%" mkdir "%RELEASE_ROOT%"
if not exist "%STAGE_PARENT%" mkdir "%STAGE_PARENT%"

echo Building launcher jar...
call "%ROOT_DIR%\scripts\build-win7.cmd"
if errorlevel 1 (
  echo Build failed. Release package was not created.
  pause
  exit /b 1
)

if not exist "%ROOT_DIR%\MCLauncherRevival.jar" (
  echo MCLauncherRevival.jar is missing after build.
  echo Release package cannot be created without the runnable launcher jar.
  pause
  exit /b 1
)

if exist "%STAGE_DIR%" rmdir /s /q "%STAGE_DIR%"
if exist "%STAGE_DIR%" (
  echo Release staging folder is locked:
  echo   %STAGE_DIR%
  echo Close any Explorer windows or terminals inside that folder, then try again.
  pause
  exit /b 1
)
if exist "%ZIP_PATH%" del /q "%ZIP_PATH%"
mkdir "%STAGE_DIR%"
mkdir "%STAGE_DIR%\scripts"

echo Staging release files...
copy /y "MCLauncherRevival.jar" "%STAGE_DIR%" >nul
copy /y "README.md" "%STAGE_DIR%" >nul
copy /y "CHANGELOG.md" "%STAGE_DIR%" >nul
copy /y "LICENSE" "%STAGE_DIR%" >nul
copy /y "NOTICE.md" "%STAGE_DIR%" >nul
copy /y "Setup MCLauncherRevival.cmd" "%STAGE_DIR%" >nul
copy /y "Start MCLauncherRevival.cmd" "%STAGE_DIR%" >nul
copy /y "Start MCLauncherRevival XP Offline.cmd" "%STAGE_DIR%" >nul
copy /y "scripts\run-win7.cmd" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\build-win7.cmd" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\banner.txt" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\boot-card-win7.txt" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\boot-card-xp.txt" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\run-linux.sh" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\build-linux.sh" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\run-macos.sh" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\build-macos.sh" "%STAGE_DIR%\scripts" >nul

if exist "%STAGE_DIR%\docs" rmdir /s /q "%STAGE_DIR%\docs"
if exist "%STAGE_DIR%\resources" rmdir /s /q "%STAGE_DIR%\resources"
if exist "%STAGE_DIR%\tools" rmdir /s /q "%STAGE_DIR%\tools"

xcopy /e /i /y "docs" "%STAGE_DIR%\docs" >nul
xcopy /e /i /y "resources" "%STAGE_DIR%\resources" >nul
mkdir "%STAGE_DIR%\tools"
copy /y "tools\download-temurin8-jdk.ps1" "%STAGE_DIR%\tools" >nul

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
  echo Internal helper scripts live under scripts\.
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

powershell -NoProfile -ExecutionPolicy Bypass -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [IO.Compression.ZipFile]::OpenRead('%ZIP_PATH%'); try { if (-not ($zip.Entries | Where-Object { ($_.FullName -replace '\\','/') -match '(^|/)MCLauncherRevival\.jar$' })) { exit 2 }; if (-not ($zip.Entries | Where-Object { ($_.FullName -replace '\\','/') -match '(^|/)scripts/run-win7\.cmd$' })) { exit 3 }; if (-not ($zip.Entries | Where-Object { ($_.FullName -replace '\\','/') -match '(^|/)scripts/banner\.txt$' })) { exit 5 }; if (-not ($zip.Entries | Where-Object { ($_.FullName -replace '\\','/') -match '(^|/)resources/net/minecraft/themes/beta\.png$' })) { exit 4 } } finally { $zip.Dispose() }"
if errorlevel 1 (
  echo Release ZIP verification failed.
  echo Expected MCLauncherRevival.jar, scripts\run-win7.cmd, scripts\banner.txt, and theme resources.
  pause
  exit /b 1
)

echo.
echo Release ZIP verified: MCLauncherRevival.jar, scripts\run-win7.cmd, scripts\banner.txt, and theme resources are included.
rmdir /s /q "%STAGE_DIR%" >nul 2>nul
if exist "%STAGE_DIR%" (
  echo [WARN] Could not remove temporary staging folder:
  echo        %STAGE_DIR%
)
echo Created:
echo   %ZIP_PATH%
pause
exit /b 0


