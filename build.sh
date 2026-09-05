#!/bin/bash
# Build script for GuessTheWord Android project
# Requires: Android SDK (API 10), Ant, Java JDK

echo "Building GuessTheWord for Android 2.3.6 (API 10)..."
echo

# Check for Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "ERROR: ANDROID_HOME environment variable not set"
    echo "Please set ANDROID_HOME to your Android SDK path"
    exit 1
fi

echo "Using Android SDK: $ANDROID_HOME"

# Update project for API 10
"$ANDROID_HOME/tools/android" update project --path . --target android-10 --name GuessTheWord

echo
echo "Building debug APK..."
ant debug

if [ $? -ne 0 ]; then
    echo
    echo "BUILD FAILED"
    exit 1
fi

echo
echo "BUILD SUCCESSFUL"
echo "APK location: bin/GuessTheWord-debug.apk"
echo