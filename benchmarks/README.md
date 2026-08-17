# Benchmarks

> **AUDIT NOTICE (2026-08-17):** The existing benchmark results in this directory are
> **throughput measurements**, not real-time playout measurements. They were produced
> by rendering all frames back-to-back as fast as the CPU allows, with no deadline-based
> pacing. The "FPS" numbers represent CPU throughput (frames per wall-clock second of
> rendering), not real-time output at 30fps.

## Current Results

### `poc_results.json` — Throughput Benchmark (NOT real-time)

- **Canvas:** 1920x1080 @ 30fps target
- **Total frames:** 150 (5 seconds of content)
- **Elapsed:** 3.41 seconds (rendered 4.4x faster than real-time)
- **Throughput FPS:** 43.93 (frames/elapsed)
- **Dropped frames:** 0
- **CPU:** 229% (multi-threaded)
- **RAM:** 259 MB

### What This Means

This benchmark proves the pipeline CAN render faster than 30fps in throughput mode.
It does NOT prove the pipeline can sustain 30fps in real-time with:
- Deadline-based frame scheduling
- Proper late-frame accounting
- Bounded queues with backpressure
- Real-time pacing enforcement

The POC gate (Phase C3 of `docs/CORRECTION_PLAN.md`) requires a real-time paced
benchmark, not a throughput measurement.

### Corrected POC Gate Requirements (ADR-019)

- >= 30 FPS rendered AND output
- 0 dropped frames
- Real-time pacing (deadline-based, not back-to-back)
- 1920x1080 resolution
- CPU-only (no GPU, no X server, no Xvfb)
- Minimum 10-minute soak
- Metrics: FPS, avg/p95 frame time, dropped frames, CPU%, RAM, per-component costs

### 2026-08-17 Re-run (bounded queue, current prototype)

`run_poc_benchmark` now uses `tarva::BoundedQueue` (capacity 4) between composite and encode
with real backpressure (audit A5). Re-run on the local host: **48.03 FPS throughput, 0 dropped
frames, 204% CPU, 253 MB RAM** — still a throughput measurement (no deadline pacing), so it is
NOT gate evidence (see ADR-019). The committed `poc_results.json` remains the honest,
previously-committed throughput record.

### Architecture Status

The current implementation uses Cairo/WebKitGTK, not the approved GPAC/WPE architecture.
See `docs/AUDIT_REPORT.md` and `docs/CORRECTION_PLAN.md` for details.
