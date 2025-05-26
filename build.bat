@echo off
echo Building Poime's Mod Checker...
gradlew.bat build

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Build succeeded!
echo The mod jar is located at: build\libs\
pause
