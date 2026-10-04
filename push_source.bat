@echo off
setlocal enabledelayedexpansion
title Acoustic Driver - Push Source Code Update

echo ======================================================
echo    ACOUSTIC DRIVER - PUSH SOURCE CODE UPDATE
echo    Profile: vrai17 ^| Branch: main
echo ======================================================
echo.

set /p COMMIT_MSG="Enter commit message (Press Enter for default 'chore: update source code'): "
if "%COMMIT_MSG%"=="" set COMMIT_MSG=chore: update source code

echo.
echo [1/3] Staging necessary source files...
git add .

echo.
echo [2/3] Committing changes...
git commit -m "%COMMIT_MSG%"
if errorlevel 1 (
    echo [INFO] Working tree was already clean or no changes to commit.
)

echo.
echo [3/3] Pushing to GitHub (origin main)...
git push origin main
if errorlevel 1 (
    echo.
    echo [ERROR] Git push failed. Please check network connection and credentials.
    pause
    exit /b 1
)

echo.
echo ======================================================
echo    SUCCESS: Source code successfully pushed to GitHub!
echo ======================================================
echo.
pause
