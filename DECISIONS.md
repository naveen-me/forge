# Architecture Decisions

## ADR-001 — Everything is a layer

Status: Accepted

There is no special program/main/overlay scheduling concept.

Every visual source is a layer.

Reason:
- simpler JSON
- simpler engine
- flexible composition
- same rules for video, images, HTML, text and ads

## ADR-002 — Layer means z-index

Status: Accepted

`layer` is the numeric z-order.

Higher values render above lower values.

Do not introduce a separate `slot` field.

## ADR-003 — One global playout clock

Status: Accepted

All `start`/`end` values refer to one global timeline.

Reason:
- deterministic behavior
- no conflicting media clocks at the scene level
- simple scheduling
- easy hot updates

## ADR-004 — Same layer uses replacement semantics

Status: Accepted

If multiple items overlap on the same layer, the later-starting item replaces the earlier item.

Different layers may overlap freely.

## ADR-005 — Hot updates

Status: Accepted

Layer state can be changed without restarting the engine.

Updates are atomic and should become visible at a frame boundary.

## ADR-006 — Immediate and scheduled updates

Status: Accepted

Support both:

- immediate update
- update scheduled for an exact playout time

## ADR-007 — Opportunistic preload

Status: Accepted

Preload when there is enough lead time.

Never delay the global timeline because a source was not preloaded.

## ADR-008 — Local and URL sources

Status: Accepted

A source may be local or remote.

Supported protocol capabilities depend on the installed backend/build.

## ADR-009 — GPAC

Status: Accepted as the initial compositor/media foundation

Reason:
- mature multimedia framework
- active current development
- headless/filter operation
- CPU 2D compositor path
- timed composition
- broad media/protocol support
- LGPL licensing

Caveat:
- exact performance and WPE framebuffer integration must be proven in the POC.

## ADR-010 — WPE WebKit

Status: Accepted as the HTML engine

Reason:
- mature embedded WebKit port
- maintained by Igalia/WebKit ecosystem
- explicit server-side/headless rendering use cases
- suitable for HTML/CSS/JS graphics

Caveat:
- production frame/buffer integration must be validated.

## ADR-011 — FFmpeg/libav

Status: Accepted for media/output integration

Reason:
- mature decoding/encoding
- H.264/AAC
- muxing
- broad protocol support
- RTMP publishing

The core engine does not implement RTMP.

## ADR-012 — No screenshot-per-frame HTML pipeline

Status: Accepted

Do not use a production design that repeatedly saves HTML frames as PNG/JPEG and decodes them again.

Reason:
- excessive CPU
- memory bandwidth
- latency
- disk/IPC overhead

Use an offscreen frame/buffer integration.

## ADR-013 — CPU-first

Status: Accepted

The target VPS must not require a discrete GPU.

Optional hardware acceleration can be added later.

## ADR-014 — StreamKit is not a dependency

Status: Rejected

StreamKit was investigated but is too immature to be the production foundation.

It may be useful for research only.

## ADR-015 — OBS is not the engine

Status: Rejected as the server core

OBS is an excellent reference for scene concepts but is not the target runtime because the product must run headlessly on ordinary VPS infrastructure without a GPU dependency.

## ADR-016 — POC before full implementation

Status: NOT PASSED — superseded by audit findings (see ADR-017)

The POC gate described in `AGENTS.md` and `ROADMAP.md` Phase 1 has **not** been passed. The audit
(`docs/AUDIT_REPORT.md`, 2026-08-16) found that the committed benchmark measures throughput (all
frames rendered back-to-back, no real-time pacing) at 43.93 FPS, and the actual renderer is
WebKitGTK+GTK+Cairo, not WPEPlatform+WPE. The previous claim of "41.66 FPS" with GPAC compositor
filter graphs and WPEBackend-fdo SHM buffer flow was **not supported by the source code**.

Committed evidence (`benchmarks/poc_results.json`): 43.93 FPS throughput, 0 dropped frames,
229% CPU, 259 MB RAM. This is a throughput measurement, not a real-time playout measurement.

The correction plan is defined in `docs/CORRECTION_PLAN.md` (Phases C0–C5).

### What was actually implemented (Cairo/WebKitGTK prototype)

1. `WpeHtmlRenderer` uses `webkit_web_view_new_with_context()` (WebKitGTK API) and
   `gtk_offscreen_window_new()` / `gtk_widget_draw()` (GTK API) for frame capture.
   WPE libraries are linked but never called.
2. `GpacCompositor` uses Cairo API exclusively (`cairo_image_surface_create`, `cairo_create`,
   `cairo_paint`, etc.). GPAC APIs are linked but never called.
3. `HtmlSource::load` captures one cached frame and replays it via `memcpy` — no per-frame
   WPE rendering.
4. Benchmark has no real-time pacing — renders all 150 frames as fast as possible.
5. `std::queue<QueuedFrame>` is unbounded.

The timeline engine, scene controller, JSON schema, FFmpeg encode pipeline, and API server
core logic exist and are tested at unit level, but the rendering and composition path is
not the approved architecture.

## ADR-017 — Architecture audit result

Status: Accepted

Date: 2026-08-16

An independent architecture review and source-code audit confirmed the following deviations
from the approved architecture (`docs/AUDIT_REPORT.md`):

1. `GpacCompositor` is pure Cairo, not GPAC (GPAC linked but never called).
2. `WpeHtmlRenderer` is WebKitGTK+GTK, not WPEPlatform (WPE linked but never called).
3. HTML layer is a cached snapshot, not per-frame WPE rendering.
4. Video sources are not timestamp-aligned to the global clock.
5. No audio in the output pipeline.
6. Benchmark has no real-time pacing (throughput only).
7. Benchmark queue is unbounded.
8. RTMP path never exercised.
9. HLS/SRT are string-prefix checks only.
10. Acceptance docs contain fabricated metrics (31.66 FPS vs committed 25.75/43.93).
11. Production container requires Xvfb (violates no-X11 requirement).
12. /status endpoint missing required metrics.

All findings are confirmed against the source.

## ADR-018 — Correction approach

Status: Accepted

Date: 2026-08-16

The correction plan (`docs/CORRECTION_PLAN.md`) defines phases C0-C5:

- C0: Repository integrity (honest documentation, no code behavior changes)
- C1: Prove WPEPlatform headless -> CPU-readable buffer (no X11)
- C2: Prove GPAC CPU compositor consumes the buffer
- C3: 1080p30 POC benchmark on the real path (the gate)
- C4: Remaining corrections (RTMP, HLS/SRT, timestamps, audio, recovery)
- C5: Documentation and release

Constraint: C0-C3 must pass before any C4+ work. The POC gate must not be lowered.

## ADR-019 — Benchmark gate correction

Status: Accepted

Date: 2026-08-16

The POC benchmark gate requires ALL of:
- >= 30 FPS rendered AND output
- 0 dropped frames
- Real-time pacing (deadline-based, not back-to-back throughput)
- 1920x1080 resolution
- CPU-only (no discrete GPU, no X server, no Xvfb)
- Meaningful soak (minimum 10 minutes)

Throughput measurements (frames/elapsed) are explicitly not accepted as evidence.

## ADR-020 — WPEPlatform headless path + GPAC compositor filter path

Status: Accepted

Date: 2026-08-16

The corrected rendering path is:

```text
WPE WebKit -> WPEDisplayHeadless -> WPEBuffer -> CPU-readable RGBA
  -> GF_FilterSession compositor filter (ogl=off, opfmt=rgba, mode2d=immediate)
  -> FFmpeg H.264/AAC encoder -> RTMP output
```

Key APIs:
- `wpe_display_headless_new()` for headless WPE display
- `wpe_view_new()` / `webkit_web_view_new()` (WPE variant) for HTML rendering
- `GF_FilterSession` / `gf_filter_pid_new` / `gf_filter_pck_send` for GPAC compositor
- `LIBGL_ALWAYS_SOFTWARE=1` + Mesa llvmpipe as fallback if headless EGL needs software GL

The implementation must use CPU-readable buffer path, not GPU DMABUF.
One WPE display per process constraint must be respected.

## ADR-021 — Operator-directed sequencing: stack-independent C4 work before the C1–C3 gate

Status: Accepted (operator decision, 2026-08-17)

Reason: the only local build host is a 4-thread i3-1005G1 with 10 GiB RAM (swap in use).
Ubuntu 26.04 packages neither WPE WebKit nor GPAC, so C1 requires a multi-hour WPE WebKit
source build that this machine cannot reasonably host. Docker is not installed and has been
too heavy for this host in the past. The operator chose (2026-08-17) to proceed with the
stack-independent C4 corrections on the current Cairo/WebKitGTK prototype and to defer the
C1–C3 gate to a capable build host.

Scope executed on the current prototype (all built and tested locally, 13/13 CTest suites):

1. **Bounded queues** (audit A5 / C4.7): new `tarva::BoundedQueue<T>` with hard capacity and
   blocking backpressure; used in `run_poc_benchmark` (composite -> encode, capacity 4);
   unit-tested (`test_bounded_queue`).
2. **/status metrics** (audit A6 / C4.5): new `tarva::RuntimeStats` (atomic counters) updated
   by the playout loop; `/status` now returns playout time, target/rendered FPS, rendered /
   dropped / output frames, output state, active layers, per-source states, CPU % and RAM RSS.
   HTTP-tested via `test_status`. Verified live: engine renders ~30 FPS, 0 dropped, full JSON.
3. **Video pts alignment** (audit #4 / C4.3): `VideoSource::read_frame_rgba` now presents the
   frame at the requested global-clock pts (bounded presentation-order reorder buffer sized
   `has_b_frames + 1`, refcounted AVFrames, keyframe-seek on backward jumps, clock-mapped
   looping at EOF with decoder flush). Tested in `test_sources` (sparse jump, backward jump,
   loop wrap).
4. **Audio in the output pipeline** (audit #5 / C4.6): `MediaOutput` gains an AAC stream
   (S16 interleaved -> FLTP, sample-accurate pts, bounded input fifo); `VideoSource` decodes
   audio (isolated demux context, swr to 48 kHz stereo S16, clock-aligned); the engine loop
   mixes active-layer audio with `AudioMixer` (silence when no audio source). `tarva_playout`
   output verified by ffprobe: h264 video + aac audio. `test_audio` covers decode, mix, mux.

Constraints preserved:

- The POC gate (ADR-019) is NOT lowered; C1–C3 remain the gate and must be proven on the
  real GPAC/WPE path on a capable host.
- All four items above are stack-independent: they carry over to the corrected architecture
  unchanged, and ACCEPTANCE_TESTS.md rows re-marked ✅ were verified against the current
  prototype only; re-verification on the real path is required (C4/C5).
- One incidental fix: `GpacCompositor::init_gpac_filter_session` called
  `gf_fs_new_defaults(0)` with an implicit int conversion that no longer compiles against
  GPAC 26.08 — cast to `GF_FilterSessionFlags` (the GPAC session scaffolding remains
  non-functional per ADR-017 until C2).
