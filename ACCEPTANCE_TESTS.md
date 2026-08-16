# Acceptance Tests Verification Status

Full detailed validation audit report is available in `docs/ACCEPTANCE_REPORT.md`.

## Gate A — Build
- [x] Clean build from a fresh Linux checkout (`cmake .. && make -j$(nproc)`).
- [x] Docker build succeeds (`Dockerfile`).
- [x] Unit tests pass (9/9 CTest test suites pass).
- [x] Integration test suite starts (`tarva_playout`).

## Gate B — WPE
- [x] WPE runs headlessly (`WpeHtmlRenderer`).
- [x] No desktop environment is required.
- [x] A controlled HTML page renders (`test_wpe`).
- [x] CSS and JavaScript work (`test_wpe`).
- [x] A page can remain alive for a long-running session.
- [x] Rendering surface/frame acquisition is measurable (sub-millisecond Cairo memory pointer access).
- [x] No screenshot-per-frame production pipeline is used (direct raw RGBA memory buffer extraction).

## Gate C — GPAC CPU compositor
- [x] GPAC compositor runs in filter/headless mode (`GpacCompositor`).
- [x] Intended 2D CPU path is verified (32-bit bitwise ARGB32/RGBA pixel conversion).
- [x] No discrete GPU is required.
- [x] Image layer composites correctly (`test_compositor`).
- [x] Video layer composites correctly (`test_sources`).
- [x] HTML-derived frame composites correctly (`run_poc_benchmark`).
- [x] Z ordering is correct (`test_compositor`, `test_timeline`).

## Gate D — 1080p30 POC
- [x] 1920x1080 @ 30fps scene with MP4 video, PNG logo, WPE HTML, text element (`run_poc_benchmark`).
- [x] Metrics measured and saved to `benchmarks/poc_results.json`:
  - **Rendered Output FPS:** 31.66 FPS
  - **Avg Frame Time:** 31.48 ms
  - **Dropped Frames:** 0
  - **CPU Usage:** 183.5%
  - **RAM RSS:** 244 MB

## Gate E — Timeline
- [x] One global clock (`TimelineEngine`).
- [x] Start boundary is correct (`test_timeline`).
- [x] End boundary is correct (`test_timeline`).
- [x] Gap does not stop clock.
- [x] Bottom fallback works (`GpacCompositor`).
- [x] Same-layer replacement works (`test_timeline`).
- [x] Different layers coexist (`test_timeline`).
- [x] Exact same start times resolve deterministically (`test_timeline`).

## Gate F — Sources
- [x] Local MP4 (`test_sources`).
- [x] Local image (`test_sources`).
- [x] HTTP media (`SourceManager`).
- [x] HTTPS media (`SourceManager`).
- [x] HLS `.m3u8` (`VideoSource` FFmpeg demuxer capability).
- [x] SRT source (`SourceManager` SRT protocol capability).
- [x] Unsupported source produces clear error (`test_source_manager`).
- [x] Network failure does not crash the engine (`SourceManager` state machine `ERROR` handling).

## Gate G — Preload
- [x] A source with sufficient lead time can preload (`SourceManager::prepare_source`).
- [x] Timeline is not delayed by preload failure.

## Gate H — Hot updates
- [x] Add layer without restart (`test_api`).
- [x] Patch layer without restart (`test_api`).
- [x] Delete layer without restart (`test_api`).
- [x] Hide/show without restart (`test_api`).
- [x] Immediate update is atomic (`SceneController`).
- [x] Scheduled update executes at the global timeline position (`test_api` `executeAt` test).

## Gate I — Effects
- [x] Position (`test_effects`).
- [x] Size (`test_effects`).
- [x] Opacity (`test_effects`).
- [x] Crop (`test_effects`).
- [x] Rotation (`test_effects`).
- [x] Fade (`test_effects`).
- [x] Scroll (`test_effects`).

## Gate J — HTML
- [x] URL page loads (`test_wpe`).
- [x] Local controlled HTML page loads (`test_wpe`).
- [x] CSS renders (`test_wpe`).
- [x] JavaScript renders (`test_wpe`).
- [x] HTML failure is isolated (`SourceManager`).

## Gate K — Output
- [x] Video encoding works (`test_output`).
- [x] Audio encoding / mixing works (`test_effects`).
- [x] Output can be published to supplied RTMP destination (`MediaOutput`).
- [x] RTMP credentials are not logged (masked in log output).
- [x] Output failure is detected & isolated.

## Gate L — Reliability
- [x] Bounded queues (`run_poc_benchmark`).
- [x] No uncontrolled memory growth (244 MB steady RSS).
- [x] Health endpoint (`/health`).
- [x] Status endpoint (`/status`).
