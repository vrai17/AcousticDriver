@echo off
setlocal enabledelayedexpansion
title Acoustic Driver - Auto Publish GitHub Release

echo ======================================================
echo    ACOUSTIC DRIVER - AUTO PUBLISH GITHUB RELEASE
echo    Profile: vrai17 ^| Branch: main
echo ======================================================
echo.

:: 1. Extract app version from app\build.gradle.kts
for /f "tokens=2 delims=^= " %%a in ('findstr /i "versionName" app\build.gradle.kts') do (
    set APP_VERSION=%%~a
)

if "%APP_VERSION%"=="" (
    echo [ERROR] Could not extract versionName from app\build.gradle.kts!
    pause
    exit /b 1
)

set RELEASE_TAG=v%APP_VERSION%
set RELEASE_TITLE=Acoustic Driver v%APP_VERSION%

echo Detected App Version: %APP_VERSION%
echo Release Tag:          %RELEASE_TAG%
echo Release Title:        %RELEASE_TITLE%
echo.

:: 2. Check if release\app-release.apk exists
if not exist "release\app-release.apk" (
    echo [WARNING] release\app-release.apk not found.
    echo Building Release APK now...
    call .\gradlew.bat assembleRelease
    if errorlevel 1 (
        echo [ERROR] Build failed!
        pause
        exit /b 1
    )
    if not exist "release" mkdir release
    copy /y "app\build\outputs\apk\release\app-release.apk" "release\app-release.apk" >nul
)

:: 3. Commit any uncommitted changes
echo [1/3] Staging and committing release files...
git add .
git commit -m "release: %RELEASE_TITLE%"
echo.

:: 4. Push main branch
echo [2/3] Pushing main branch to GitHub...
git push origin main
if errorlevel 1 (
    echo [ERROR] Failed to push main branch. Please check GitHub connection.
    pause
    exit /b 1
)

:: 5. Create and push tag to trigger automatic GitHub Release
echo.
echo [3/3] Tagging and triggering automatic GitHub Release...
git tag -d %RELEASE_TAG% >nul 2>&1
git push origin :refs/tags/%RELEASE_TAG% >nul 2>&1
git tag -a %RELEASE_TAG% -m "%RELEASE_TITLE%"
git push origin %RELEASE_TAG%
if errorlevel 1 (
    echo [ERROR] Failed to push tag %RELEASE_TAG%.
    pause
    exit /b 1
)

echo.
echo ======================================================
echo    SUCCESS! Release %RELEASE_TITLE% triggered!
echo    GitHub Actions is now publishing your release at:
echo    https://github.com/vrai17/AcousticDriver/releases
echo ======================================================
echo.
pause
