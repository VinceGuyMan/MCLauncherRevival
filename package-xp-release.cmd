@echo off
setlocal
cd /d "%~dp0"

set "RELEASE_VERSION=%~1"
if "%RELEASE_VERSION%"=="" set "RELEASE_VERSION=v0.3.0-alpha"

set "PACKAGE_NAME=MCLauncherRevival-%RELEASE_VERSION%-xp-bundled-java"
set "DIST_DIR=%~dp0dist"
set "STAGE_DIR=%DIST_DIR%\%PACKAGE_NAME%"
set "ZIP_PATH=%DIST_DIR%\%PACKAGE_NAME%.zip"

echo Preparing %PACKAGE_NAME%
echo.
echo XP bundled Java packaging does not download Java.
echo It only packages maintainer-provided Java files from tools\java7 or tools\java-installers.
echo.

set "HAS_JAVA7_RUNTIME="
set "HAS_JAVA_INSTALLERS="
if exist "%~dp0tools\java7\bin\java.exe" set "HAS_JAVA7_RUNTIME=1"
if exist "%~dp0tools\java-installers\*.exe" set "HAS_JAVA_INSTALLERS=1"

if not defined HAS_JAVA7_RUNTIME if not defined HAS_JAVA_INSTALLERS (
  echo XP bundled Java package cannot be created because tools\java7\bin\java.exe was not found. Add a verified redistributable Java 7 or XP-compatible Java 8 runtime first.
  echo Or place verified Java installer EXEs at tools\java-installers.
  pause
  exit /b 1
)

if not exist "%~dp0MCLauncherRevival.jar" (
  echo MCLauncherRevival.jar was not found.
  echo Build MCLauncherRevival.jar on Windows 7 or newer before creating the XP bundled-Java package.
  pause
  exit /b 1
)

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
if exist "%STAGE_DIR%" rmdir /s /q "%STAGE_DIR%"
if exist "%ZIP_PATH%" del /q "%ZIP_PATH%"
mkdir "%STAGE_DIR%"

echo Staging XP bundled-Java release files...
copy /y "MCLauncherRevival.jar" "%STAGE_DIR%\" >nul
copy /y "README.md" "%STAGE_DIR%\" >nul
copy /y "LICENSE" "%STAGE_DIR%\" >nul
copy /y "NOTICE.md" "%STAGE_DIR%\" >nul
copy /y "Start MCLauncherRevival XP Offline.cmd" "%STAGE_DIR%\" >nul
copy /y "Start MCLauncherRevival.cmd" "%STAGE_DIR%\" >nul
copy /y "run-win7.cmd" "%STAGE_DIR%\" >nul

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
  echo Start XP offline/classic mode:
  echo   Start MCLauncherRevival XP Offline.cmd
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

powershell -NoProfile -ExecutionPolicy Bypass -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $zip = [IO.Compression.ZipFile]::OpenRead('%ZIP_PATH%'); try { $hasJar = $false; $hasJava = $false; $hasInstaller = $false; foreach ($entry in $zip.Entries) { $name = ($entry.FullName -replace '\\','/'); if ($name -match '(^|/)MCLauncherRevival\.jar$') { $hasJar = $true }; if ($name -match '(^|/)tools/java7/bin/java\.exe$') { $hasJava = $true }; if ($name -match '(^|/)tools/java-installers/[^/]+\.exe$') { $hasInstaller = $true } }; if (-not $hasJar -or (-not $hasJava -and -not $hasInstaller)) { exit 2 } } finally { $zip.Dispose() }"
if errorlevel 1 (
  echo XP bundled-Java ZIP verification failed.
  echo Expected MCLauncherRevival.jar and either tools\java7\bin\java.exe or Java installer EXEs under tools\java-installers.
  pause
  exit /b 1
)

echo.
echo XP bundled-Java ZIP verified.
echo Created:
echo   %ZIP_PATH%
pause
exit /b 0
