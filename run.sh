#!/bin/bash
cd "$(dirname "$0")"

# Auto-compile if not already compiled
if [ ! -f "src/Main.class" ]; then
    echo "Compiling Wikipedia Fetcher..."
    javac -cp "lib/*:src" src/Main.java src/model/*.java src/service/*.java src/ui/*.java src/utils/*.java
    if [ $? -ne 0 ]; then
        echo "Compilation failed. Ensure JDK is installed."
        exit 1
    fi
fi

# Launch GUI
java -cp "lib/*:src" Main &

