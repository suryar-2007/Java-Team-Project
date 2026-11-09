@echo off
title Commercial Airline Fleet Analytics Engine
echo ======================================================================
echo Commercial Airline Fleet Fuel Efficiency and Maintenance Engine
echo ======================================================================

REM Check for Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java runtime not found in PATH. Please install Java 17+ or configure PATH.
    pause
    exit /b 1
)

REM Check if Maven is available in PATH
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] Running with Maven...
    mvn clean compile exec:java
    goto end
)

echo [INFO] Maven not in PATH. Compiling and running with javac and bundled libraries...
if not exist "bin" mkdir "bin"

echo Compiling Java source files...
javac -encoding UTF-8 -cp "lib/*;src/main/resources" -d bin src/main/java/com/airline/fleet/*.java src/main/java/com/airline/fleet/model/*.java src/main/java/com/airline/fleet/service/*.java src/main/java/com/airline/fleet/ui/*.java src/main/java/com/airline/fleet/ui/components/*.java src/main/java/com/airline/fleet/ui/panels/*.java

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

echo Copying resources...
xcopy /s /y /q "src\main\resources\*" "bin\" >nul 2>nul

echo Launching MainApp...
java -cp "bin;lib/*;src/main/resources" com.airline.fleet.MainApp

:end
pause
