@echo off
cd /d "%~dp0"

REM Auto-compile if not already compiled
if not exist "src\Main.class" (
    echo Compiling Wikipedia Fetcher...
    javac -cp "lib/*;src" src\Main.java src\model\*.java src\service\*.java src\ui\*.java src\utils\*.java
    if errorlevel 1 (
        echo Compilation failed. Ensure JDK is installed and added to PATH.
        pause
        exit /b 1
    )
)

REM Launch GUI
start "" javaw -cp "lib/*;src" Main
