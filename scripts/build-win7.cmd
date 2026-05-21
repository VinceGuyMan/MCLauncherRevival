@echo off
setlocal
set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
cd /d "%ROOT_DIR%"

if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  echo XP offline mode detected.
  echo Online login and fresh downloads are disabled/best-effort on XP.
  echo XP mode will not try to download Java automatically.
)

call :FindJdk
if defined JAVAC_EXE if defined JAR_EXE goto HAVE_JDK

:NEED_JDK
if /I "%MCLAUNCHER_XP_MODE%"=="1" (
  echo Java runtime not found.
  echo Windows XP offline/classic mode needs Java 7 or an XP-compatible Java 8 runtime already installed or extracted at tools\java7 ^(preferred^) or tools\jdk8.
  pause
  exit /b 1
)

echo Java JDK 8 is recommended to build this launcher.
echo The jar is compiled as Java 7 bytecode for Windows XP-era compatibility.
echo.
echo I can download a portable Eclipse Temurin 8 JDK from Adoptium now.
echo It will be stored locally in:
echo   %ROOT_DIR%\tools\jdk8
echo.
set "DOWNLOAD_JDK="
set /p "DOWNLOAD_JDK=Download Java JDK 8 now? [Y/N] "
if /I not "%DOWNLOAD_JDK%"=="Y" (
  echo Build cancelled. Install Java JDK 8 manually, then run this again.
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
call :FindJdk
if not defined JAVAC_EXE goto JDK_FAILED
if not defined JAR_EXE goto JDK_FAILED
goto HAVE_JDK

:JDK_FAILED
echo Java JDK 8 still was not found after dependency setup.
echo Install Java JDK 8 manually, or place/extract it at tools\jdk8.
pause
exit /b 1

:HAVE_JDK
if defined JAVA_EXE echo Java runtime found: %JAVA_EXE%

if not exist build mkdir build
if exist build\classes rmdir /s /q build\classes
mkdir build\classes

dir /s /b src\*.java > build\sources.txt
"%JAVAC_EXE%" -source 1.7 -target 1.7 -encoding UTF-8 -d build\classes @build\sources.txt
if errorlevel 1 (
  echo Build failed.
  pause
  exit /b 1
)

if exist resources xcopy /e /i /y resources build\classes\ >nul

> build\manifest.mf echo Manifest-Version: 1.0
>> build\manifest.mf echo Main-Class: net.minecraft.MinecraftLauncher
>> build\manifest.mf echo.

"%JAR_EXE%" cfm MCLauncherRevival.jar build\manifest.mf -C build\classes .
if errorlevel 1 (
  echo Jar packaging failed.
  pause
  exit /b 1
)

echo Built MCLauncherRevival.jar
pause
exit /b 0

:FindJdk
set "JAVA_EXE="
set "JAVAC_EXE="
set "JAR_EXE="
if exist "%ROOT_DIR%\tools\jdk8\bin\java.exe" (
  set "JAVA_HOME=%ROOT_DIR%\tools\jdk8"
  set "PATH=%ROOT_DIR%\tools\jdk8\bin;%PATH%"
  set "JAVA_EXE=%ROOT_DIR%\tools\jdk8\bin\java.exe"
  if exist "%ROOT_DIR%\tools\jdk8\bin\javac.exe" set "JAVAC_EXE=%ROOT_DIR%\tools\jdk8\bin\javac.exe"
  if exist "%ROOT_DIR%\tools\jdk8\bin\jar.exe" set "JAR_EXE=%ROOT_DIR%\tools\jdk8\bin\jar.exe"
)
if not defined JAVAC_EXE (
  for /d %%D in ("%ROOT_DIR%\tools\jdk8\*") do (
    if exist "%%~fD\bin\java.exe" (
      set "JAVA_HOME=%%~fD"
      set "PATH=%%~fD\bin;%PATH%"
      set "JAVA_EXE=%%~fD\bin\java.exe"
      if exist "%%~fD\bin\javac.exe" set "JAVAC_EXE=%%~fD\bin\javac.exe"
      if exist "%%~fD\bin\jar.exe" set "JAR_EXE=%%~fD\bin\jar.exe"
    )
  )
)
if not defined JAVAC_EXE (
  if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
    if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_EXE=%JAVA_HOME%\bin\jar.exe"
  )
)
if not defined JAVA_EXE (
  for %%P in (java.exe) do (
    if not "%%~$PATH:P"=="" set "JAVA_EXE=%%~$PATH:P"
  )
)
if not defined JAVAC_EXE (
  for %%P in (javac.exe) do (
    if not "%%~$PATH:P"=="" set "JAVAC_EXE=%%~$PATH:P"
  )
)
if not defined JAR_EXE (
  for %%P in (jar.exe) do (
    if not "%%~$PATH:P"=="" set "JAR_EXE=%%~$PATH:P"
  )
)
exit /b 0
