@echo off
setlocal enabledelayedexpansion
title Acoustic Driver - Git Deployment Center

:MENU
cls
echo ======================================================
echo       ACOUSTIC DRIVER - GIT DEPLOYMENT CENTER
echo       GitHub: https://github.com/vrai17/AcousticDriver
echo       Profile: vrai17 ^| Branch: main
echo ======================================================
echo.
echo Select an action:
echo.
echo   [1] Push Source Code Update (Fast - No APK Rebuild)
echo   [2] Build Release APK ^& Push Update (assembleRelease)
echo   [3] Publish Official GitHub Release (Titled with App Version)
echo   [4] Push Existing Commits Directly (git push origin main)
echo   [5] Check Git Status
echo   [6] Exit
echo.
set /p CHOICE="Enter your choice (1-6): "

if "%CHOICE%"=="1" goto PUSH_SOURCE
if "%CHOICE%"=="2" goto PUSH_APK
if "%CHOICE%"=="3" goto PUSH_RELEASE
if "%CHOICE%"=="4" goto PUSH_ONLY
if "%CHOICE%"=="5" goto STATUS
if "%CHOICE%"=="6" goto EXIT
echo.
echo Invalid selection. Please choose 1, 2, 3, 4, 5, or 6.
timeout /t 2 >nul
goto MENU

:PUSH_SOURCE
cls
call push_source.bat
goto MENU

:PUSH_APK
cls
call push_apk.bat
goto MENU

:PUSH_RELEASE
cls
call create_release.bat
goto MENU

:PUSH_ONLY
cls
echo ======================================================
echo       PUSHING EXISTING COMMITS TO GITHUB
echo ======================================================
echo.
git push origin main
if errorlevel 1 (
    echo.
    echo [ERROR] Git push failed.
) else (
    echo.
    echo [SUCCESS] Pushed to origin main!
)
echo.
pause
goto MENU

:STATUS
cls
echo ======================================================
echo                 CURRENT GIT STATUS
echo ======================================================
echo.
git status
echo.
echo ------------------------------------------------------
echo Recent Commits:
git log -n 3 --oneline
echo.
pause
goto MENU

:EXIT
exit /b 0
