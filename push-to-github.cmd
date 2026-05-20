@echo off
setlocal
cd /d "%~dp0"

echo MCLauncherRevival GitHub upload helper
echo.
echo This will initialize this clean upload folder as a git repo and push it to:
echo   https://github.com/VinceGuyMan/MCLauncherRevival.git
echo.
echo It does NOT upload the local JDK, build folder, auth tokens, or generated caches.
echo.
choice /C YN /M "Continue"
if errorlevel 2 exit /b 1

where git >nul 2>nul
if errorlevel 1 (
  echo Git was not found on PATH.
  echo Install Git for Windows from https://git-scm.com/download/win and run this again.
  pause
  exit /b 1
)

if not exist .git (
  git init
  if errorlevel 1 goto FAIL
)

git branch -M main
if errorlevel 1 goto FAIL

git remote remove origin >nul 2>nul
git remote add origin https://github.com/VinceGuyMan/MCLauncherRevival.git
if errorlevel 1 goto FAIL

git add .
if errorlevel 1 goto FAIL

git commit -m "Initial MCLauncherRevival upload"
if errorlevel 1 (
  echo Commit may have failed because there are no changes, or Git user.name/user.email is not configured.
  echo If Git asks who you are, run:
  echo   git config --global user.name "VinceGuyMan"
  echo   git config --global user.email "YOUR_EMAIL@example.com"
  pause
  exit /b 1
)

git push -u origin main
if errorlevel 1 goto FAIL

echo.
echo Upload complete!
echo Open: https://github.com/VinceGuyMan/MCLauncherRevival
pause
exit /b 0

:FAIL
echo.
echo Upload failed. Git should have printed the reason above.
echo Common fixes:
echo - Sign in to Git Credential Manager when prompted.
echo - Make sure the repo exists and you have write access.
echo - If GitHub rejects password auth, use a browser sign-in or personal access token.
pause
exit /b 1
