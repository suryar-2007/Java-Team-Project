# Commercial Airline Fleet Analytics Engine - PowerShell Launch Script
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Commercial Airline Fleet Fuel Efficiency and Maintenance Engine" -ForegroundColor White
Write-Host "======================================================================" -ForegroundColor Cyan

if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "[INFO] Maven detected. Compiling and launching via exec:java..." -ForegroundColor Green
    mvn clean compile exec:java
    exit
}

Write-Host "[INFO] Maven not in PATH. Compiling via standard javac..." -ForegroundColor Yellow
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sources = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -cp "lib/*;src/main/resources" -d bin $sources

if ($LASTEXITCODE -eq 0) {
    Copy-Item -Path "src/main/resources/*" -Destination "bin" -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "[SUCCESS] Compilation succeeded. Launching MainApp..." -ForegroundColor Green
    java -cp "bin;lib/*;src/main/resources" com.airline.fleet.MainApp
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}
