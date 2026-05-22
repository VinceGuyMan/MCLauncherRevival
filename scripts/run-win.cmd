@echo off
setlocal
if /I "%~1"=="--verbose" set "MCLR_VERBOSE=1"
if defined MCLR_VERBOSE echo on

set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
cd /d "%ROOT_DIR%" || (
  echo [FAIL] Could not open launcher folder.
  echo        %ROOT_DIR%
  pause
  exit /b 1
)

title MCLauncherRevival
mode con: cols=140 lines=42 >nul 2>nul
if /I "%MCLAUNCHER_XP_MODE%"=="1" (color 0A) else (color 0E)

if not defined MCLR_BANNER_SHOWN (
  set "MCLR_BANNER_SHOWN=1"
  call :ShowBanner
)

if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  call :Status " XP " "Offline/classic mode enabled"
  call :Status "INFO" "Microsoft login and fresh downloads are best-effort on XP"
)

call :Status " OK " "Checking launcher folder"

if exist MCLauncherRevival.jar (
  call :Status " OK " "Checking launcher jar"
) else (
  if /I "%MCLAUNCHER_XP_MODE%"=="1" (
    call :Status "FAIL" "MCLauncherRevival.jar was not found."
    echo XP offline mode needs a release package that already includes the jar.
    echo Download the release ZIP, or build the jar on Windows 7+ and copy it here.
    pause
    exit /b 1
  )
  call :Status "WARN" "Launcher jar was not found; building it now"
  call "%ROOT_DIR%\scripts\build-win.cmd" %*
  if errorlevel 1 (
    call :Status "FAIL" "Build failed."
    pause
    exit /b 1
  )
  if exist MCLauncherRevival.jar (
    call :Status " OK " "Checking launcher jar"
  ) else (
    call :Status "FAIL" "MCLauncherRevival.jar was not created."
    pause
    exit /b 1
  )
)

call :FindJava
if not defined JAVA_EXE goto NEED_JAVA
call :Status " OK " "Checking Java runtime"
goto RUN_LAUNCHER

:NEED_JAVA
call :Status "WARN" "Java runtime was not found."
if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  echo.
  echo Windows XP offline/classic mode needs Java 7 or an XP-compatible Java 8 runtime.
  echo Use the XP bundled-Java release package, run a bundled Java installer, or place Java at tools\java7.
  echo Expected runtime path: tools\java7\bin\java.exe
  echo.
  call :OfferJavaInstaller
  call :FindJava
  if defined JAVA_EXE (
    call :Status " OK " "Java runtime found"
    goto RUN_LAUNCHER
  )
  call :Status "FAIL" "Java runtime not found."
  pause
  exit /b 1
)

echo.
echo MCLauncherRevival needs Java to start.
echo A portable Java 8 runtime can be downloaded into:
echo.
echo   tools\jdk8
echo.
echo This stays inside this folder.
echo It does not install Java system-wide.
echo.
set "DOWNLOAD_JDK="
set /p "DOWNLOAD_JDK=Download portable Java 8 now? [Y/N] "
if "%DOWNLOAD_JDK%"=="" set "DOWNLOAD_JDK=Y"
if /I not "%DOWNLOAD_JDK%"=="Y" (
  call :Status "FAIL" "Launch cancelled. Install Java 8 manually, then run this again."
  pause
  exit /b 1
)
if not exist "%ROOT_DIR%\tools" mkdir "%ROOT_DIR%\tools"
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT_DIR%\tools\download-temurin8-jdk.ps1" -Destination "%ROOT_DIR%\tools\jdk8"
if errorlevel 1 (
  call :Status "FAIL" "Java dependency setup failed."
  echo Java JDK 8 still was not found after dependency setup.
  echo Install Java JDK 8 manually, or place/extract it at tools\jdk8.
  pause
  exit /b 1
)
call :FindJava
if not defined JAVA_EXE (
  call :Status "FAIL" "Java runtime not found after dependency setup."
  echo Install Java JDK 8 manually, or place/extract it at tools\jdk8.
  pause
  exit /b 1
)
call :Status " OK " "Java runtime found"
goto RUN_LAUNCHER

:RUN_LAUNCHER
call :Status " OK " "Starting launcher window..."
echo.
echo                     Press Nothing. Happy Mining!
echo.
if /I "%JAVA_SOURCE%"=="java7" call :Status "INFO" "Using bundled Java runtime: tools\java7"
if defined MCLR_VERBOSE echo Java runtime found: %JAVA_EXE%
"%JAVA_EXE%" %MCLAUNCHER_JAVA_OPTS% -jar MCLauncherRevival.jar
set "LAUNCH_EXIT=%ERRORLEVEL%"
if not "%LAUNCH_EXIT%"=="0" (
  call :Status "FAIL" "Launcher exited with an error."
  pause
)
exit /b %LAUNCH_EXIT%

:ShowBanner
cls
if exist "%ROOT_DIR%\scripts\banner.txt" (
  type "%ROOT_DIR%\scripts\banner.txt"
) else (
  echo MCLauncherRevival
  echo Starting launcher...
)
echo.
if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  if exist "%ROOT_DIR%\scripts\boot-card-xp.txt" (
    type "%ROOT_DIR%\scripts\boot-card-xp.txt"
  ) else (
    echo MCLauncherRevival Alpha
    echo Windows XP offline/classic mode
    echo Use downloaded versions + Play Offline
  )
) else (
  if exist "%ROOT_DIR%\scripts\boot-card-win.txt" (
    type "%ROOT_DIR%\scripts\boot-card-win.txt"
  ) else (
    echo MCLauncherRevival Alpha
    echo Classic launcher shell. Modern auth. Old-school dirt.
  )
)
echo.
exit /b 0

:Status
echo [%~1] %~2
exit /b 0

:OfferJavaInstaller
if exist "%ROOT_DIR%\tools\java7\bin\java.exe" exit /b 0
set "INSTALLER="
if exist "%ROOT_DIR%\jre-7u1-windows-i586.exe" set "INSTALLER=%ROOT_DIR%\jre-7u1-windows-i586.exe"
if not defined INSTALLER if exist "%ROOT_DIR%\jre-7u1-windows-x64.exe" set "INSTALLER=%ROOT_DIR%\jre-7u1-windows-x64.exe"
if not defined INSTALLER if exist "%ROOT_DIR%\jre-8u151-windows-i586.exe" set "INSTALLER=%ROOT_DIR%\jre-8u151-windows-i586.exe"
if not defined INSTALLER if exist "%ROOT_DIR%\jre-8u151-windows-x64.exe" set "INSTALLER=%ROOT_DIR%\jre-8u151-windows-x64.exe"
if not defined INSTALLER exit /b 0

echo Found an XP-compatible Java installer in this folder.
echo This installer is third-party Java software under its own license.
echo Old Java is not safe for general browsing or production use.
echo.
set "RUN_INSTALLER="
set /p "RUN_INSTALLER=Run the Java installer now? [Y/N] "
if /I not "%RUN_INSTALLER%"=="Y" exit /b 0
start /wait "Java Installer" "%INSTALLER%"
exit /b 0

:FindJava
set "JAVA_EXE="
set "JAVA_SOURCE="
if exist "%ROOT_DIR%\tools\java7\bin\java.exe" (
  set "JAVA_EXE=%ROOT_DIR%\tools\java7\bin\java.exe"
  set "JAVA_SOURCE=java7"
  exit /b 0
)
for /d %%D in ("%ROOT_DIR%\tools\java7\*") do (
  if exist "%%~fD\bin\java.exe" (
    set "JAVA_EXE=%%~fD\bin\java.exe"
    set "JAVA_SOURCE=java7"
    exit /b 0
  )
)
if exist "%ROOT_DIR%\tools\jdk8\bin\java.exe" (
  set "JAVA_EXE=%ROOT_DIR%\tools\jdk8\bin\java.exe"
  set "JAVA_SOURCE=jdk8"
  exit /b 0
)
for /d %%D in ("%ROOT_DIR%\tools\jdk8\*") do (
  if exist "%%~fD\bin\java.exe" (
    set "JAVA_EXE=%%~fD\bin\java.exe"
    set "JAVA_SOURCE=jdk8"
    exit /b 0
  )
)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
  set "JAVA_SOURCE=JAVA_HOME"
  exit /b 0
)
for %%J in (java.exe) do (
  if not "%%~$PATH:J"=="" (
    set "JAVA_EXE=%%~$PATH:J"
    set "JAVA_SOURCE=PATH"
    exit /b 0
  )
)
call :FindInstalledJava "%ProgramFiles%\Java"
if defined JAVA_EXE exit /b 0
call :FindInstalledJava "%ProgramFiles%\Eclipse Adoptium"
if defined JAVA_EXE exit /b 0
call :FindInstalledJava "%ProgramFiles%\Microsoft"
if defined JAVA_EXE exit /b 0
call :FindInstalledJava "%ProgramFiles(x86)%\Java"
if defined JAVA_EXE exit /b 0
call :FindInstalledJava "%ProgramFiles(x86)%\Eclipse Adoptium"
if defined JAVA_EXE exit /b 0
call :FindInstalledJava "%ProgramFiles(x86)%\Microsoft"
if defined JAVA_EXE exit /b 0
call :FindInstalledJava "%LOCALAPPDATA%\Programs\Eclipse Adoptium"
if defined JAVA_EXE exit /b 0
call :FindInstalledJava "%LOCALAPPDATA%\Programs\Java"
exit /b 0

:FindInstalledJava
set "JAVA_SCAN_DIR=%~1"
if not defined JAVA_SCAN_DIR exit /b 0
if not exist "%JAVA_SCAN_DIR%" exit /b 0
for /d %%D in ("%JAVA_SCAN_DIR%\jdk*" "%JAVA_SCAN_DIR%\jre*") do (
  if exist "%%~fD\bin\java.exe" (
    set "JAVA_EXE=%%~fD\bin\java.exe"
    set "JAVA_SOURCE=installed"
    exit /b 0
  )
)
exit /b 0
