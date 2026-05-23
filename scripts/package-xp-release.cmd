@echo off
setlocal
set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
cd /d "%ROOT_DIR%"

set "RELEASE_VERSION=%~1"
if "%RELEASE_VERSION%"=="" set "RELEASE_VERSION=v0.7.0-alpha"

set "PACKAGE_NAME=MCLauncherRevival-%RELEASE_VERSION%-xp-bundled-java"
set "DIST_DIR=%ROOT_DIR%\dist"
set "STAGE_PARENT=%DIST_DIR%\_staging"
set "STAGE_DIR=%STAGE_PARENT%\%PACKAGE_NAME%"
set "ZIP_PATH=%DIST_DIR%\%PACKAGE_NAME%.zip"

echo Preparing %PACKAGE_NAME%
echo.
echo XP bundled Java packaging does not download Java.
echo It only packages maintainer-provided Java files from tools\java7 or tools\java-installers.
echo.

set "HAS_JAVA7_RUNTIME="
set "HAS_JAVA_INSTALLERS="
if exist "%ROOT_DIR%\tools\java7\bin\java.exe" set "HAS_JAVA7_RUNTIME=1"
if exist "%ROOT_DIR%\tools\java-installers\*.exe" set "HAS_JAVA_INSTALLERS=1"

if not defined HAS_JAVA7_RUNTIME if not defined HAS_JAVA_INSTALLERS (
  echo XP bundled Java package cannot be created because tools\java7\bin\java.exe was not found. Add a verified redistributable Java 7 or XP-compatible Java 8 runtime first.
  echo Or place verified Java installer EXEs at tools\java-installers.
  pause
  exit /b 1
)

if not exist "%ROOT_DIR%\MCLauncherRevival.jar" (
  echo MCLauncherRevival.jar was not found.
  echo Build MCLauncherRevival.jar on Windows 7 or newer before creating the XP bundled-Java package.
  pause
  exit /b 1
)

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
if not exist "%STAGE_PARENT%" mkdir "%STAGE_PARENT%"
if exist "%STAGE_DIR%" rmdir /s /q "%STAGE_DIR%"
if exist "%STAGE_DIR%" (
  echo XP release staging folder is locked:
  echo   %STAGE_DIR%
  echo Close any Explorer windows or terminals inside that folder, then try again.
  pause
  exit /b 1
)
if exist "%ZIP_PATH%" del /q "%ZIP_PATH%"
mkdir "%STAGE_DIR%"
mkdir "%STAGE_DIR%\scripts"

echo Staging XP bundled-Java release files...
copy /y "MCLauncherRevival.jar" "%STAGE_DIR%" >nul
copy /y "README.md" "%STAGE_DIR%" >nul
copy /y "LICENSE" "%STAGE_DIR%" >nul
copy /y "NOTICE.md" "%STAGE_DIR%" >nul
copy /y "Setup MCLR.cmd" "%STAGE_DIR%" >nul
copy /y "Start MCLR XP.cmd" "%STAGE_DIR%" >nul
copy /y "Start MCLR.cmd" "%STAGE_DIR%" >nul
copy /y "scripts\run-win.cmd" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\banner.txt" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\boot-card-win.txt" "%STAGE_DIR%\scripts" >nul
copy /y "scripts\boot-card-xp.txt" "%STAGE_DIR%\scripts" >nul

xcopy /e /i /y "docs" "%STAGE_DIR%\docs" >nul
xcopy /e /i /y "resources" "%STAGE_DIR%\resources" >nul
if defined HAS_JAVA7_RUNTIME xcopy /e /i /y "tools\java7" "%STAGE_DIR%\tools\java7" >nul
if defined HAS_JAVA_INSTALLERS xcopy /e /i /y "tools\java-installers" "%STAGE_DIR%\tools\java-installers" >nul

(
  echo MCLauncherRevival %RELEASE_VERSION% XP bundled-Java package
  echo.
  echo This package is for Windows XP offline/classic use.
  echo It includes MCLauncherRevival.jar and maintainer-provided Java files.
  echo.
  echo If tools\java7 is present, the XP launcher will use it first.
  echo If tools\java-installers is present, the XP launcher can ask before running a bundled installer.
  echo.
  echo Recommended first run:
  echo   Setup MCLR.cmd
  echo.
  echo Start XP offline/classic mode:
  echo   Start MCLR XP.cmd
  echo.
  echo Internal helper scripts live under scripts\.
  echo.
  echo Bundled Java is third-party software under its own license/readme files.
  echo Old Java runtimes are not secure for general browsing or production use.
  echo.
  echo This project is unofficial alpha software and is not affiliated with Mojang, Microsoft, Xbox, or Minecraft.
) > "%STAGE_DIR%\RELEASE_INFO_XP.txt"

echo Creating ZIP...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Compress-Archive -Path '%STAGE_DIR%' -DestinationPath '%ZIP_PATH%' -Force"
if errorlevel 1 (
  echo Failed to create XP bundled-Java ZIP package.
  pause
  exit /b 1
)

echo.
echo Final ZIP contents:
powershell -NoProfile -ExecutionPolicy Bypass -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [IO.Compression.ZipFile]::OpenRead('%ZIP_PATH%'); try { $zip.Entries | ForEach-Object { $_.FullName } } finally { $zip.Dispose() }"
if errorlevel 1 (
  echo Failed to inspect XP bundled-Java ZIP package.
  pause
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [IO.Compression.ZipFile]::OpenRead('%ZIP_PATH%'); try { $hasJar = $false; $hasJava = $false; $hasInstaller = $false; $hasThemes = $false; $hasBanner = $false; foreach ($entry in $zip.Entries) { $name = ($entry.FullName -replace '\\','/'); if ($name -match '(^|/)MCLauncherRevival\.jar$') { $hasJar = $true }; if ($name -match '(^|/)tools/java7/bin/java\.exe$') { $hasJava = $true }; if ($name -match '(^|/)tools/java-installers/[^/]+\.exe$') { $hasInstaller = $true }; if ($name -match '(^|/)resources/net/minecraft/themes/beta\.png$') { $hasThemes = $true }; if ($name -match '(^|/)scripts/banner\.txt$') { $hasBanner = $true } }; if (-not $hasJar -or -not $hasThemes -or -not $hasBanner -or (-not $hasJava -and -not $hasInstaller)) { exit 2 } } finally { $zip.Dispose() }"
if errorlevel 1 (
  echo XP bundled-Java ZIP verification failed.
  echo Expected MCLauncherRevival.jar, theme resources, scripts\banner.txt, and either tools\java7\bin\java.exe or Java installer EXEs under tools\java-installers.
  pause
  exit /b 1
)

echo.
echo XP bundled-Java ZIP verified.
rmdir /s /q "%STAGE_DIR%" >nul 2>nul
if exist "%STAGE_DIR%" (
  echo [WARN] Could not remove temporary staging folder:
  echo        %STAGE_DIR%
)
echo Created:
echo   %ZIP_PATH%
pause
exit /b 0


