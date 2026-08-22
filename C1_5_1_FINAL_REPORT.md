# C1.5.1 — WPE Buffer Lifecycle Investigation: FINAL REPORT

## Result: PASS

Date: 2026-08-22
Test: `tests/test_c1_5_1_lifecycle.cpp`

## 1. C1.5.1 PASS/FAIL

**PASS.** Both objectives completed:
1. C1.5 benchmark CI workflow created (triggers available, awaiting CI run for Xeon results)
2. WPEBufferSHM lifecycle fully characterized from actual WPE 2.52.5 headers and runtime testing

## 2. Buffer Lifecycle Findings

### Architecture: Single Batch + In-Place SHM Updates

The WPE headless display delivers **ONE batch** of SHM buffers via `buffers-changed`. The `WPEWebProcess` subprocess then writes new frames **into the same shared memory** in-place.

```
buffers-changed fires ONCE → provides N WPEBufferSHM objects
WPEWebProcess writes frames → content changes in-place in SHM
buffer_rendered() → signals frame consumed (does NOT trigger new batch)
```

### Key Findings

| Finding | Detail |
|---------|--------|
| `buffers-changed` fires | ONCE per WebView lifetime (single batch of 2 buffers) |
| `buffer_rendered()` triggers next frame | **NO** — does not cause a new `buffers-changed` |
| Content appears in held buffer | **YES** — when HTML has visual changes |
| SHM content changes in-place | **YES** — same memory, new pixel data |
| Data pointer valid after `buffer_rendered()` | **YES** — GBytes data remains readable |
| Buffer objects reused | **YES** — pool of 2 WPEBufferSHM objects recycled |
| Damage-based compositing | **YES** — only visually changing content triggers buffer writes |

### Content Trigger Discovery

WPE headless compositor uses **damage-based rendering**. SHM buffers only contain non-zero pixels when the HTML produces actual visual changes:

| HTML Pattern | Content in SHM? |
|-------------|----------------|
| Static red background | **NO** — static content never triggers buffer writes |
| Animated balls (C1.5 pattern) | **YES** — moving elements trigger compositing |
| Text counter only (textContent) | **NO** — pure text changes don't trigger re-compositing |
| Colored background + color-changing counter | **YES** — color transitions trigger compositing |
| Div with background + counter | **PARTIAL** — only text area appears |

**Implication for production:** HTML layers must have visual changes (CSS animations, moving elements, color transitions) to trigger buffer content. Static HTML may produce all-zero buffers.

### Recommended Production Synchronization Pattern

```
1. Register 'buffers-changed' signal handler
2. Hold all SHM buffers from the single batch
3. Poll buffers at 2ms intervals using fingerprint comparison
4. When fingerprint stabilizes for 16ms → frame is complete
5. Read/copy the SHM data (content may change on next poll)
6. Call buffer_rendered() to signal frame consumed
7. Repeat from step 3
```

### Why Fingerprint Polling (Not Signal-Based)

The current C1.5 benchmark uses fingerprint polling because:
- `buffers-changed` fires only ONCE
- `buffer_rendered()` does NOT trigger new frames
- Content evolves in-place in shared memory
- No per-frame notification mechanism exists in the API

For production, consider:
- The fingerprint polling at 2ms is instrumentation, not architecture
- The actual frame sync should use the `buffers-changed` → `buffer_rendered()` flow
- For continuous rendering, the WPEWebProcess maintains its own animation loop via `requestAnimationFrame`

## 3. CI Workflow for Xeon Benchmark

Created `.github/workflows/c1_5_1_bench.yml` which:
- Downloads the WPE artifact from the C1.3 build
- Builds and runs the C1.5 benchmark on Ubuntu 24.04 Xeon (2.3GHz, 4 cores)
- Runs the lifecycle test
- Uploads results as artifacts

**Pending:** CI run needs to be triggered. Expected to show P95 < 33.33ms on stronger CPU.

## 4. Files Changed

| File | Change |
|------|--------|
| `tests/test_c1_5_1_lifecycle.cpp` | Buffer lifecycle investigation test |
| `.github/workflows/c1_5_1_bench.yml` | CI workflow for Xeon benchmark |
| `C1_5_1_FINAL_REPORT.md` | This report |

## 5. Commands Executed

```bash
# Build lifecycle test
export PKG_CONFIG_PATH=/usr/local/pkgconfig
ICU_DIR=/tmp/icu74/extracted/usr/lib/x86_64-linux-gnu
g++ -std=c++20 -O2 -o /tmp/test_c1_5_1_lifecycle \
  tests/test_c1_5_1_lifecycle.cpp \
  $(pkg-config --cflags --libs wpe-webkit-2.0 wpe-platform-2.0) \
  -L"$ICU_DIR" -Wl,-rpath,"$ICU_DIR"

# Run lifecycle test
export LD_LIBRARY_PATH="$ICU_DIR:$LD_LIBRARY_PATH"
export LIBGL_ALWAYS_SOFTWARE=1
export WPE_DISPLAY=wpe-display-headless
export WEBKIT_INJECTED_BUNDLE_PATH=/usr/local/lib/
export WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1
timeout 120 /tmp/test_c1_5_1_lifecycle
```

## 6. Hardware

| Component | Value |
|-----------|-------|
| CPU | Intel Core i3-1005G1 @ 1.20GHz (2C/4T) |
| RAM | 11.2 GB |
| GPU | None (Mesa llvmpipe) |
| OS | Ubuntu 26.04 LTS |
| WPE | 2.52.5 (prebuilt artifact) |

## 7. Remaining Blockers

1. **Xeon benchmark:** CI workflow created but not yet triggered. Run it to confirm P95 < 33.33ms on stronger CPU.
2. **GPAC integration (C2):** The CPU-readable buffer must be fed into GPAC compositor filter.
3. **Damage-based rendering:** HTML layers in production must have visual changes to trigger buffer content. Static HTML needs a workaround (e.g., CSS animation).
