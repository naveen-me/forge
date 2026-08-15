# TARVA Headless Playout Engine

This repository is the project brain for a headless, CPU-first, JSON-driven real-time playout engine intended to run on ordinary Linux VPS infrastructure without requiring a GPU.

## Primary architecture

- **Our engine:** timeline, layer state, scheduling, hot updates, source management, orchestration and API.
- **GPAC:** media/compositing foundation, with the intended CPU 2D compositor path.
- **WPE WebKit:** headless HTML/CSS/JavaScript rendering.
- **FFmpeg/libav:** media decoding/encoding and final output integration.
- **RTMP:** an output protocol handled by the media/output layer; the core engine does not implement the RTMP protocol itself.

## Critical first gate

Do NOT implement the entire product before proving:

`WPE HTML -> rendered frame/buffer -> GPAC CPU compositor -> 1920x1080 @ 30fps`

with at least:

- one video
- one image
- one HTML layer
- one text/graphics element
- measurable CPU/RAM/frame-drop results

If this gate fails, stop and investigate alternatives. Do not silently replace the approved architecture.

## User-facing model

Everything is a layer.

- `layer` is the z-index.
- There is no special `program`, `main`, or `overlay` concept.
- One global playout clock is the single source of timeline truth.
- `start` and `end` are absolute positions on that clock.
- Same layer: a later-starting active item replaces the previous item.
- Different layers: they coexist.
- Visual ordering is determined by layer/z-index.
- Local files and URL sources are supported.
- HTML sources are rendered by WPE.
- HLS/SRT and other media protocols are supported when available in the selected backend/build.
- Preloading is opportunistic, never a requirement.
- Updates are hot, atomic and do not require restarting the engine.

Read these files in order:

1. `AGENTS.md`
2. `PROJECT_SPEC.md`
3. `ARCHITECTURE.md`
4. `DECISIONS.md`
5. `ROADMAP.md`
6. `ACCEPTANCE_TESTS.md`
7. `research/GPAC-WPE.md`
8. `research/Protocol-Sources.md`
