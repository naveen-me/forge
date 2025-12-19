# Broadcast Playout System

A professional Windows broadcast playout application with three distinct layers:
- UI Editor (Electron + Vue 3 + Tailwind)
- Playout Engine (.NET Core, headless, deterministic)
- Renderer (OBS via WebSocket)

## Architecture Overview

```
┌─────────────────┐    JSON    ┌──────────────────────┐    WebSocket    ┌─────────────────┐
│                 │ ────────→  │                      │ ──────────────→ │                 │
│   UI EDITOR     │            │   PLOUT ENGINE       │                 │    OBS RENDERER │
│ (Electron+Vue)  │ ←──────── │  (.NET Core)         │ ←─────────────  │  (WebSocket)    │
│                 │ State Sync │                      │ Playout Status  │                 │
└─────────────────┘            └──────────────────────┘                 └─────────────────┘
```

## Project Structure
```
playout/
├── electron-ui/                 # Electron + Vue 3 application
│   ├── src/
│   │   ├── components/          # Vue components (Editor, Library, Timeline)
│   │   ├── views/               # Main views (EditMode, PlayMode)
│   │   ├── stores/              # Pinia stores for state management
│   │   ├── services/            # API clients and business logic
│   │   ├── utils/               # Reusable utilities
│   │   └── assets/              # Static assets (icons, default graphics)
│   ├── public/
│   ├── tailwind.config.js
│   ├── package.json
│   └── vue.config.js
├── engine-core/                # .NET Core Playout Engine
│   ├── src/
│   │   ├── BroadcastEngine/     # Main engine executable
│   │   ├── Engine.Contracts/    # Shared contracts and interfaces
│   │   ├── Engine.Services/     # Core services (Scheduler, AdManager, etc.)
│   │   ├── Engine.OBS/          # OBS WebSocket integration
│   │   ├── Engine.Logging/      # Logging and monitoring
│   │   ├── Engine.Licensing/    # License validation and enforcement
│   │   └── Engine.Models/       # Data models and transfer objects
│   ├── tests/
│   └── Dockerfile
├── shared-contracts/           # JSON schemas and contracts
├── media-assets/               # Default media assets
├── config/                     # Configuration files
├── docs/                       # Documentation
├── scripts/                    # Deployment and maintenance scripts
└── README.md
```

## Core Features
- Media library (folders, metadata, thumbnails, paths)
- Ads (grouped, insertable at offsets)
- Overlays (text/image/video with x, y, size, timing)
- Timeline scheduler (drag & drop, offsets, repeat, dates)
- Edit mode vs Play mode
- Subscription & license enforcement (cloud-based, engine enforced)

## Setup Instructions

### Prerequisites
- Node.js v18+
- .NET 6 SDK or higher
- OBS Studio with WebSocket plugin installed

### Development Setup
1. Install dependencies for UI: `cd electron-ui && npm install`
2. Restore .NET packages: `cd engine-core/src && dotnet restore`
3. Ensure OBS WebSocket is configured and running

## Key Design Principles
- Vue never controls OBS directly
- Vue never handles real-time playback or timing
- UI produces JSON only
- Engine executes JSON using wall-clock time
- OBS is a dumb renderer
- Architecture designed for 24/7 operation
- Stability prioritized over cleverness

## State Machine
The playout engine follows a deterministic state machine: STOPPED → LOADING → PLAYING → PAUSED → TRANSITIONING → AD INSERTION → OVERLAY UPDATE → ERROR → RECOVERY

## Configuration & Ports
All ports and settings are centralized in config files:
- `config/appsettings.json` - Main configuration
- `config/engine.json` - Engine-specific settings
- `config/ui.json` - UI-specific settings

## Schema Versioning
The timeline schema is versioned to ensure compatibility:
- Current version: `timeline-schema.v1.json` (STABLE)
- Schema version must be specified in schedule files as "version": "1.0"

## Development Scripts
For integrated development, use:
- Windows: `./dev.ps1` or `dev.bat`

This starts both engine and UI simultaneously.

See docs/architecture.md for detailed specifications.