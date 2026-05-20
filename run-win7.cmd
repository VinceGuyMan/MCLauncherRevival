@echo off
setlocal
cd /d "%~dp0"

if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  echo XP offline mode detected.
  echo Online login and fresh downloads are disabled/best-effort on XP.
)

call :FindJava

if exist MCLauncherRevival.jar (
  if /I "%MCLAUNCHER_XP_MODE%"=="1" echo Using existing MCLauncherRevival.jar.
) else (
  if /I "%MCLAUNCHER_XP_MODE%"=="1" (
    echo MCLauncherRevival.jar was not found.
    echo You may have downloaded GitHub's source-code ZIP instead of the release package.
    echo Download the attached release ZIP from GitHub Releases, or build the jar on Windows 7+ and copy it here.
    echo XP offline mode runs the existing jar and does not build/download Java automatically.
    pause
    exit /b 1
  )
  call build-win7.cmd
  if errorlevel 1 exit /b 1
  call :FindJava
)

if not defined JAVA_EXE goto NEED_JAVA

if /I "%JAVA_SOURCE%"=="java7" echo Using bundled Java runtime: tools\java7
echo Java runtime found: %JAVA_EXE%
"%JAVA_EXE%" %MCLAUNCHER_JAVA_OPTS% -jar MCLauncherRevival.jar
if errorlevel 1 (
  echo Launcher exited with an error.
  pause
)
exit /b 0

:NEED_JAVA
echo Java runtime not found.
if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  echo Windows XP offline/classic mode needs Java 7 or an XP-compatible Java 8 runtime.
  echo Use the XP bundled-Java release package, or place Java at tools\java7.
  echo Expected runtime path: tools\java7\bin\java.exe
  pause
  exit /b 1
)

echo Java is required to run this launcher.
echo.
echo I can download a portable Eclipse Temurin 8 JDK from Adoptium now.
echo It will be stored locally in:
echo   %~dp0tools\jdk8
echo.
set "DOWNLOAD_JDK="
set /p "DOWNLOAD_JDK=Download Java JDK 8 now? [Y/N] "
if /I not "%DOWNLOAD_JDK%"=="Y" (
  echo Launch cancelled. Install Java 8 manually, then run this again.
  pause
  exit /b 1
)
if not exist "%~dp0tools" mkdir "%~dp0tools"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\download-temurin8-jdk.ps1" -Destination "%~dp0tools\jdk8"
if errorlevel 1 (
  echo Java JDK 8 still was not found after dependency setup.
  echo Install Java JDK 8 manually, or place/extract it at tools\jdk8.
  pause
  exit /b 1
)
call :FindJava
if not defined JAVA_EXE (
  echo Java JDK 8 still was not found after dependency setup.
  echo Install Java JDK 8 manually, or place/extract it at tools\jdk8.
  pause
  exit /b 1
)
if /I "%JAVA_SOURCE%"=="java7" echo Using bundled Java runtime: tools\java7
echo Java runtime found: %JAVA_EXE%
"%JAVA_EXE%" %MCLAUNCHER_JAVA_OPTS% -jar MCLauncherRevival.jar
if errorlevel 1 (
  echo Launcher exited with an error.
  pause
)
exit /b 0

:FindJava
set "JAVA_EXE="
set "JAVA_SOURCE="
if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  if exist "%~dp0tools\java7\bin\java.exe" (
    set "JAVA_HOME=%~dp0tools\java7"
    set "PATH=%~dp0tools\java7\bin;%PATH%"
    set "JAVA_EXE=%~dp0tools\java7\bin\java.exe"
    set "JAVA_SOURCE=java7"
  )
)
if exist "%~dp0tools\jdk8\bin\java.exe" (
  if not defined JAVA_EXE (
    set "JAVA_HOME=%~dp0tools\jdk8"
    set "PATH=%~dp0tools\jdk8\bin;%PATH%"
    set "JAVA_EXE=%~dp0tools\jdk8\bin\java.exe"
  )
)
if not defined JAVA_EXE (
  for /d %%D in ("%~dp0tools\jdk8\*") do (
    if exist "%%~fD\bin\java.exe" (
      set "JAVA_HOME=%%~fD"
      set "PATH=%%~fD\bin;%PATH%"
      set "JAVA_EXE=%%~fD\bin\java.exe"
    )
  )
)
if not defined JAVA_EXE (
  if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
  )
)
if not defined JAVA_EXE (
  for %%P in (java.exe) do (
    if not "%%~$PATH:P"=="" set "JAVA_EXE=%%~$PATH:P"
  )
)
exit /b 0
