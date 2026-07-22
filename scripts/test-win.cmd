@echo off
setlocal

set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
cd /d "%ROOT_DIR%" || exit /b 1

call "%ROOT_DIR%\scripts\build-win.cmd"
if errorlevel 1 exit /b 1

call :FindJdk
if not defined JAVAC_EXE (
  echo [FAIL] javac.exe was not found for launcher self-tests.
  exit /b 1
)
if not defined JAVA_EXE (
  echo [FAIL] java.exe was not found for launcher self-tests.
  exit /b 1
)

if exist build\test-classes rmdir /s /q build\test-classes
mkdir build\test-classes
dir /s /b tests\*.java > build\test-sources.txt
"%JAVAC_EXE%" -source 7 -target 7 -Xlint:-options -encoding UTF-8 -cp build\classes -d build\test-classes @build\test-sources.txt
if errorlevel 1 exit /b 1

"%JAVA_EXE%" -Djava.awt.headless=true -cp "build\classes;build\test-classes" net.minecraft.LauncherSelfTest
exit /b %ERRORLEVEL%

:FindJdk
set "JAVA_EXE="
set "JAVAC_EXE="
for %%R in ("%ROOT_DIR%\tools\jdk8" "%JAVA_HOME%") do (
  if not defined JAVA_EXE if exist "%%~R\bin\java.exe" set "JAVA_EXE=%%~R\bin\java.exe"
  if not defined JAVAC_EXE if exist "%%~R\bin\javac.exe" set "JAVAC_EXE=%%~R\bin\javac.exe"
)
if defined JAVA_EXE if defined JAVAC_EXE exit /b 0
for /d %%D in ("%ROOT_DIR%\tools\jdk8\*") do (
  if not defined JAVA_EXE if exist "%%~fD\bin\java.exe" set "JAVA_EXE=%%~fD\bin\java.exe"
  if not defined JAVAC_EXE if exist "%%~fD\bin\javac.exe" set "JAVAC_EXE=%%~fD\bin\javac.exe"
)
if not defined JAVA_EXE for %%J in (java.exe) do if not "%%~$PATH:J"=="" set "JAVA_EXE=%%~$PATH:J"
if not defined JAVAC_EXE for %%J in (javac.exe) do if not "%%~$PATH:J"=="" set "JAVAC_EXE=%%~$PATH:J"
exit /b 0
