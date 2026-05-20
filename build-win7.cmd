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

where javac >nul 2>nul
if errorlevel 1 goto NEED_JDK
goto HAVE_JDK

:NEED_JDK
echo Java JDK 8 is recommended to build this launcher.
echo The jar is compiled as Java 7 bytecode for Windows XP-era compatibility.
echo.
echo I can download a portable Eclipse Temurin 8 JDK from Adoptium now.
echo It will be stored locally in:
echo   %~dp0tools\jdk8
echo.
choice /C YN /M "Download Java JDK 8 now"
if errorlevel 2 (
  echo Build cancelled. Install Java JDK 8 manually, then run this again.
  pause
  exit /b 1
)
if not exist "%~dp0tools" mkdir "%~dp0tools"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\download-temurin8-jdk.ps1" -Destination "%~dp0tools\jdk8"
if errorlevel 1 (
  echo Java download failed.
  echo You can manually install Java JDK 8 and rerun this file.
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

:HAVE_JDK
where javac >nul 2>nul
if errorlevel 1 (
  echo Java JDK still was not found after dependency setup.
  echo Install Java JDK 8 manually, or check tools\jdk8.
  pause
  exit /b 1
)

where jar >nul 2>nul
if errorlevel 1 (
  echo jar.exe was not found. A full Java JDK is required, not only a JRE.
  pause
  exit /b 1
)

if not exist build mkdir build
if exist build\classes rmdir /s /q build\classes
mkdir build\classes

dir /s /b src\*.java > build\sources.txt
javac -source 1.7 -target 1.7 -encoding UTF-8 -d build\classes @build\sources.txt
if errorlevel 1 (
  echo Build failed.
  pause
  exit /b 1
)

if not exist build\classes\net\minecraft mkdir build\classes\net\minecraft
if exist resources\net\minecraft\*.png copy /y resources\net\minecraft\*.png build\classes\net\minecraft\ >nul
if exist resources\net\minecraft\*.jpg copy /y resources\net\minecraft\*.jpg build\classes\net\minecraft\ >nul

> build\manifest.mf echo Manifest-Version: 1.0
>> build\manifest.mf echo Main-Class: net.minecraft.MinecraftLauncher
>> build\manifest.mf echo.

jar cfm MCLauncherRevive-modern.jar build\manifest.mf -C build\classes .
if errorlevel 1 (
  echo Jar packaging failed.
  pause
  exit /b 1
)

echo Built MCLauncherRevive-modern.jar
pause
