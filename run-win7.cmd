@echo off
setlocal
cd /d "%~dp0"

if exist "%~dp0tools\jdk8\bin\java.exe" (
  set "JAVA_HOME=%~dp0tools\jdk8"
  set "PATH=%~dp0tools\jdk8\bin;%PATH%"
)

if not exist "%~dp0tools\jdk8\bin\java.exe" (
  for /d %%D in ("%~dp0tools\jdk8\*") do (
    if exist "%%~fD\bin\java.exe" (
      set "JAVA_HOME=%%~fD"
      set "PATH=%%~fD\bin;%PATH%"
    )
  )
)

if not exist MCLauncherRevive-modern.jar (
  call build-win7.cmd
  if errorlevel 1 exit /b 1
  if exist "%~dp0tools\jdk8\bin\java.exe" (
    set "JAVA_HOME=%~dp0tools\jdk8"
    set "PATH=%~dp0tools\jdk8\bin;%PATH%"
  )
  for /d %%D in ("%~dp0tools\jdk8\*") do (
    if exist "%%~fD\bin\java.exe" (
      set "JAVA_HOME=%%~fD"
      set "PATH=%%~fD\bin;%PATH%"
    )
  )
)

where java >nul 2>nul
if errorlevel 1 goto NEED_JAVA
goto HAVE_JAVA

:NEED_JAVA
echo Java is required to run this launcher.
echo.
echo I can download a portable Eclipse Temurin 8 JDK from Adoptium now.
echo It will be stored locally in:
echo   %~dp0tools\jdk8
echo.
choice /C YN /M "Download Java JDK 8 now"
if errorlevel 2 (
  echo Launch cancelled. Install Java 8 manually on Windows 7 or newer, or Java 7 / XP-compatible Java 8 on Windows XP, then run this again.
  pause
  exit /b 1
)
if not exist "%~dp0tools" mkdir "%~dp0tools"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\download-temurin8-jdk.ps1" -Destination "%~dp0tools\jdk8"
if errorlevel 1 (
  echo Java download failed.
  echo You can manually install Java 8 on Windows 7 or newer, or Java 7 / XP-compatible Java 8 on Windows XP, and rerun this file.
  pause
  exit /b 1
)
if exist "%~dp0tools\jdk8\bin\java.exe" (
  set "JAVA_HOME=%~dp0tools\jdk8"
  set "PATH=%~dp0tools\jdk8\bin;%PATH%"
)
for /d %%D in ("%~dp0tools\jdk8\*") do (
  if exist "%%~fD\bin\java.exe" (
    set "JAVA_HOME=%%~fD"
    set "PATH=%%~fD\bin;%PATH%"
  )
)

:HAVE_JAVA
where java >nul 2>nul
if errorlevel 1 (
  echo Java still was not found after dependency setup.
  echo Install Java manually, or check tools\jdk8.
  pause
  exit /b 1
)

java %MCLAUNCHER_JAVA_OPTS% -jar MCLauncherRevive-modern.jar
if errorlevel 1 (
  echo Launcher exited with an error.
  pause
)
