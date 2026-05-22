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

if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  call :Status " XP " "Offline/classic mode enabled"
  call :Status "INFO" "XP mode will not try to download Java automatically"
)

call :FindJdk
if defined JAVAC_EXE if defined JAR_EXE goto HAVE_JDK

:NEED_JDK
if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  call :Status "FAIL" "Java build tools were not found."
  echo Windows XP offline/classic mode needs Java 7 or an XP-compatible Java 8 runtime already installed or extracted at tools\java7 ^(preferred^) or tools\jdk8.
  pause
  exit /b 1
)

call :Status "WARN" "Java JDK 8 was not found."
echo.
echo MCLauncherRevival needs a JDK to build from source.
echo A portable Java 8 JDK can be downloaded into:
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
  call :Status "FAIL" "Build cancelled. Install Java JDK 8 manually, then run this again."
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
call :FindJdk
if not defined JAVAC_EXE goto JDK_STILL_MISSING
if not defined JAR_EXE goto JDK_STILL_MISSING
goto HAVE_JDK

:JDK_STILL_MISSING
call :Status "FAIL" "Java JDK 8 still was not found after dependency setup."
echo Install Java JDK 8 manually, or place/extract it at tools\jdk8.
pause
exit /b 1

:HAVE_JDK
call :Status " OK " "Checking Java JDK"
if defined MCLR_VERBOSE echo Java runtime found: %JAVA_EXE%
if not exist build mkdir build
if exist build\classes rmdir /s /q build\classes
mkdir build\classes
"%JAVAC_EXE%" -source 7 -target 7 -Xlint:-options -encoding UTF-8 -d build\classes src\net\minecraft\*.java
if errorlevel 1 (
  call :Status "FAIL" "Java compile failed."
  pause
  exit /b 1
)
xcopy /E /I /Y resources build\classes >nul
if exist MCLauncherRevival.jar del MCLauncherRevival.jar
"%JAR_EXE%" cfe MCLauncherRevival.jar net.minecraft.MinecraftLauncher -C build\classes .
if errorlevel 1 (
  call :Status "FAIL" "Jar packaging failed."
  pause
  exit /b 1
)
call :Status " OK " "Built MCLauncherRevival.jar"
exit /b 0

:Status
echo [%~1] %~2
exit /b 0

:FindJdk
set "JAVA_EXE="
set "JAVAC_EXE="
set "JAR_EXE="
if exist "%ROOT_DIR%\tools\java7\bin\java.exe" set "JAVA_EXE=%ROOT_DIR%\tools\java7\bin\java.exe"
if exist "%ROOT_DIR%\tools\java7\bin\javac.exe" set "JAVAC_EXE=%ROOT_DIR%\tools\java7\bin\javac.exe"
if exist "%ROOT_DIR%\tools\java7\bin\jar.exe" set "JAR_EXE=%ROOT_DIR%\tools\java7\bin\jar.exe"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
for /d %%D in ("%ROOT_DIR%\tools\java7\*") do (
  if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
  if exist "%%~fD\bin\javac.exe" set "JAVAC_EXE=%%~fD\bin\javac.exe"
  if exist "%%~fD\bin\jar.exe" set "JAR_EXE=%%~fD\bin\jar.exe"
  if defined JAVAC_EXE if defined JAR_EXE exit /b 0
)
if exist "%ROOT_DIR%\tools\jdk8\bin\java.exe" set "JAVA_EXE=%ROOT_DIR%\tools\jdk8\bin\java.exe"
if exist "%ROOT_DIR%\tools\jdk8\bin\javac.exe" set "JAVAC_EXE=%ROOT_DIR%\tools\jdk8\bin\javac.exe"
if exist "%ROOT_DIR%\tools\jdk8\bin\jar.exe" set "JAR_EXE=%ROOT_DIR%\tools\jdk8\bin\jar.exe"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
for /d %%D in ("%ROOT_DIR%\tools\jdk8\*") do (
  if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
  if exist "%%~fD\bin\javac.exe" set "JAVAC_EXE=%%~fD\bin\javac.exe"
  if exist "%%~fD\bin\jar.exe" set "JAR_EXE=%%~fD\bin\jar.exe"
  if defined JAVAC_EXE if defined JAR_EXE exit /b 0
)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_EXE=%JAVA_HOME%\bin\jar.exe"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
for %%J in (java.exe) do if not "%%~$PATH:J"=="" set "JAVA_EXE=%%~$PATH:J"
for %%J in (javac.exe) do if not "%%~$PATH:J"=="" set "JAVAC_EXE=%%~$PATH:J"
for %%J in (jar.exe) do if not "%%~$PATH:J"=="" set "JAR_EXE=%%~$PATH:J"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
call :FindInstalledJdk "%ProgramFiles%\Java"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
call :FindInstalledJdk "%ProgramFiles%\Eclipse Adoptium"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
call :FindInstalledJdk "%ProgramFiles%\Microsoft"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
call :FindInstalledJdk "%ProgramFiles(x86)%\Java"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
call :FindInstalledJdk "%ProgramFiles(x86)%\Eclipse Adoptium"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
call :FindInstalledJdk "%ProgramFiles(x86)%\Microsoft"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
call :FindInstalledJdk "%LOCALAPPDATA%\Programs\Eclipse Adoptium"
if defined JAVAC_EXE if defined JAR_EXE exit /b 0
call :FindInstalledJdk "%LOCALAPPDATA%\Programs\Java"
exit /b 0

:FindInstalledJdk
set "JAVA_SCAN_DIR=%~1"
if not defined JAVA_SCAN_DIR exit /b 0
if not exist "%JAVA_SCAN_DIR%" exit /b 0
for /d %%D in ("%JAVA_SCAN_DIR%\jdk*") do (
  if exist "%%~fD\bin\javac.exe" if exist "%%~fD\bin\jar.exe" (
    if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
    set "JAVAC_EXE=%%~fD\bin\javac.exe"
    set "JAR_EXE=%%~fD\bin\jar.exe"
    exit /b 0
  )
)
exit /b 0
