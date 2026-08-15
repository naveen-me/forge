# Roadmap

## Phase 0 — Repository foundation

Deliver:

- build system
- dependency manifest
- basic executable
- logging
- configuration
- CI
- Docker skeleton

Exit criteria:
- clean build in documented Linux environment
- tests execute

## Phase 1 — Architecture POC

Goal:

`WPE HTML + video + image -> GPAC CPU compositor -> 1080p30`

Tasks:

1. Build GPAC with required CPU compositor capabilities.
2. Build/use WPE WebKit + WPEPlatform headless backend.
3. Render a controlled HTML page.
4. Obtain an offscreen frame/buffer.
5. Feed that frame into the compositor.
6. Add an MP4 video source.
7. Add a PNG/JPEG image.
8. Produce 1920x1080/30 output.
9. Measure CPU, RAM and dropped frames.
10. Run a soak test.

Exit criteria:
- no screenshot-per-frame pipeline
- no GPU requirement
- stable output
- benchmark report committed

If this phase fails, stop and investigate before proceeding.

## Phase 2 — Core scene model

Implement:

- canvas
- layers
- source types
- start/end
- z-order
- same-layer replacement
- gaps/fallback
- deterministic ordering
- time conversion

Tests:
- timeline boundary tests
- overlap tests
- exact-start tests
- same-layer replacement tests

## Phase 3 — Source manager

Implement:

- local file
- HTTP/HTTPS media
- HLS
- SRT
- capability reporting
- source errors
- reconnect
- cache
- preload

## Phase 4 — Runtime updates

Implement:

- add layer
- patch layer
- replace layer
- delete
- hide/show
- immediate update
- scheduled update
- atomic revision
- WebSocket events

Benchmark:
- property update latency
- source replacement latency

## Phase 5 — Rendering features

Implement:

- image
- video
- text
- HTML
- opacity
- crop
- scale
- position
- rotation
- fade
- scroll
- basic transitions

## Phase 6 — Audio

Implement:

- video audio
- global mix
- silence/failure handling
- synchronization
- output audio

## Phase 7 — Output

Implement:

- H.264
- AAC
- muxing
- RTMP
- reconnect
- output metrics

## Phase 8 — Reliability

Implement:

- process supervision
- source recovery
- WPE recovery
- output recovery
- bounded queues
- state persistence
- optional resume
- health endpoint

## Phase 9 — Docker/VPS

Test:

- 2 vCPU / 4GB
- 4 vCPU / 8GB
- 8 vCPU / 16GB

Record:

- CPU
- RAM
- FPS
- dropped frames
- latency
- network
- source startup

## Phase 10 — 24-hour soak

Run realistic scenes for at least 24 hours.

Acceptance:
- no crash
- no memory growth indicating a leak
- no unbounded queues
- stable frame timing
- output recovery works

## Phase 11 — Documentation and release

Deliver:

- API docs
- schema docs
- example scenes
- deployment guide
- troubleshooting
- dependency/license report
- benchmark report
- known limitations
- versioned release
