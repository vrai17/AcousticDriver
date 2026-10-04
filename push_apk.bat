@echo off
setlocal enabledelayedexpansion
title Acoustic Driver - Build & Push Release APK

echo ======================================================
echo    ACOUSTIC DRIVER - BUILD & PUSH RELEASE APK
echo    Profile: vrai17 ^| Branch: main
echo ======================================================
echo.

echo [1/4] Compiling signed Release APK with Gradle...
call .\gradlew.bat assembleRelease
if errorlevel 1 (
    echo.
    echo [ERROR] Gradle compilation failed! Please check errors above.
    pause
    exit /b 1
)

echo.
echo [2/4] Updating release\app-release.apk...
if not exist "release" mkdir release
copy /y "app\build\outputs\apk\release\app-release.apk" "release\app-release.apk" >nul
if errorlevel 1 (
    echo.
    echo [ERROR] Failed to copy APK to release directory.
    pause
    exit /b 1
)
echo [OK] release\app-release.apk updated successfully.

set /p COMMIT_MSG="Enter commit message (Press Enter for default 'release: update app-release.apk build'): "
if "%COMMIT_MSG%"=="" set COMMIT_MSG=release: update app-release.apk build

echo.
echo [3/4] Staging and committing release APK and assets...
git add .
git commit -m "%COMMIT_MSG%"
if errorlevel 1 (
    echo [INFO] No new changes to commit.
)

echo.
echo [4/4] Pushing to GitHub (origin main)...
git push origin main
if errorlevel 1 (
    echo.
    echo [ERROR] Git push failed. Please check network connection and credentials.
    pause
    exit /b 1
)

echo.
echo ======================================================
echo    SUCCESS: Release APK successfully pushed to GitHub!
echo ======================================================
echo.
pause
