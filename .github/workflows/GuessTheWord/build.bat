@echo off
REM Build script for GuessTheWord Android project
REM Requires: Android SDK (API 10), Ant, Java JDK

echo Building GuessTheWord for Android 2.3.6 (API 10)...
echo.

REM Check for Android SDK
if not defined ANDROID_HOME (
    echo ERROR: ANDROID_HOME environment variable not set
    echo Please set ANDROID_HOME to your Android SDK path
    pause
    exit /b 1
)

echo Using Android SDK: %ANDROID_HOME%

REM Update project for API 10
call %ANDROID_HOME%\tools\android.bat update project --path . --target android-10 --name GuessTheWord

echo.
echo Building debug APK...
call ant debug

if errorlevel 1 (
    echo.
    echo BUILD FAILED
    pause
    exit /b 1
)

echo.
echo BUILD SUCCESSFUL
echo APK location: bin\GuessTheWord-debug.apk
echo.
pause