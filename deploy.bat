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
echo   [3] Push Existing Commits Directly (git push origin main)
echo   [4] Check Git Status
echo   [5] Exit
echo.
set /p CHOICE="Enter your choice (1-5): "

if "%CHOICE%"=="1" goto PUSH_SOURCE
if "%CHOICE%"=="2" goto PUSH_APK
if "%CHOICE%"=="3" goto PUSH_ONLY
if "%CHOICE%"=="4" goto STATUS
if "%CHOICE%"=="5" goto EXIT
echo.
echo Invalid selection. Please choose 1, 2, 3, 4, or 5.
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
