@echo off
setlocal
set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
cd /d "%ROOT_DIR%"

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
  call "%ROOT_DIR%\scripts\build-win7.cmd"
  if errorlevel 1 exit /b 1
  call :FindJava
)

if not defined JAVA_EXE goto NEED_JAVA

goto RUN_LAUNCHER

:NEED_JAVA
echo Java runtime not found.
if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  echo Windows XP offline/classic mode needs Java 7 or an XP-compatible Java 8 runtime.
  echo Use the XP bundled-Java release package, run a bundled Java installer, or place Java at tools\java7.
  echo Expected runtime path: tools\java7\bin\java.exe
  call :OfferJavaInstaller
  call :FindJava
  if defined JAVA_EXE goto RUN_LAUNCHER
  echo Java runtime not found after installer prompt.
  pause
  exit /b 1
)

echo Java is required to run this launcher.
echo.
echo I looked in bundled Java folders, JAVA_HOME, PATH, and common Program Files Java folders.
echo I can download a portable Eclipse Temurin 8 JDK from Adoptium now.
echo It will be stored locally in:
echo   %ROOT_DIR%\tools\jdk8
echo.
set "DOWNLOAD_JDK="
set /p "DOWNLOAD_JDK=Download Java JDK 8 now? [Y/n] "
if "%DOWNLOAD_JDK%"=="" set "DOWNLOAD_JDK=Y"
if /I "%DOWNLOAD_JDK%"=="N" (
  echo Launch cancelled. Install Java 8 manually, then run this again.
  pause
  exit /b 1
)
if not exist "%ROOT_DIR%\tools" mkdir "%ROOT_DIR%\tools"
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT_DIR%\tools\download-temurin8-jdk.ps1" -Destination "%ROOT_DIR%\tools\jdk8"
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

goto RUN_LAUNCHER

:RUN_LAUNCHER
if /I "%JAVA_SOURCE%"=="java7" echo Using bundled Java runtime: tools\java7
echo Java runtime found: %JAVA_EXE%
"%JAVA_EXE%" %MCLAUNCHER_JAVA_OPTS% -jar MCLauncherRevival.jar
set "LAUNCH_EXIT=%ERRORLEVEL%"
if not "%LAUNCH_EXIT%"=="0" (
  echo Launcher exited with an error.
  pause
)
exit /b %LAUNCH_EXIT%

:OfferJavaInstaller
if not exist "%ROOT_DIR%\tools\java-installers" exit /b 0
echo.
echo Bundled Java installers are available.
echo Choose one only if you trust this release package and accept the Java distributor's license.
echo.
echo   1. jre-7u1-windows-i586.exe  ^(32-bit XP JRE^)
echo   2. jre-7u1-windows-x64.exe   ^(64-bit XP JRE^)
echo   3. jdk-7-windows-i586.exe    ^(32-bit XP JDK^)
echo   4. jdk-7-windows-x64.exe     ^(64-bit XP JDK^)
echo   5. jre-8u151-windows-i586.exe ^(32-bit XP-compatible Java 8 JRE^)
echo   6. jre-8u151-windows-x64.exe  ^(64-bit XP-compatible Java 8 JRE^)
echo.
set "JAVA_INSTALL_CHOICE="
set /p "JAVA_INSTALL_CHOICE=Run a bundled Java installer now? [1-6/N] "
if "%JAVA_INSTALL_CHOICE%"=="" exit /b 0
if /I "%JAVA_INSTALL_CHOICE%"=="N" exit /b 0
set "JAVA_INSTALLER="
if "%JAVA_INSTALL_CHOICE%"=="1" set "JAVA_INSTALLER=%ROOT_DIR%\tools\java-installers\jre-7u1-windows-i586.exe"
if "%JAVA_INSTALL_CHOICE%"=="2" set "JAVA_INSTALLER=%ROOT_DIR%\tools\java-installers\jre-7u1-windows-x64.exe"
if "%JAVA_INSTALL_CHOICE%"=="3" set "JAVA_INSTALLER=%ROOT_DIR%\tools\java-installers\jdk-7-windows-i586.exe"
if "%JAVA_INSTALL_CHOICE%"=="4" set "JAVA_INSTALLER=%ROOT_DIR%\tools\java-installers\jdk-7-windows-x64.exe"
if "%JAVA_INSTALL_CHOICE%"=="5" set "JAVA_INSTALLER=%ROOT_DIR%\tools\java-installers\jre-8u151-windows-i586.exe"
if "%JAVA_INSTALL_CHOICE%"=="6" set "JAVA_INSTALLER=%ROOT_DIR%\tools\java-installers\jre-8u151-windows-x64.exe"
if not defined JAVA_INSTALLER (
  echo No bundled Java installer selected.
  exit /b 0
)
if not exist "%JAVA_INSTALLER%" (
  echo Selected bundled Java installer was not found:
  echo   %JAVA_INSTALLER%
  exit /b 0
)
echo Starting bundled Java installer:
echo   %JAVA_INSTALLER%
echo Finish the installer, then return to this window.
start /wait "" "%JAVA_INSTALLER%"
exit /b 0

:FindJava
set "JAVA_EXE="
set "JAVA_SOURCE="
if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  if exist "%ROOT_DIR%\tools\java7\bin\java.exe" (
    set "JAVA_HOME=%ROOT_DIR%\tools\java7"
    set "PATH=%ROOT_DIR%\tools\java7\bin;%PATH%"
    set "JAVA_EXE=%ROOT_DIR%\tools\java7\bin\java.exe"
    set "JAVA_SOURCE=java7"
  )
)
if exist "%ROOT_DIR%\tools\jdk8\bin\java.exe" (
  if not defined JAVA_EXE (
    set "JAVA_HOME=%ROOT_DIR%\tools\jdk8"
    set "PATH=%ROOT_DIR%\tools\jdk8\bin;%PATH%"
    set "JAVA_EXE=%ROOT_DIR%\tools\jdk8\bin\java.exe"
  )
)
if not defined JAVA_EXE (
  for /d %%D in ("%ROOT_DIR%\tools\jdk8\*") do (
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
if not defined JAVA_EXE call :FindInstalledJava "%ProgramFiles%\Java"
if not defined JAVA_EXE call :FindInstalledJava "%ProgramFiles(x86)%\Java"
if not defined JAVA_EXE (
  for %%P in (java.exe) do (
    if not "%%~$PATH:P"=="" set "JAVA_EXE=%%~$PATH:P"
  )
)
exit /b 0

:FindInstalledJava
set "JAVA_SCAN_ROOT=%~1"
if "%JAVA_SCAN_ROOT%"=="" exit /b 0
if not exist "%JAVA_SCAN_ROOT%" exit /b 0
for /d %%D in ("%JAVA_SCAN_ROOT%\jdk*" "%JAVA_SCAN_ROOT%\jre*" "%JAVA_SCAN_ROOT%\*") do (
  if not defined JAVA_EXE if exist "%%~fD\bin\java.exe" (
    set "JAVA_HOME=%%~fD"
    set "PATH=%%~fD\bin;%PATH%"
    set "JAVA_EXE=%%~fD\bin\java.exe"
  )
)
exit /b 0
