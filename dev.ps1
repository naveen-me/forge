# dev.ps1 - Development startup script for Broadcast Playout Application
# This script starts both the engine and UI simultaneously

param(
    [string]$Environment = "development"
)

Write-Host "🚀 Starting Broadcast Playout Development Environment" -ForegroundColor Green
Write-Host "Environment: $Environment" -ForegroundColor Cyan
Write-Host ""

# Function to start the engine
function Start-Engine {
    param([string]$WorkingDir)
    
    Write-Host "🔵 Starting Playout Engine..." -ForegroundColor Blue
    Set-Location $WorkingDir
    
    # Build the engine first
    Write-Host "📦 Building engine..." -ForegroundColor Yellow
    dotnet build "engine-core/src/BroadcastEngine/BroadcastEngine.csproj" --configuration $Environment
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Engine build failed!" -ForegroundColor Red
        exit 1
    }
    
    # Start the engine
    Write-Host "⚙️  Engine starting on port 7001..." -ForegroundColor Green
    Start-Process -FilePath "dotnet" -ArgumentList "run", "--project", "engine-core/src/BroadcastEngine" -WorkingDirectory (Get-Location)
    
    # Wait for engine to start
    Start-Sleep -Seconds 5
}

# Function to start the UI
function Start-UI {
    param([string]$WorkingDir)
    
    Write-Host "🔵 Starting UI Editor..." -ForegroundColor Blue
    Set-Location "$WorkingDir\electron-ui"
    
    # Install dependencies if not already done
    if (!(Test-Path "node_modules")) {
        Write-Host "📦 Installing UI dependencies..." -ForegroundColor Yellow
        npm install
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ UI dependencies installation failed!" -ForegroundColor Red
            exit 1
        }
    }
    
    # Start the UI
    Write-Host "🎨 UI starting on port 8080..." -ForegroundColor Green
    Start-Process -FilePath "npm" -ArgumentList "run", "electron:serve" -WorkingDirectory (Get-Location)
    
    # Wait for UI to start
    Start-Sleep -Seconds 8
}

# Main execution
$rootDir = Get-Location

# Start engine first
Start-Engine -WorkingDir $rootDir

# Then start UI
Start-UI -WorkingDir $rootDir

Write-Host ""
Write-Host "✅ Development environment started successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "🔗 Engine Health: http://localhost:7001/health" -ForegroundColor Cyan
Write-Host "🔗 UI Interface: http://localhost:8080" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Press Ctrl+C to stop the development environment" -ForegroundColor Yellow
Write-Host ""

# Wait for user input to stop
Write-Host "Press any key to stop..." -ForegroundColor Red
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")