# Architecture Audit Report — TARVA Headless Playout Engine

Date: 2026-08-16
Auditor: autonomous engineering agent
Scope: verify the findings of the independent architecture review against the actual source, per `AGENTS.md` ("verify each of these findings against the actual source").

Source of truth documents read before auditing: `AGENTS.md`, `PROJECT_SPEC.md`, `ARCHITECTURE.md`, `DECISIONS.md`, `ROADMAP.md`, `ACCEPTANCE_TESTS.md`, `README.md`, `research/GPAC-WPE.md`, `research/Protocol-Sources.md`, `DEPLOYMENT.md`, `API.md`, `SETUP.md`, `Dockerfile`, `CMakeLists.txt`, and every file under `src/`, `include/`, `tests/`, `benchmarks/`.

---

## Verdict

Every finding in the independent architecture review is **CONFIRMED** against the source, and the audit found several additional deviations of the same class (claims recorded as verified that the source does not support). The current implementation **does not implement the approved architecture**. It is a Cairo/GTK prototype that links (but never calls) GPAC and WPE libraries, requires Xvfb, has no real frame pacing, has no real RTMP validation, and its acceptance documentation contains claims contradicted by the committed benchmark evidence.

The approved architecture is:

```text
Our Engine -> WPEPlatform/WPE buffer -> GPAC CPU compositor -> FFmpeg/output -> RTMP
```

The current implementation is:

```text
Our Engine -> WebKitGTK offscreen window -> gtk_widget_draw into Cairo -> Cairo composite -> FFmpeg -> local MP4
```

---

## Review finding 1 — GpacCompositor is Cairo-based, not GPAC

**CONFIRMED.**

`src/gpac_compositor.cpp` is pure Cairo:

- `src/gpac_compositor.cpp:11` — `cairo_image_surface_create(CAIRO_FORMAT_ARGB32, width_, height_)` creates the canvas.
- The entire `render_frame` (lines 38–186) uses `cairo_create`, `cairo_paint`, `cairo_set_source_surface`, `cairo_paint_with_alpha`, rotation via `cairo_rotate`, etc.
- `include/gpac_compositor.h:4` — `#include <cairo.h>`; the class holds `cairo_surface_t* canvas_surface_`.
- A repository-wide search for GPAC API usage (`GF_`, `gp_`, `gpac`) finds **zero** GPAC calls in `src/` or `tests/`. `libgpac` is linked (`CMakeLists.txt:11` `pkg_check_modules(GPAC REQUIRED gpac)`; `CMakeLists.txt:59` link) but never called.

The class name claims GPAC; the implementation is a hand-rolled Cairo compositor. GPAC's compositor (filter mode / `GF_Compositor` C API, per `https://github.com/gpac/gpac/wiki/compositor` and `https://doxygen.gpac.io/group__compose__grp.html`) is never used.

## Review finding 2 — WpeHtmlRenderer uses WebKitGTK/GTK offscreen, not WPEPlatform/WPEBuffer

**CONFIRMED.**

`src/wpe_html_renderer.cpp` is WebKitGTK with a GTK offscreen window:

- `src/wpe_html_renderer.cpp:4` — `#include <gtk/gtk.h>`.
- `src/wpe_html_renderer.cpp:19–22` — `gtk_init_check(...)`.
- `src/wpe_html_renderer.cpp:49` — `webkit_web_view_new_with_context(...)` (WebKitGTK API).
- `src/wpe_html_renderer.cpp:57` — `gtk_offscreen_window_new()`.
- `src/wpe_html_renderer.cpp:149` — `gtk_widget_draw(GTK_WIDGET(web_view_), cr)` captures the frame into a Cairo surface.
- `CMakeLists.txt:14` — `pkg_check_modules(WEBKIT REQUIRED webkit2gtk-4.1)`.
- `CMakeLists.txt:15–16` — `wpe-1.0` and `wpebackend-fdo-1.0` are required but **never referenced by any source file** (decoy dependencies).
- `Dockerfile:40` — runtime installs `xvfb`; `Dockerfile:83` — `ENTRYPOINT ["xvfb-run", "-a", "/app/tarva_playout"]`; every WebKit-dependent test is wrapped in `xvfb-run -a` (`CMakeLists.txt`).

This contradicts:
- `ARCHITECTURE.md` §2 (WPE: "headless rendering", "frame/buffer bridge", "Do not use screenshot-per-frame as the production integration").
- `ARCHITECTURE.md` §9 HTML integration (WPE page -> offscreen rendering surface -> frame/buffer bridge -> compositor input).
- `research/GPAC-WPE.md` ("New work should prefer WPEPlatform over legacy libwpe-style assumptions").
- `DEPLOYMENT.md` / `PROJECT_SPEC.md` §14: production container must not require X11; the current Dockerfile requires Xvfb at runtime.

The class name claims WPE; the implementation is WebKitGTK. The WPEPlatform API (`WPEDisplayHeadless`, `WPEView`, `WPEBuffer`) is not used at all.

## Review finding 3 — Committed benchmark says 25.75 FPS while acceptance docs report 31.66 FPS

**CONFIRMED — the acceptance documentation contradicts the committed evidence.**

- `benchmarks/poc_results.json` (the committed evidence): `renderedFps` = **25.75**, `avgFrameTimeMs` = 38.73, `cpuUsagePercent` = 168.47, `ramRssMb` = 244, `totalFrames` = 150, `elapsedSeconds` = 5.83.
- `DECISIONS.md` ADR-016 agrees with the committed JSON (25.75 FPS).
- `ACCEPTANCE_TESTS.md` Gate D and `docs/ACCEPTANCE_REPORT.md` claim **31.66 FPS**, avg frame time 31.48 ms, CPU 183.5%. The acceptance report even contains a fabricated-looking pipeline timing breakdown (compositing 30.02 ms + encoding 14.80 ms = "31.66 FPS") that matches neither the committed JSON nor any instrumented source (the benchmark harness records only aggregate frame time, `src/benchmark_harness.cpp`).

The 31.66 FPS claim in the acceptance documents is **not supported by repository evidence**. The committed evidence is 25.75 FPS, which also fails the project's own 30 FPS target.

## Review finding 4 — Benchmark accepts >= 25 FPS instead of >= 30 FPS

**CONFIRMED.**

- `benchmarks/run_poc_benchmark.cpp:207` — `if (m.rendered_fps >= 25.0 && m.dropped_frames == 0)` declares the POC gate "PASSED".
- `PROJECT_SPEC.md` §27 targets 1920x1080 @ 30fps; the agreed correction target is **>= 30 FPS with zero dropped frames** (per the architecture review).

## Review finding 5 — Benchmark outputs MP4, not real RTMP publishing

**CONFIRMED.**

- `benchmarks/run_poc_benchmark.cpp:37` — `output.initialize("/tmp/poc_output_1080p30.mp4")`.
- `tests/test_output.cpp` — writes `/tmp/test_output.mp4` only; no test in the repository opens an `rtmp://` destination.
- `docs/ACCEPTANCE_REPORT.md` Gate K claims "RTMP publishing support: VERIFIED (Unit) (`MediaOutput` FLV/RTMP protocol output)" — **false**. `MediaOutput::initialize` (`src/media_output.cpp:23–25`) selects the `flv` muxer for `rtmp://`/`rtsp://` URLs, but this path is never exercised by any test or benchmark.

## Review finding 6 — HLS/SRT recognized but not proven by integration tests

**CONFIRMED.**

- `src/source_manager.cpp:9–18` — `supports_protocol` is a string-prefix check. `https://...m3u8` passes because it is `https`; `srt://` passes because of the prefix. No protocol-specific logic, no live-source semantics, no reconnect, no timeout handling.
- `tests/test_source_manager.cpp:22–23` — the "HLS" and "SRT" tests are only `assert(manager.supports_protocol(...))` prefix checks. No real `.m3u8` is ever demuxed and no SRT socket is ever opened in any test.
- `docs/ACCEPTANCE_REPORT.md` Gate F marks HLS and SRT "VERIFIED (Unit)" — **false**.
- `research/Protocol-Sources.md` requires "Live HLS must not be treated like a finite MP4 timeline" and "SRT ... source lifecycle/reconnect behavior matters". Neither exists.

## Review finding 7 — No monotonic deadline-based frame scheduler

**CONFIRMED.**

- `src/main.cpp:100–104` — pacing is a naive `sleep_for(target_ms - elapsed_ms)` after work completes. There is no deadline tracking, no catch-up policy, no late-frame accounting, and `output.send_frame_rgba(...)`'s return value is ignored (`src/main.cpp:92`), so the runtime loop cannot even detect dropped frames.
- The playout clock is derived from frame count (`src/main.cpp:84` `current_pts_ns = frame_idx * frame_duration_ns`), which drifts from wall time whenever a frame is slow.
- `benchmarks/run_poc_benchmark.cpp` has **no pacing at all** — it renders all 150 frames back-to-back as fast as the CPU allows (measured elapsed 5.83 s) and reports "FPS" as frames/elapsed. This is a throughput measurement, not a real-time measurement.

---

## Additional findings from the audit (same class as the review findings)

### A1. The HTML layer is a static cached snapshot, not per-frame WPE rendering

`HtmlSource::load` captures **one** frame (`src/media_sources.cpp:317`) and `read_frame_rgba` replays the cached buffer forever (`src/media_sources.cpp:326–329` — `if (has_cached_frame_ ...) std::memcpy(...)`). Consequences:

- The POC benchmark never exercises per-frame WPE rendering; it copies a pre-captured buffer.
- `docs/ACCEPTANCE_REPORT.md`'s "WPE Offscreen Buffer Retrieval: 0.1 ms (cached)" **admits** the caching.
- Browser-side animation / data-driven HTML graphics (a stated WPE use case in `ARCHITECTURE.md` §2 and `research/GPAC-WPE.md`) cannot work.
- The claim "WPE HTML layer renders live in the 1080p30 POC" is **false** as implemented.

### A2. Video sources are not timestamp-aligned to the global clock

`VideoSource::read_frame_rgba` (`src/media_sources.cpp:108–139`) ignores `pts_ns` and decodes the next frame sequentially; `seek()` (`src/media_sources.cpp:142–147`) exists but is never called by the compositor or render loop. Consequences:

- Video layer position is not aligned to the playout clock (violates `PROJECT_SPEC.md` §16 "frame timestamp alignment").
- `sourceStart`/trim is not implemented anywhere (schema has no field; spec §16 requires it).
- If the render loop runs faster or slower than 30 fps, video drifts.

### A3. GPAC and WPE libraries are build-time requirements that are never used

`CMakeLists.txt:11,15,16` require `gpac`, `wpe-1.0`, and `wpebackend-fdo-1.0` pkg-config modules; zero source files reference their APIs. The build would fail on a machine without them even though the code is Cairo/GTK. `Dockerfile:10–17,65–66` installs `gpac`/`libgpac` and builds `libwpe` + `WPEBackend-fdo` from source — all dead weight for the current code.

### A4. Production container requires Xvfb

`Dockerfile:40` (`xvfb`), `Dockerfile:83` (`xvfb-run` entrypoint), and every WebKit-dependent test under `xvfb-run`. Contradicts `ARCHITECTURE.md` §14 ("no X11 dependency for the intended path") and `DEPLOYMENT.md` ("not require X11").

### A5. Benchmark queue is unbounded despite "bounded queues" claims

`benchmarks/run_poc_benchmark.cpp:80–102` — `std::queue<QueuedFrame> frame_queue` has no capacity limit; the producer pushes without backpressure. `docs/ACCEPTANCE_REPORT.md` Gate L "Bounded queues: VERIFIED" is **false**.

### A6. /status does not expose the metrics API.md requires

`src/api_server.cpp:20–30` — `/status` returns only `status`, `canvas`, `revision`, `layerCount`. `API.md` requires playout time, FPS, dropped frames, active layers, source states, output state. `DEPLOYMENT.md` ("Operational checks") requires the same.

### A7. No audio in the output pipeline

`MediaOutput` opens a video-only stream (`src/media_output.cpp:31–34`); `AudioMixer` (`src/audio_mixer.cpp`) is a standalone utility never wired into the engine or output. `test_effects` only mixes two tiny in-memory PCM buffers. `ACCEPTANCE_TESTS.md` Gate K "Audio encoding / mixing works" and Gate A "9/9 CTest suites pass" are at minimum misleading; spec §23 requires audio in the output pipeline.

### A8. Timeline tie-breaker comment claims a revision field that does not exist

`src/timeline_engine.cpp:44–46` — the same-start tie-breaker comment says "later revision", but `Layer` has no revision/sequence field; the code always picks the later item in the scene vector. The behavior is deterministic (spec §6 requires "monotonically increasing revision/sequence value"), but the implementation does not track a per-item sequence as specified.

### A9. Benchmark FPS is throughput, not real-time output

Because the benchmark loop has no pacing (finding 7), `renderedFps`/`outputFps` in `benchmarks/poc_results.json` measure CPU throughput of the pipeline, not real-time playout at 30 fps. This invalidates both the 25.75 and 31.66 numbers as "real-time" evidence.

---

## What was NOT verifiable in this environment

This audit environment has no toolchain (no `pkg-config`, `cmake`, compiler, `xvfb`, `ffmpeg`). Gate A claims (clean build, Docker build, "9/9 CTest suites pass") could not be re-run here; they are marked UNVERIFIED in the corrected status tables below. Nothing in the source contradicts the possibility that the code compiles — the audit is about what the code **does**, not whether it builds.

---

## Corrected acceptance status

| Claimed (acceptance docs) | Actual state per source |
|---|---|
| WPE headless rendering via WpeHtmlRenderer | WebKitGTK + GTK offscreen window; requires Xvfb; not WPEPlatform |
| GPAC CPU compositor | Hand-rolled Cairo compositor; GPAC never called |
| 31.66 FPS @ 1080p30 POC | Committed evidence: 25.75 FPS throughput (no pacing); 30fps target not met |
| POC gate >= 25 FPS = PASS | Gate criterion wrong; target is >= 30 FPS, 0 dropped frames |
| RTMP publishing verified | Only MP4 tested; rtmp:// path never exercised |
| HLS/SRT verified | String-prefix capability checks only; no real source integration tests |
| Per-frame WPE HTML in benchmark | Single cached frame replayed via memcpy |
| Bounded queues verified | Benchmark queue is unbounded |
| Frame pacing | Naive sleep loop in main; none in benchmark |
| /status operational metrics | Missing FPS, drops, source states, output state |
| Audio in output pipeline | AudioMixer standalone; no audio stream in MediaOutput |

---

## Conclusion

The POC gate described in `AGENTS.md` and `ROADMAP.md` Phase 1 **has not actually been passed**: the real WPEPlatform -> buffer -> GPAC CPU compositor path at 1920x1080/30fps was never built or measured. The current codebase is a functional Cairo/WebKitGTK prototype with honest engineering in its own right (timeline semantics, atomic scene updates, JSON schema, FFmpeg encode pipeline all exist and are tested at unit level), but it is not the approved architecture and its documentation overstates what was proven.

Per `AGENTS.md`: do not silently replace GPAC with Cairo or WPE with WebKitGTK/GTK. The correction path is defined in `docs/CORRECTION_PLAN.md` and recorded in `DECISIONS.md` (ADR-017..ADR-020).
