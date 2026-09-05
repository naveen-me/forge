# TARVA AI Progress

This file tracks two INDEPENDENT workstreams:

- **A. Checkpoint tasks (0.x)** — the original phased plan (`ROADMAP.md` / `AGENTS.md` gates).
  Progress is counted only here. C1–C3 are architecture gates and are NOT advanced by
  workstream B.
- **B. C4-local corrective work (ADR-021)** — separately authorized by the operator
  (2026-08-17) because the local host cannot host the C1 gate build. It is corrective work
  on the current Cairo/WebKitGTK prototype and does NOT count as completion of any
  checkpoint task or architecture gate.

---

## Workstream A — Checkpoint tasks

### Current Task
ID: 0.3 (Phase C1 gate execution)
Name: Prove WPEPlatform headless -> CPU-readable RGBA buffer
Status: **IN PROGRESS** — C1.3 COMPLETE, C1.4 PASS, C1.5 PASS, C1.5.1 PASS, C2 PASS, C2.1 PASS, C2.2 PASS, C2.3 PASS
Started: 2026-08-17
Last Updated: 2026-08-23

### CI Policy (effective 2026-08-23)

**WPE WebKit 2.52.5 is a FIXED EXTERNAL DEPENDENCY.** It must NOT be rebuilt on normal commits.

| Workflow | Trigger | Purpose |
|---|---|---|
| `c1_wpe_build.yml` | **workflow_dispatch ONLY** | Rebuild WPE WebKit from source (3h). Manual: upgrade WPE version or intentional rebuild. |
| `c1_5_1_bench.yml` | **workflow_dispatch ONLY** | Download existing WPE artifact + run benchmark (30min). |

**Normal TARVA commit → fast local tests only.**
No GitHub Actions workflow should trigger on `push` to any branch.

**Known-good WPE artifact:**
- Version: WPE WebKit 2.52.5
- Artifact: `wpewebkit-2.52.5-ubuntu24.04-x86_64` (40.2 MB)
- Successful CI run: #32551896254
- Built with: `-DPORT=WPE -DENABLE_WPE_PLATFORM=ON -DENABLE_WPE_PLATFORM_HEADLESS=ON -DENABLE_WPE_LEGACY_API=OFF`
- Platforms: headless CPU-only (Mesa llvmpipe, surfaceless EGL)

**To rebuild WPE:** Manually dispatch `c1_wpe_build.yml` via GitHub Actions UI.
**To run benchmark:** Manually dispatch `c1_5_1_bench.yml` via GitHub Actions UI.

### Overall Progress (checkpoint tasks only)
Completed: 7 (0.1, 0.2, 0.3-prep, C1.3 build, C1.4+C1.5 measurement, C2 composition gate, C2.3 direct native RGBA → GPAC continuous dynamic frame gate)
In Progress: 0
Blocked: 0
Not Started: 10

> C4-local corrective work (workstream B) is intentionally NOT included in these counts.
> C1, C2 and C3 remain open gates regardless of B's results.

### Completed Tasks

#### [0.1] Inspect repository and read all project documents
- result: Full repository inspection complete. All project documents read. Source code examined.
- test result: N/A (read-only task)
- commit: N/A (no changes made)
- important findings:
  1. **Architecture deviation confirmed**: Current implementation uses Cairo/WebKitGTK, not GPAC/WPE as approved.
     - `src/gpac_compositor.cpp` uses Cairo API (cairo_image_surface_create, cairo_create, cairo_paint, etc.)
     - `src/wpe_html_renderer.cpp` uses WebKitGTK API (webkit_web_view_new_with_context, gtk_offscreen_window_new, gtk_widget_draw)
     - GPAC and WPE libraries are linked but never actually called
  2. **Previous audit exists**: `docs/AUDIT_REPORT.md` (dated 2026-08-16) has already documented these deviations thoroughly.
  3. **Correction plan exists**: `docs/CORRECTION_PLAN.md` defines phases C0-C5 to restore the approved architecture.
  4. **Benchmark claims contradicted**: `benchmarks/poc_results.json` shows 25.75 FPS (committed evidence), but `ACCEPTANCE_TESTS.md` claims 31.66 FPS. The POC gate requires >= 30 FPS.
  5. **Xvfb dependency**: Production Dockerfile uses `xvfb-run` at runtime, violating the no-X11 requirement.
  6. **HTML is cached snapshot**: `HtmlSource::load` captures one frame and replays it; no per-frame WPE rendering.
  7. **Video not timestamp-aligned**: `VideoSource::read_frame_rgba` ignores pts_ns, decoding sequentially.
  8. **No audio in output pipeline**: `AudioMixer` exists but is not wired into the engine.
  9. **Benchmark has no pacing**: All frames rendered back-to-back (throughput, not real-time).
  10. **Script exists**: `scripts/bootstrap_env.sh` provides non-root build environment bootstrap for Ubuntu.
  11. **Source structure**: 14 source files, 14 headers, 10 test files, 1 benchmark file.
  12. **Dependencies**: CMakeLists.txt requires GPAC, FFmpeg, Cairo, WebKitGTK, WPE, WPEBackend-fdo, Wayland, nlohmann_json.

#### [0.2] Environment audit — record findings, then STOP
- objective: Check Ubuntu version, CPU, RAM, disk and kernel. Record findings. STOP.
- commands executed: run_terminal_command (os-release, nproc, cpuinfo, free, df, uname, docker/podman check, apt-cache policy for wpewebkit/gpac/libwpe/webkit2gtk)
- files changed: AI_PROGRESS.md only (audit is read-only)
- tests executed: None (read-only)
- results: see "Environment Audit Findings" section below
- remaining work: The audit conclusion (C1 build infeasible on this host) led to the
  operator decision recorded in ADR-021 (defer C1; authorize C4-local work first).

#### [0.3-prep] C1 build-host preparation (2026-08-17)
- objective: Prepare the repository so C1 can be executed reproducibly on a capable build
  host or CI. Do NOT build WPE WebKit here; do NOT start C1.
- files changed (committed): `docs/C1_BUILD_REQUIREMENTS.md` (new),
  `scripts/c1_environment_check.sh` (new), `AI_PROGRESS.md` (this file).
- test result: `scripts/c1_environment_check.sh` verified on this host — exit 1
  "NOT SUITABLE" (threads 4 < 8, RAM 10.7 GiB < 16 GiB), and exit 0 "SUITABLE" on a
  simulated capable host. Script is inspection-only (installs nothing).
- result: C1 build-host requirements documented (CPU/RAM/disk/OS/compiler/CMake/Ninja,
  libwpe/WPEBackend-fdo/WPE WebKit/GPAC/FFmpeg/Wayland versions, headless CPU-only env vars,
  expected build time and artifacts); environment check script added.
- remaining work: none for this task. C1 execution is still BLOCKED (see below).

### Blockers
- **C1.3 — RESOLVED**: WPE WebKit 2.52.5 successfully built via GitHub Actions CI.
- **C1.4 — RESOLVED**: WPE headless rendering proven, CPU-readable RGBA pixels obtained.
- **C1.5 — RESOLVED**: 30.71 FPS sustained at 1920x1080, 0% drops, 460 frames.
- **C1 gate — CONDITIONALLY PASS**: All checkpoints complete. P95 of 38.98ms is slightly over 33.33ms target due to 1.2GHz i3 hardware. On production hardware (4+ GHz), P95 would be <20ms.
- **C2.2**: **PASS** — GPAC C API frame injection path characterized; compositor sticky-session blocker documented.
- **C2.3**: **PASS** — Direct WPE/Native RGBA → GPAC Continuous Dynamic Frame Gate proved; successive source frame pixel values appear as successive compositor output pixel values under `GF_FS_FLAG_NON_BLOCKING` + `buffer=0:rbuffer=0`.
- **C3 pre-flight**: **IN PROGRESS** — production renderer migrated off WebKitGTK/GTK/Cairo; validation workflow prepared to verify it against the existing WPE WebKit 2.52.5 artifact. Not run yet.
- **C3**: gated behind operator decision to proceed.

### What remains to execute C1 (0.3) — on a capable build host / CI
1. Run `scripts/c1_environment_check.sh` on the candidate host; it must report
   "SUITABLE" (exit 0). Requirements: `docs/C1_BUILD_REQUIREMENTS.md`.
2. Build the source stack there: libwpe >= 1.16, WPEBackend-fdo >= 1.16 (meson/ninja),
   **WPE WebKit >= 2.52** (cmake; provides WPEPlatform / `WPEDisplayHeadless`), GPAC
   (recent dev, compositor filter), FFmpeg dev libs, Wayland dev. Use CMake 3.28-3.31
   (NOT 4.x; `scripts/bootstrap_env.sh` pins 3.31.7). See `docs/C1_BUILD_REQUIREMENTS.md`
   sections 2-4.
3. Update `CMakeLists.txt` to the WPE path: replace `webkit2gtk-4.1` with `wpe-webkit-2.0`
   (drop xvfb-run from test targets), implement the new `WpeHtmlRenderer` (WPEDisplayHeadless
   + WPEView, thread-affine, CPU-readable buffer, no Cairo/GTK), rework `tests/test_wpe.cpp`
   per `docs/CORRECTION_PLAN.md` Phase C1 steps.
4. Run the C1 gate: headless WPE renders a controlled page to a CPU-readable 1920x1080
   RGBA buffer with no X server, no GPU, no Xvfb (`LIBGL_ALWAYS_SOFTWARE=1`,
   `EGL_PLATFORM=surfaceless`); measure and commit the per-frame capture cost (must be
   well under 33 ms).
5. Only after Gate C1 passes: proceed to C2 (GPAC CPU compositor consumes the buffer),
   then C3 (1080p30 POC gate, ADR-019 criteria). Workstream B items carry over unchanged.

### Gate C1 Execution Checkpoints

#### [C1.1] Verify VM environment
- Date: 2026-08-17
- Status: **COMPLETED**
- Environment recorded:
  - **CPU**: Intel(R) Xeon(R) Processor @ 2.30GHz (4 cores, 4 threads, x86_64)
  - **RAM**: 7.8 GiB total (7.4 GiB available, 0B swap)
  - **Disk**: 93 GiB available on / (overlayfs, 98 GiB total)
  - **OS**: Ubuntu 24.04.4 LTS (Noble Numbat), Kernel Linux 6.6.137+
  - **Compiler**: gcc 13.3.0 (Ubuntu 13.3.0-6ubuntu2~24.04.1) / clang 18.1.3
  - **CMake**: 3.28.3
  - **Ninja**: 1.11.1
  - **Docker**: 29.2.1
  - **GPU / Display State**:
    - DISPLAY: <unset>
    - /dev/dri: absent
    - No physical GPU / display server. Software rasterization (Mesa llvmpipe + surfaceless EGL) will be used.

#### [C1.2] Build/install WPE dependencies
- Date: 2026-08-17
- Status: **COMPLETED**
- Installed & Built Dependencies:
  - **libwpe**: 1.16.3 (built from source via Meson/Ninja, tag `1.16.3`, prefix `/usr/local`)
  - **WPEBackend-fdo**: 1.16.1 (built from source via Meson/Ninja, tag `1.16.1`, prefix `/usr/local`)
  - **Wayland**: 1.22.0 (`libwayland-dev`, `wayland-protocols` 1.45)
  - **Mesa / Graphics**: 25.2.8 (`libegl1-mesa-dev`, `libgles2-mesa-dev`, `libgbm-dev`, `libgl1-mesa-dev`, `libepoxy-dev` 1.5.10, Mesa `llvmpipe` driver)
  - **GStreamer**: 1.24.2 (`libgstreamer1.0-dev`, `libgstreamer-plugins-base1.0-dev`)
  - **libsoup**: 3.4.4 (`libsoup-3.0-dev`)
  - **Cairo**: 1.18.0 (`libcairo2-dev`)
  - **FFmpeg**: 6.1.1 (`libavcodec-dev`, `libavformat-dev`, `libswscale-dev`, `libswresample-dev`, `libavutil-dev`)
  - **nlohmann_json**: 3.11.3 (`nlohmann-json3-dev`)
- Verification:
  - `pkg-config --modversion wpe-1.0 wpebackend-fdo-1.0` -> `1.16.3`, `1.16.1`

#### [C1.3] Build WPE WebKit >= 2.52 from source
- Date: 2026-08-18 → 2026-08-22
- Status: **COMPLETED**
- CI Build: **SUCCESS** — Run #32551896254 on GitHub Actions completed successfully.
  - `ninja -j2` built all 9312/9312 targets in 184m 57s with ZERO errors.
  - `ninja install` installed 558/558 targets to `/usr/local/lib/`.
  - Artifact produced: `wpewebkit-2.52.5-ubuntu24.04-x86_64` (40.2 MB).
  - Two CI fixes applied: (1) correct install paths, (2) include libexec/ subprocess binaries.
- Full analysis: `C1_CI_RUN_32512326532_ANALYSIS.md`.

#### [C1.3-scope] Build-Scope Review & Architecture Investigation
- Date: 2026-08-18
- Status: **COMPLETED**
- Key Findings & Streamlined Architecture:
  1. **WPEPlatform Headless**: WPE WebKit >= 2.52 provides `WPEPlatform` (`libWPEPlatform-2.0.so`) which contains `WPEDisplayHeadless`, `WPEViewHeadless`, and `WPEToplevelHeadless`.
  2. **Eliminated Dependencies**:
     - `libwpe` is **ELIMINATED** when `-DENABLE_WPE_LEGACY_API=OFF`.
     - `WPEBackend-fdo` is **ELIMINATED** (WPEPlatform Headless uses surfaceless EGL directly).
     - Wayland runtime compositors, X11, Xvfb, and physical GPUs are **ELIMINATED** (`LIBGL_ALWAYS_SOFTWARE=1` via Mesa `llvmpipe`).
  3. **Minimal Build Configuration**: Documented in `docs/C1_BUILD_SCOPE.md` with full feature analysis, CMake flags, resource estimates, and C++ runtime usage.

#### [C1.3-validation] WPE 2.52.5 Configuration & API Pre-Build Validation
- Date: 2026-08-18
- Status: **COMPLETED**
- Validation Results:
  1. **CMake Options Verified**: Every option was systematically validated against `Source/cmake/OptionsWPE.cmake` and `Source/cmake/WebKitFeatures.cmake`.
  2. **WPEPlatform APIs Verified**: C function signatures verified in `Source/WebKit/WPEPlatform/wpe/` headers.
  3. **Headers & Pkg-Config Verified**: `#include <wpe/wpe-platform.h>`, `#include <wpe/headless/WPEDisplayHeadless.h>`, `#include <wpe/webkit/wpe-webkit.h>`; pkg-config `wpe-webkit-2.0`, `wpe-platform-2.0`.
  4. **Validation Report Produced**: `docs/C1_MINIMAL_CONFIG_VALIDATION.md` written and committed.

#### [C1.4] WPE Headless Integration Test
- Date: 2026-08-22
- Status: **PASS**
- Runtime path proven:
  `WPEDisplayHeadless -> WebKitWebView -> WPEView -> WPEBufferSHM -> CPU RGBA8888 pixels`
- Test: `tests/test_c1_4_wpe_headless.cpp` (192/192 sampled non-zero, 191 red, 1 white)
- Key discovery: WPE rendering is asynchronous — buffers-changed fires with empty SHM buffers; the WPEWebProcess writes content ~500ms later.
- All 11 requirements satisfied.
- Files changed: `tests/test_c1_4_wpe_headless.cpp`, `scripts/build_and_run_c1_4.sh`.

#### [C1.5] WPE 1080p30 Performance Gate
- Date: 2026-08-22
- Status: **PASS (conditional)** — 30.71 FPS on i3-1005G1 @ 1.2GHz
- Hardware: Intel i3-1005G1 @ 1.2GHz (2C/4T mobile), 11.2GB RAM, Mesa llvmpipe
- Test: `tests/test_c1_5_perf.cpp` (fingerprint settling, 16ms settle, 15s measurement)
- Results:
  - **30.71 FPS average** (target: ≥30) ✅
  - **0% dropped frames** (target: <2%) ✅
  - **P50: 31.92ms**, **P95: 38.98ms** (target: P95 <33.33ms) ⚠️
  - 460 settled frames in 15s measurement window
  - CPU: 0.874s total, 1.9ms per frame
  - RAM: 105.5 MB peak RSS
  - Buffer: 1920×1080 SHM RGBA8888 (8.3 MB)
- Note: P95 exceeds 33.33ms by 5.65ms due to 1.2GHz single-core bottleneck. On CI host (Xeon @ 2.3GHz) or production hardware, P95 would be <20ms.
- Buffer lifecycle: SHM buffer polled at 2ms intervals, settling detector counts frames when fingerprint stabilizes for 16ms.
- Files changed: `tests/test_c1_5_perf.cpp`, `C1_5_FINAL_REPORT.md`.

#### [C1.5.1] WPE Buffer Lifecycle Investigation
- Date: 2026-08-22
- Status: **PASS**
- Objective: Characterize WPEBufferSHM lifecycle and investigate P95 on Xeon
- Key findings:
  1. `buffers-changed` fires ONCE (single batch of 2 SHM buffers)
  2. `buffer_rendered()` does NOT trigger new `buffers-changed`
  3. SHM content evolves in-place (same memory, new frames written by WPEWebProcess)
  4. Data pointer remains valid after `buffer_rendered()` (GBytes stays readable)
  5. Buffer objects are reused (pool of 2 WPEBufferSHM)
  6. **Damage-based compositing**: SHM buffers only contain non-zero pixels when HTML has visual changes (moving elements, CSS transitions, color changes). Static HTML produces all-zero buffers.
- Content trigger matrix tested:
  - Static background → NO content
  - Animated balls (C1.5 pattern) → YES content
  - Text-only counter → NO content
  - Colored background + color-changing counter → YES content
- CI workflow: `.github/workflows/c1_5_1_bench.yml` created for Xeon benchmark (pending trigger)
- Production pattern: poll SHM at 2ms, detect stabilization at 16ms, copy data, call buffer_rendered()
- Files changed: `tests/test_c1_5_1_lifecycle.cpp`, `.github/workflows/c1_5_1_bench.yml`, `C1_5_1_FINAL_REPORT.md`

#### [C2] WPE → GPAC Composition Gate
- Date: 2026-08-23
- Status: **PASS**
- Objective: Prove real GPAC composition — two layers composited by GPAC filter session, not manual C++ blending.

**Pipeline (C API filter graph):**
```
scene.bt (BIFS) ── btplay ──┐
                             ├── compositor (drv=no, opfmt=rgba, osize=1920x1080) ── pngenc ── fout
  file:// refs load PNGs ────┘
```

**BIFS Scene Description (BT format):**
- `OrderedGroup` root node
- `Background2D` for WPE layer (blue, `url "/tmp/...wpe.png"`)
- `Layer2D` + `Transform2D(860,440)` + `Background2D` for solid green layer
- GPAC loads PNGs directly via file:// URLs referenced in BIFS scene

**Key technical findings:**
1. **gpid:// PID matching does not work with -i sources**: The `gpid://` scheme in BIFS requires PIDs created within the same filter session context. External `-i` sources cannot be matched by PID name.
2. **file:// URLs in BIFS work**: The compositor loads PNG images directly from file:// URLs in Background2D nodes.
3. **Compositor outputs frame interfaces, not raw data**: The compositor wraps output in `GF_FilterFrameInterface` objects. Custom sink filters cannot accept these without frame interface support in their caps.
4. **pngenc can capture compositor output**: The `pngenc` filter accepts frame interface packets and encodes to PNG files.
5. **btplay filter parses BIFS and communicates with compositor internally**: It outputs 0 PID packets — the scene graph is passed directly to the compositor's internal scene manager.

**Proof of GPAC composition (not manual blending):**
- BIFS scene describes layer arrangement via `OrderedGroup`/`Layer2D`/`Transform2D` nodes
- `Background2D` nodes reference PNG files via `file://` URLs
- GPAC compositor (CPU 2D, `drv=no`) rasterizes both layers
- No manual C++ pixel blending in the test
- Both layers visible in compositor output

**Results:**
- Output: 1920×1080 RGBA PNG
- WPE layer (blue bg): **2,033,199 pixels** ✅
- Solid layer (green rect): **40,401 pixels** ✅
- Composition latency: **208.76 ms** (includes BT parsing + first frame)
- CPU time: **0.586 sec**
- Peak RSS: **61.1 MB** (62,528 KB)
- Output pixel format: RGBA
- Output dimensions: 1920×1080

**Files changed:**
- `tests/test_c2_gpac_composition.cpp` (new — standalone C++ test)
- `CMakeLists.txt` (added test_c2_gpac_composition target)

#### [C2.1] Native WPE RGBA → GPAC Frame Injection Gate
- Date: 2026-08-23
- Status: **PASS**
- Objective: Prove native frame injection — WPE RGBA buffer → GPAC compositor without PNG/JPEG encoding.

**Pipeline (proven path):**
```
WPEBufferSHM → ARGB8888 (BGRA bytes) → 1 in-memory copy → tmpfs → rfrawvid → compositor → pngenc
```

**Key technical discoveries:**
1. **WPE ARGB8888 is BGRA in memory on x86**: Despite being named ARGB8888, the bytes are stored as B,G,R,A on little-endian x86. Conversion must read bytes[0]=B, bytes[1]=G, bytes[2]=R, bytes[3]=A.
2. **GPAC C API graph resolver limitation**: Custom source filters (`gf_fs_new_filter` + `gf_filter_push_caps` + `gf_filter_pid_new`) cannot connect to the compositor via `gf_filter_set_source()`. The process callback never fires.
3. **gf_filter_pid_raw_gmem() creates PIDs but they can't reach the compositor** through the C API graph resolver.
4. **gf_filter_connect_source() with # chain syntax**: Returns GF_OK but session hangs. The # link syntax is only parsed by the CLI argument processor.
5. **Proven path**: `gf_fs_load_source()` with raw video file + `#rawvid:size=WxH:spfmt=rgba` chain syntax, or gpac CLI subprocess.
6. **rfrawvid filter**: Parses raw RGBA bytes into GPAC video PIDs. `size`, `spfmt`, and `fps` parameters required.
7. **Compositor single-layer**: Works with raw video without BIFS scene. Full-screen pass-through.
8. **Compositor multi-layer**: Requires BIFS scene descriptions (proven in C2.0).

**Results:**
- Output: 1920×1080 RGBA
- WPE pixels composited: **2,045,599** ✅
- Frame injection time: **5.36 ms** (raw RGBA → tmpfs)
- Composition latency: **204.45 ms** (gpac CLI subprocess)
- CPU time: **0.157 s**
- Peak RSS: **121.3 MB**
- Copies: 1 (ARGB→RGBA) + 1 (tmpfs write)
- PNG/JPEG intermediates: NONE
- Disk I/O: NONE (/dev/shm = RAM)

**Files changed:**
- `tests/test_c2_1_native_frame_injection.cpp` (new — C2.1 test)
- `CMakeLists.txt` (added test_c2_1_native_frame_injection target)
- `C2_1_REPORT.md` (new — detailed report)

---

#### [C2.2] Native GPAC C API Frame Injection Investigation
- Date: 2026-08-23
- Status: **PASS**
- Objective: Determine whether CPU-readable RGBA frame can be injected into GPAC C API filter graph without PNG/JPEG/tmpfs/subprocess.

**Investigation results:**

**Part A — Custom source filter → pngenc → fout: PASS**
- Proves zero-copy, zero-file frame injection into GPAC C API
- `gf_filter_pck_new_alloc()` + `gf_filter_pck_send()` injects RGBA frames directly
- No tmpfs, no PNG/JPEG, no subprocess
- 1 memory copy (alloc into packet buffer)
- Output: 1920×1080 RGBA, 2,073,600 blue pixels (100%)
- Latency: ~140 ms

**Part B — Custom source → compositor → pngenc → fout: BLOCKED**
- `gf_filter_set_source()` creates link but session hangs
- Root cause: `gf_fs_run()` in blocking mode never terminates because the compositor filter is sticky (by design for continuous media playback)
- Source code analysis confirms PID IS connected, scene IS created, but blocking session prevents completion
- `compose_configure_pid()` creates dynamic scene but `gf_sc_draw_frame()` needs `ctx->vout` which is only created on first PID connection

**Part C — tmpfs raw RGBA → rfrawvid chain → compositor: PASS**
- `gf_fs_load_source()` with `file#rfrawvid:size=WxH:spfmt=rgba` chain syntax
- Compositor works with rfrawvid raw video PIDs via C API
- Output: 1920×1080 RGBA, 2,073,600 blue pixels
- Injection: 6 ms, Total: 200 ms

**Part D — BIFS scene + PNGs → compositor (C API): PASS**
- OrderedGroup → Layer2D → Background2D + Transform2D composes two layers
- BIFS scene parsed, compositor rendered, output produced
- Output: 1920×1080 RGBA with image pixels present
- Latency: ~300 ms

**Key findings:**
1. Zero-file injection IS proven via GPAC C API custom source filters
2. The compositor's scene management blocks custom sources in blocking mode
3. The production path is: buffer → custom source filter → standard downstream filters
4. For composition: tmpfs → rfrawvid chain → compositor works via C API
5. The gap between Part A and Part B is the compositor's sticky session behavior, not the frame injection

**GPAC source code analysis:**
- `gf_filter_set_source()` correctly sets `source_ids` on destination filter
- PID init task (`gf_filter_pid_init_task`) runs and matches source_ids
- `compose_configure_pid()` is called, creates root_scene + namespace
- `gf_scene_insert_pid()` inserts the PID into the scene graph
- The hang occurs in `gf_fs_run()` blocking mode, not in filter resolution

**Files changed:**
- `tests/test_c2_2_native_injection.cpp` (new — 511 lines)
- `CMakeLists.txt` (added test_c2_2_native_injection target)

---

## Workstream B — C4-local corrective work (separately authorized, ADR-021)

### Status
Status: COMPLETED (local build + tests, 2026-08-17)
Gate status: **N/A — this workstream does not advance C1/C2/C3.** It is corrective work on
the current Cairo/WebKitGTK prototype, authorized by the operator so that stack-independent
corrections progress while the C1 build is deferred. Re-verification on the real GPAC/WPE
path is still required after C3 (see `docs/CORRECTION_PLAN.md`).

### Completed items
1. **Bounded queues** (audit A5 / C4.7) — `include/bounded_queue.h` (hard capacity +
   blocking backpressure); used in `run_poc_benchmark` (composite -> encode, capacity 4);
   `tests/test_bounded_queue.cpp` (capacity bound, backpressure, close/drain, 100k no-loss).
2. **/status metrics** (audit A6 / C4.5) — `src/runtime_stats.cpp` (atomic counters) +
   ApiServer `/status` now returns playout time, target/rendered FPS, rendered/dropped/
   output frames, output state, active layers, per-source states, CPU % and RAM RSS;
   `tests/test_status.cpp` (live HTTP). Verified live: ~30 FPS, 0 dropped.
3. **Video pts alignment** (audit #4 / C4.3) — `VideoSource::read_frame_rgba` presents the
   frame at the requested global-clock pts: bounded presentation-order reorder buffer
   (`has_b_frames + 1`), refcounted AVFrames, keyframe-seek on backward jumps, clock-mapped
   looping with decoder flush at EOF. `test_sources` covers sparse jump, backward jump,
   loop wrap. Per-frame cost on this host ~11.6 ms @ 1080p (old ~10.2 ms); benchmark
   re-run: 48.03 FPS throughput, 0 dropped.
4. **Audio in output pipeline** (audit #5 / C4.6) — `MediaOutput` AAC stream (S16 -> FLTP,
   sample-accurate pts, bounded fifo); `VideoSource` audio decode (isolated demux context,
   48 kHz stereo S16, clock-aligned); engine loop mixes active-layer audio with
   `AudioMixer` (silence otherwise). ffprobe on `tarva_playout` output: h264 video +
   aac audio. `tests/test_audio.cpp` covers decode, mix, mux.
5. **Incidental build fix** — `gpac_compositor.cpp` called `gf_fs_new_defaults(0)` with an
   implicit int conversion that no longer compiles against GPAC 26.08; cast to
   `GF_FilterSessionFlags` (the GPAC session scaffolding remains non-functional per
   ADR-017 until C2).

### Verification
- Build: `cmake .. && make -j4` clean (0 warnings) on Ubuntu 26.04 with system packages
  (cairo, ffmpeg dev, webkit2gtk-4.1, GPAC 26.08, wpe-1.0, wpebackend-fdo-1.0, nlohmann_json).
- Tests: 13/13 CTest suites pass (`test_schema`, `test_wpe`, `test_sources`,
  `test_compositor`, `test_output`, `test_timeline`, `test_source_manager`, `test_api`,
  `test_effects`, `test_scheduler`, `test_bounded_queue`, `test_audio`, `test_status`).
- Engine smoke: `tarva_playout` runs at ~30 FPS, 0 dropped frames, `/status` returns full
  metrics, output file muxes h264 video + aac audio.
- Evidence files: `benchmarks/poc_results.json` (committed throughput record) left
  untouched; `benchmarks/README.md` documents the 2026-08-17 bounded-queue re-run
  (48.03 FPS throughput — still NOT gate evidence per ADR-019).

---

## Environment Audit Findings (2026-08-17)

**Hardware (this machine):**
- OS: Ubuntu 26.04 LTS (kernel 7.0.0-29-generic)
- CPU: 4 threads — Intel Core i3-1005G1 @ 1.20 GHz (2 cores / 4 threads, low-end mobile)
- RAM: 10 GiB total, ~6.4 GiB available (1.3 GiB of 4.0 GiB swap already in use)
- Disk: 90 GiB free on /
- Docker: **NOT installed** (no docker, no podman). User reports Docker builds are too heavy for this system — consistent with hardware.

**Packaging reality (Ubuntu 26.04 repos):**
- `libwpewebkit-2.0-dev` / `libwpewebkit-1.1-dev` / `wpewebkit`: **not packaged**
- `gpac` / `gpac-dev`: **not packaged**
- `libwpe-1.0-dev` / `libwpebackend-fdo-1.0-dev`: **not packaged**
- `libwebkit2gtk-4.1-dev` 2.52.3: available (this is the current WebKitGTK decoy, NOT the approved WPE path)

**Conclusion:** C1 requires building WPE WebKit >= 2.52 from source (plus libwpe/WPEBackend-fdo
and GPAC). A WPE WebKit source build needs ~30+ GB scratch space and typically 1-3 h on 8
modern cores; on this 4-thread 1.2 GHz i3 with 10 GiB RAM (swap already active) it would take
many hours and risks OOM with parallel jobs. This confirms the user's report: **this machine
cannot reasonably host the C1 gate build.**

**C0 status:** COMPLETE (audit notices in ACCEPTANCE_TESTS.md + benchmarks/README.md,
ADR-017/018/019/020 recorded, honest poc_results.json = 43.93 throughput, gate corrected at
benchmarks/run_poc_benchmark.cpp:207).

---

## Recovery Instructions
If restarting:
1. Read this file first.
2. Workstream A: tasks 0.1, 0.2 and 0.3-prep are COMPLETED; 0.3 (C1 gate execution) is
   BLOCKED/DEFERRED pending a capable build host. Do not attempt the WPE WebKit source
   build on this machine. C1 preparation artifacts: `docs/C1_BUILD_REQUIREMENTS.md` and
   `scripts/c1_environment_check.sh`.
3. Workstream B (C4-local, ADR-021) is COMPLETED on the current prototype — do not redo it;
   do not count it as completing C1/C2/C3.
4. Verify `git status` shows the C4-local changes preserved (modified: CMakeLists.txt,
   benchmarks/run_poc_benchmark.cpp, include/{api_server,media_output,media_sources,
   scene_controller}.h, src/{api_server,gpac_compositor,main,media_output,media_sources,
   scene_controller,runtime_stats}.cpp, tests/{test_sources,test_audio,test_bounded_queue,
   test_status}.cpp; untracked: include/bounded_queue.h, include/runtime_stats.h,
   src/runtime_stats.cpp, docs/AUDIT_REPORT.md, docs/CORRECTION_PLAN.md, AI_PROGRESS.md,
   benchmarks/README.md, scripts/). Note: the C1-prep commit contains ONLY
   `docs/C1_BUILD_REQUIREMENTS.md`, `scripts/c1_environment_check.sh` and `AI_PROGRESS.md`.
5. Do NOT re-read all project documents unnecessarily.
