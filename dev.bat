@echo off
echo 🚀 Starting Broadcast Playout Development Environment
echo.

echo 🔵 Starting Playout Engine...
cd /d "%~dp0"
echo 📦 Building engine...
dotnet build "engine-core/src/BroadcastEngine/BroadcastEngine.csproj" --configuration development
if %errorlevel% neq 0 (
    echo ❌ Engine build failed!
    exit /b 1
)

echo ⚙️  Engine starting on port 7001...
start /b dotnet run --project "engine-core/src/BroadcastEngine"

timeout /t 5 /nobreak >nul

echo 🔵 Starting UI Editor...
cd electron-ui

if not exist "node_modules" (
    echo 📦 Installing UI dependencies...
    npm install
    if %errorlevel% neq 0 (
        echo ❌ UI dependencies installation failed!
        exit /b 1
    )
)

echo 🎨 UI starting on port 8080...
start /b npm run electron:serve

echo.
echo ✅ Development environment started successfully!
echo.
echo 🔗 Engine Health: http://localhost:7001/health
echo 🔗 UI Interface: http://localhost:8080
echo.
echo 💡 The applications are now running in the background
echo.
pause