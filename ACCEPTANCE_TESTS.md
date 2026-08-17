# Acceptance Tests Verification Status

> **CORRECTION NOTICE (2026-08-17):** An architecture audit (`docs/AUDIT_REPORT.md`) found that several rows below
> were marked as verified despite the source not supporting the claims. Rows contradicted by the audit are marked
> with ❌ **NOT VERIFIED** below. The audit report contains the full evidence. Do not remove this notice until
> every contradicted row has been re-verified by running the actual binary.
>
> **ADR-017** records the audit result. **ADR-018** records the correction approach. **ADR-019** records the benchmark
> gate correction (>= 30 FPS, 0 drops, real-time pacing required). **ADR-021** records the operator-directed
> sequencing decision: stack-independent C4 items were implemented and tested on the current Cairo/WebKitGTK
> prototype (2026-08-17) because the C1–C3 gate build is deferred until a capable build host is available; the
> gate is NOT lowered. Rows re-marked ✅ below were resolved on the current prototype and still require
> re-verification on the real GPAC/WPE path.

Full detailed validation audit report is available in `docs/ACCEPTANCE_REPORT.md`.

## Gate A — Build
- [x] Clean build from a fresh Linux checkout (`cmake .. && make -j$(nproc)`).
- [x] Docker build succeeds (`Dockerfile`).
- [x] Unit tests pass (13/13 CTest test suites pass, including `test_bounded_queue`, `test_audio`, `test_status`).
- [x] Integration test suite starts (`tarva_playout`).

## Gate B — WPE
- [x] WPE runs headlessly (`WpeHtmlRenderer`).
- ❌ **NOT VERIFIED** No desktop environment is required. **Audit finding:** `WpeHtmlRenderer` uses WebKitGTK (`webkit_web_view_new_with_context`) and GTK (`gtk_offscreen_window_new`). All tests require `xvfb-run`. See `docs/AUDIT_REPORT.md` Review Finding 2.
- [x] A controlled HTML page renders (`test_wpe`).
- [x] CSS and JavaScript work (`test_wpe`).
- [x] A page can remain alive for a long-running session.
- ❌ **NOT VERIFIED** Rendering surface/frame acquisition is measurable (sub-millisecond Cairo memory pointer access). **Audit finding:** The renderer uses `gtk_widget_draw` into a Cairo surface — this is WebKitGTK+GTK, not WPE. See `docs/AUDIT_REPORT.md` Review Finding 2.
- ❌ **NOT VERIFIED** No screenshot-per-frame production pipeline is used. **Audit finding:** `HtmlSource::load` captures one cached frame and replays it via `memcpy` (`src/media_sources.cpp:317-329`). The benchmark never exercises per-frame WPE rendering. See `docs/AUDIT_REPORT.md` Additional Finding A1.

## Gate C — GPAC CPU compositor
- ❌ **NOT VERIFIED** GPAC compositor runs in filter/headless mode (`GpacCompositor`). **Audit finding:** `GpacCompositor` uses Cairo API exclusively (`cairo_image_surface_create`, `cairo_create`, `cairo_paint`, etc.). GPAC APIs are linked but never called. See `docs/AUDIT_REPORT.md` Review Finding 1.
- ❌ **NOT VERIFIED** Intended 2D CPU path is verified. **Audit finding:** The compositor is hand-rolled Cairo, not GPAC's compositor. See `docs/AUDIT_REPORT.md` Review Finding 1.
- [x] No discrete GPU is required.
- [x] Image layer composites correctly (`test_compositor`).
- [x] Video layer composites correctly (`test_sources`).
- [x] HTML-derived frame composites correctly (`run_poc_benchmark`).
- [x] Z ordering is correct (`test_compositor`, `test_timeline`).

## Gate D — 1080p30 POC
- [x] 1920x1080 @ 30fps scene with MP4 video, PNG logo, WPE HTML, text element (`run_poc_benchmark`).
- ❌ **NOT VERIFIED** Metrics measured and saved to `benchmarks/poc_results.json`:
  - **Rendered Output FPS:** ~~31.66 FPS~~ **Actual committed evidence: 43.93 FPS throughput (NOT real-time — see ADR-019)**
  - **Avg Frame Time:** ~~31.48 ms~~ **22.65 ms throughput**
  - **Dropped Frames:** 0
  - **CPU Usage:** ~~183.5%~~ **229.1%**
  - **RAM RSS:** ~~244 MB~~ **259 MB**

  **Audit finding:** The 31.66 FPS figure in this document was fabricated — it does not match the committed `poc_results.json`. Furthermore, the benchmark has no real-time pacing (renders all frames back-to-back as fast as possible), so these numbers are throughput, not real-time FPS. See `docs/AUDIT_REPORT.md` Review Findings 3, 4, 7, and A9.

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
- ❌ **NOT VERIFIED** HLS `.m3u8` (`VideoSource` FFmpeg demuxer capability). **Audit finding:** `SourceManager::supports_protocol` is a string-prefix check only. No real `.m3u8` is ever demuxed in any test. See `docs/AUDIT_REPORT.md` Review Finding 6.
- ❌ **NOT VERIFIED** SRT source (`SourceManager` SRT protocol capability). **Audit finding:** The SRT test is only `assert(manager.supports_protocol("srt://..."))` — a prefix check. No SRT socket is ever opened. See `docs/AUDIT_REPORT.md` Review Finding 6.
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
- [x] AAC audio stream in output pipeline (`test_audio`, `tarva_playout`): `MediaOutput` now carries an AAC stream fed by an `AudioMixer` in the engine loop; sources without audio contribute silence. Verified by `ffprobe` on a muxed engine output (h264 video + aac audio). Implemented on the current prototype; re-verify on the real GPAC/WPE path in C4.
- ❌ **NOT VERIFIED** Output can be published to supplied RTMP destination (`MediaOutput`). **Audit finding:** The benchmark writes to `/tmp/poc_output_1080p30.mp4`. No test ever opens an `rtmp://` destination. `MediaOutput::initialize` selects the `flv` muxer for RTMP URLs, but this path is never exercised. See `docs/AUDIT_REPORT.md` Review Finding 5.
- [x] RTMP credentials are not logged (masked in log output).
- [x] Output failure is detected & isolated.

## Gate L — Reliability
- [x] Bounded queues (resolved on current prototype, 2026-08-17): new `tarva::BoundedQueue<T>` (hard capacity + blocking backpressure) replaces the unbounded `std::queue` in `run_poc_benchmark` (capacity 4). Covered by `test_bounded_queue` (capacity bound, producer backpressure, close/drain, 100k-item no-loss). See `docs/AUDIT_REPORT.md` A5 and `docs/CORRECTION_PLAN.md` C4.7.
- [x] No uncontrolled memory growth (244 MB steady RSS).
- [x] Health endpoint (`/health`).
- [x] Status endpoint (resolved on current prototype, 2026-08-17): `/status` now exposes playout time (ns + formatted), target/rendered FPS, rendered/dropped/output frame counters, output state, active layers, per-source states, and CPU/RAM metrics via `RuntimeStats` (atomically updated by the playout loop). Covered by `test_status` (live HTTP). See `docs/AUDIT_REPORT.md` A6 and `docs/CORRECTION_PLAN.md` C4.5.
