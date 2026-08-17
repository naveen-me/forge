# Correction Plan — Restore the Approved Architecture

Reference: `docs/AUDIT_REPORT.md` (verified findings), `AGENTS.md`, `PROJECT_SPEC.md`, `ARCHITECTURE.md`, `DECISIONS.md`, `ROADMAP.md`.

Goal: bring the repository back to the approved architecture and prove it with real, honest evidence:

```text
Our Engine -> WPEPlatform/WPE buffer -> GPAC CPU compositor -> FFmpeg/output -> RTMP
```

Rules that govern every phase:

- No silent component substitution. GPAC stays the compositor; WPE stays the HTML engine; FFmpeg stays the output. Deviations require reproduction, evidence, `DECISIONS.md` update, and the smallest defensible change (per `AGENTS.md`).
- Nothing is "done" because it compiles or has a unit test. Every gate below requires running the real binary, capturing metrics, and committing evidence.
- The production path must be CPU-first with no X11/Xvfb and no discrete GPU.

Sequencing constraint (from the task): **prove `WPEPlatform -> buffer -> GPAC CPU compositor` at 1920x1080/30fps first.** Only after that gate passes do the remaining corrections (C4+) proceed.

---

## Phase C0 — Repository integrity (no code behavior changes)

Small, immediate fixes so the repository stops asserting false evidence:

1. `benchmarks/run_poc_benchmark.cpp:207` — change gate to `rendered_fps >= 30.0 && dropped_frames == 0`.
2. `benchmarks/poc_results.json` — keep the committed 25.75 numbers (they are the real evidence) but rename/label the report as *throughput*, not real-time FPS, until a paced run replaces it. Add a README note in `benchmarks/`.
3. `ACCEPTANCE_TESTS.md` and `docs/ACCEPTANCE_REPORT.md` — add a prominent correction notice referencing `docs/AUDIT_REPORT.md`; mark the contradicted rows (31.66 FPS, RTMP "verified", HLS/SRT "verified", bounded queues, WPE/GPAC claims) as NOT VERIFIED / CONTRADICTED. Do not delete the history — annotate it.
4. `DECISIONS.md` — record ADR-017 (audit result), ADR-018 (correction approach), ADR-019 (benchmark gate >= 30 FPS, 0 drops, real-time pacing required), ADR-020 (WPEPlatform headless path + GPAC compositor filter path).
5. `docs/CORRECTION_PLAN.md` (this file) — living plan, updated as phases complete.

Exit criteria: acceptance docs no longer claim anything the source does not support; git tree clean with the audit + plan committed.

## Phase C1 — Prove the WPEPlatform -> CPU-readable buffer path (no X11)

Goal: a headless WPE renderer that produces CPU-readable RGBA frames at 1920x1080 without Xvfb, GPU, or a display server.

Research conclusions (verified against upstream, Aug 2026):

- WPEPlatform is the current platform integration layer inside WPE WebKit (replaces libwpe API at 2.54; available from 2.48/2.52). It provides built-in `WPEDisplay` implementations including **headless output** (`WPEDisplayHeadless`, `wpe_display_headless_new()`), `WPEView`, and `WPEBuffer` classes. pkg-config module: `wpe-webkit-2.0` (WPE WebKit >= 2.52).
- Real-world reference: `merely-made/wgpu-scry` builds exactly this (`WPEDisplayHeadless` + `WebKitWebView` WPE variant) and exports frames. Its documented constraints apply to us:
  - **Thread-affine producer**: the display/view must be constructed and driven from one thread pumping the glib main context.
  - **One WPE display per process**: a second display in one process can SIGABRT/hang. For multiple HTML layers, either use one display with multiple views (verify) or isolate per subprocess.
  - **Headless toplevel resize is a no-op** on 2.52: the HTML layer's buffer size must be chosen at construction time (matches our layer model: a layer's width/height are fixed at prepare time).
- wgpu-scry exports GPU DMABUF frames; our CPU-first requirement needs the CPU-readable buffer path. Verify: `WPEBuffer` contents with CPU access (shared-memory/shm or CPU-mapped contents) for the headless display. Fallback if headless EGL/GBM bootstrap demands a GPU context: Mesa **llvmpipe** software GL (`LIBGL_ALWAYS_SOFTWARE=1`, surfaceless EGL) — still CPU-only, still no X server.

Implementation steps:

1. Install/build WPE WebKit with WPEPlatform. Prefer distro packages if present (check `libwpewebkit-2.0-dev`/`libwpewebkit-1.1-dev` on Ubuntu 26.04; else build WPE WebKit >= 2.52 from source per the existing `Dockerfile` pattern).
2. New `WpeHtmlRenderer` (replacing the WebKitGTK implementation in `src/wpe_html_renderer.cpp`):
   - Own dedicated render thread; glib `MainContext` pumped synchronously (thread-affine).
   - Construct `WPEDisplayHeadless` + `WPEView`/WebKitWebView at the layer's exact width/height.
   - Register a frame callback; convert `WPEBuffer` to CPU-readable RGBA (zero-copy where the buffer contents are CPU-mapped; explicit copy otherwise).
   - `load_url` / `load_html` with load-changed signaling and timeout (reuse current API surface so `MediaSource`/`HtmlSource` interfaces stay stable).
   - No Cairo, no GTK, no `gtk_widget_draw`.
3. `CMakeLists.txt` — replace `webkit2gtk-4.1` with `wpe-webkit-2.0` (+ keep `wpe-1.0`/`wpebackend-fdo-1.0` only if the headless path needs them); drop `xvfb-run` from the WPE test targets.
4. `Dockerfile` — runtime: drop `xvfb` and `libwebkit2gtk-4.1-0`; add WPE runtime libs; entrypoint runs the binary directly.
5. Test (`tests/test_wpe.cpp` rework): controlled HTML with CSS + JS; assert pixels; assert page stays alive; measure per-frame capture cost over N frames (must be well under 33 ms at 1080p); run WITHOUT `xvfb-run`.

Gate C1 (PASS to continue): headless WPE renders a controlled page to a CPU-readable 1920x1080 RGBA buffer on this machine with no X server, no GPU, no Xvfb; capture cost measured and committed to `docs/` or `benchmarks/`.

## Phase C2 — Prove the GPAC CPU compositor consumes the buffer

Goal: GPAC's compositor (not Cairo) composites video + image + WPE HTML frames in CPU 2D mode.

Research conclusions (verified against upstream GPAC docs):

- GPAC compositor operates in **filter-only mode** as a regular filter generating frames based on the loaded scene (`https://github.com/gpac/gpac/wiki/compositor`, moved to wiki.gpac.io).
- Options relevant to the CPU path: `ogl=off` disables OpenGL (pure software 2D; `hybrid` = software 2D + GL only for textured/3D objects); `mode2d=immediate` redraws the full screen each frame; `opfmt=rgba` sets output pixel format; `fps` drives output when only graphics are present.
- Scene description can be decoupled from source media via the `gpid://` URL scheme (Background2D node); pass-through mode ties scene clock to input frame times.
- Two integration options, both acceptable:
  - (a) Direct C API: `GF_Compositor` from `<gpac/compositor.h>` inside our process (no subprocess).
  - (b) Filter graph: build a `GF_FilterSession` programmatically (`gf_fs_*` API) with source filters feeding the `compositor` filter, then a raw video output PID consumed by our encoder stage.
  - Prefer (a) if it exposes the same options; otherwise (b). Decision recorded in `DECISIONS.md`.

Implementation steps:

1. New `GpacCompositor` implementation using the GPAC compositor path; keep the existing public interface (`render_frame(active_layers, pts_ns, output_rgba)` and `RenderableLayer`) so `TimelineEngine`, `SceneController`, and tests don't churn.
2. Feed three PID types in: FFmpeg-decoded video frames (we already decode with libav; hand frames to GPAC), image frames (decoded once, re-fed), and WPE HTML frames from C1's buffer bridge.
3. Benchmark composition cost alone at 1920x1080: per-frame composite time must fit the 33.3 ms budget with the rest of the pipeline.
4. Test (`tests/test_compositor.cpp` rework): z-order, background fill, opacity, effects, same-layer replacement visual results — assert on output pixels, but now through the real GPAC path.

Gate C2 (PASS to continue): GPAC compositor (software 2D, no GL driver) composites WPE HTML + video + image + text at 1920x1080; per-frame composite time measured and documented; unit tests pass against the real path.

## Phase C3 — 1080p30 POC benchmark on the real path (the gate)

Goal: prove the full chain at 1920x1080/30fps with zero dropped frames, CPU-first, and commit honest evidence.

1. Rebuild `benchmarks/run_poc_benchmark.cpp` on the real path:
   - WPE per-frame capture (not a cached snapshot — remove the `has_cached_frame_` shortcut or make it configurable and off for the benchmark).
   - GPAC compositor from C2.
   - FFmpeg H.264 output (existing `MediaOutput`).
   - **Deadline-based scheduler**: monotonic clock; compute next frame deadline = start + n * frame_duration; work until deadline; on overrun, decide drop-or-render-late by policy, count and expose drops; measure per-frame jitter (p50/p95/max), not just the mean.
   - **Bounded queue** between composite and encode with explicit capacity and backpressure.
2. `BenchmarkHarness` additions: real-time pacing enforcement, frame interval histogram, deadline misses, dropped frames as a first-class metric; report labeled as real-time.
3. Correct `tests/` and docs that rely on the old metrics (ADR-019).
4. Replace `benchmarks/poc_results.json` with the new honest run.

Gate C3 = the POC gate from `AGENTS.md`/`ROADMAP.md` Phase 1:
- >= 30 FPS rendered and output, 0 dropped frames, at 1920x1080, over a meaningful soak (start 10 min; stretch to 1 h).
- CPU-only (no discrete GPU, no X server, no Xvfb).
- Metrics committed: FPS, avg/p95 frame time, dropped frames, CPU %, RAM RSS, WPE capture cost, composite cost, encode cost.
- If the gate fails: investigate, reproduce, record evidence, benchmark alternatives, update `DECISIONS.md` before changing anything (per `AGENTS.md`). Do NOT lower the threshold.

## Phase C4 — Remaining corrections (only after C3 passes)

> **Status update (2026-08-17, ADR-021):** the operator directed that the stack-independent C4 items
> (timestamps, status/metrics API, audio pipeline, bounded queues) be implemented and tested on the
> current Cairo/WebKitGTK prototype before the C1–C3 gate, because the local host cannot host the WPE
> WebKit source build. Those items are DONE on the current prototype (marked ✅ below) and carry over to
> the corrected architecture; they still require re-verification against the real GPAC/WPE path after C3.

Ordered by the task instructions ("only after that proceed with the remaining corrections and features"):

1. **RTMP output validation (real)**: stand up a local RTMP server (e.g., nginx-rtmp or SRS in a Docker container) and add an integration test that publishes to it, verifies the stream with an RTMP client/player, validates reconnect after server restart, and asserts stream keys are masked in logs. Add `benchmarks/rtmp_*` evidence.
2. **HLS/SRT real integration tests**: local HLS server (ffmpeg `hls` muxer + `http.server`) test that demuxes a live `.m3u8`; SRT loopback (ffmpeg `srt` listener mode) test of ingest, playout, and reconnect. Implement live-source semantics per `research/Protocol-Sources.md` (no finite-timeline assumptions, reconnect/timeouts).
3. **Frame/timestamp correctness**: ✅ **DONE (current prototype)** — `VideoSource::read_frame_rgba` presents frames at the requested global-clock pts: bounded presentation-order reorder buffer (`has_b_frames + 1`), refcounted AVFrames, keyframe-seek on backward jumps, clock-mapped looping with decoder flush at EOF. Tested in `test_sources`. Remaining: `sourceStart`/trim per `PROJECT_SPEC.md` §16, and re-verify on the real path.
4. **Hot-update + atomicity hardening**: apply scene revisions at frame boundaries (currently applied between frames under a mutex — acceptable, but verify atomicity under load with a test); expose revision/version on layers for the deterministic tie-breaker (spec §6).
5. **Status/metrics API**: ✅ **DONE (current prototype)** — `/status` returns playout time, target/rendered FPS, rendered/dropped/output frames, output state, active layers, source states, CPU/RAM via `RuntimeStats`; `test_status` verifies over live HTTP. Remaining: re-verify on the real path.
6. **Audio in output pipeline**: ✅ **DONE (current prototype)** — `MediaOutput` AAC stream (S16 -> FLTP, sample-accurate pts), `VideoSource` audio decode (isolated demux context, 48 kHz stereo S16), engine loop mixes active-layer audio with `AudioMixer` (silence otherwise). Engine output verified by ffprobe (h264 + aac); `test_audio` covers decode/mix/mux. Remaining: audio failure isolation test, re-verify on the real path.
7. **Failure isolation + recovery**: source ERROR states already exist in `SourceManager`; add reconnect for live sources (HLS/SRT/RTMP), WPE page recovery, output reconnect; ✅ bounded queues (audit A5) resolved on the current prototype via `tarva::BoundedQueue` (used in the benchmark; `test_bounded_queue`). Remaining: audit all other producer/consumer boundaries and re-verify on the real path.
8. **No-X11 runtime finalized**: remove all `xvfb-run` usage from tests and Docker; delete `webkit2gtk`/GTK remnants; clean decoy deps from `CMakeLists.txt`.
9. **Security pass** (spec §29): SSRF policy for HTML/media URLs, credential masking verified by test, resource limits.
10. **Soak test** (Phase 10): 24 h run; no leaks, no unbounded queues, stable timing, reconnect proven.

## Phase C5 — Documentation & release

Update `ACCEPTANCE_TESTS.md` with real verification status, `DECISIONS.md` with all new ADRs, benchmark results, `LICENSES-AND-DEPENDENCIES.md` (drop WebKitGTK, add WPE WebKit/WPEPlatform), `DEPLOYMENT.md` (no Xvfb), `JSON_SCHEMA.md`, `API.md`. Update `README.md` and `FILE_INDEX.md`. Only then declare production-ready per `ACCEPTANCE_TESTS.md`.

---

## Risk register (top items)

| Risk | Mitigation |
|---|---|
| WPEPlatform headless requires GPU/EGL bootstrap on some distros | Verify CPU/shm buffer path first (C1 gate); fallback Mesa llvmpipe/surfaceless EGL (still CPU-only, no X); record evidence in `DECISIONS.md` |
| WPE WebKit not packaged for Ubuntu 26.04 | Build from source (existing Dockerfile pattern); pin version; document build time |
| GPAC compositor filter mode performance at 1080p30 | Measure composite cost alone in C2 before wiring the full chain; `mode2d=immediate`, `ogl=off`, `opfmt=rgba` |
| One-display-per-process constraint vs. multiple HTML layers | Single display + multiple views if supported; else one subprocess per HTML layer (isolation is also a security win) |
| Benchmark regresses to throughput instead of real-time | Deadline-based scheduler in C3; harness reports jitter and deadline misses; gate is hard: >= 30 FPS, 0 drops |
| Scope creep while the gate is unproven | C0–C3 are strictly the POC gate; C4+ only after C3 passes |

## Definition of done for this correction plan

- `docs/AUDIT_REPORT.md` findings resolved: real WPEPlatform renderer, real GPAC compositor, no Xvfb in production path, honest benchmark evidence, real RTMP/HLS/SRT integration tests, deadline-based pacing.
- All acceptance gates re-verified by running the actual binary and committing results.
