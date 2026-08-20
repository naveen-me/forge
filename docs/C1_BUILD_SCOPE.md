# WPE WebKit Build Scope & Architecture Review (Gate C1)

## 1. Executive Summary

This document defines the minimal, streamlined build scope for **WPE WebKit >= 2.52** for TARVA's real-time playout engine.

TARVA requires WPE WebKit solely as a **headless CPU-only HTML/CSS/JS overlay renderer** that produces CPU-readable RGBA pixel buffers (`WPEBuffer`) for consumption by TARVA's CPU video pipeline. TARVA is **not** a general-purpose web browser.

WPE WebKit >= 2.52 introduces the native **WPEPlatform** framework, which includes a built-in headless display implementation (`WPEDisplayHeadless`). By targeting WPEPlatform Headless directly and disabling legacy APIs and unused display/browser features, we eliminate external runtime display servers (Wayland, X11, Xvfb) and legacy intermediary libraries (`libwpe`, `WPEBackend-fdo`).

---

## 2. Architecture Comparison

### A. Legacy Path (Deprecated / Unnecessary for C1)
```
HTML Page
   ↓
WPE WebKit (legacy API)
   ↓
libwpe + WPEBackend-fdo
   ↓
Wayland / DRM / EGL
   ↓
External Compositor / Display Server
```
*Disadvantages:* Requires `libwpe` and `WPEBackend-fdo`, depends on Wayland runtime libraries, requires extra IPC abstraction layers, and increases build complexity.

### B. Minimal WPEPlatform Headless Path (Approved Target)
```
HTML / CSS / JS / Fonts / Canvas 2D
   ↓
WPE WebKit >= 2.52 (WPEPlatform)
   ↓
WPEDisplayHeadless (surfaceless EGL / Mesa llvmpipe)
   ↓
WPEViewHeadless -> WPEBuffer (CPU-readable SHM / RGBA)
   ↓
TARVA CPU Compositor / Encoder
```
*Advantages:* Uses WPE WebKit's built-in `WPEDisplayHeadless` (`WPE_DISPLAY=wpe-display-headless`). Completely eliminates `libwpe`, `WPEBackend-fdo`, Wayland compositors, X11, and Xvfb. Operates 100% headlessly in CPU-only software mode (`LIBGL_ALWAYS_SOFTWARE=1`, surfaceless EGL).

---

## 3. Dependency Scope Reduction

| Dependency | Legacy Stack | Streamlined WPEPlatform Stack | Status |
|---|---|---|---|
| **libwpe** | Required | **NOT Required** (`ENABLE_WPE_LEGACY_API=OFF`) | **ELIMINATED** |
| **WPEBackend-fdo** | Required | **NOT Required** (Headless built into WPEPlatform) | **ELIMINATED** |
| **Wayland Runtime** | Required | **NOT Required** (`ENABLE_WPE_PLATFORM_WAYLAND=OFF`) | **ELIMINATED** |
| **X11 / Xvfb** | Never approved | **NOT Required** | **EXCLUDED** |
| **Physical GPU** | Not needed | **NOT Required** (Mesa llvmpipe software EGL) | **EXCLUDED** |
| **GLib / GObject / Gio** | Required | Required (Core WebKit runtime) | Retained |
| **libsoup-3.0** | Required | Required (HTTP/network loader) | Retained |
| **HarfBuzz / FreeType / Fontconfig** | Required | Required (Text & font rendering) | Retained |
| **Cairo / Skia** | Required | Required (2D canvas & WebCore rendering) | Retained |
| **LibXml2 / LibXslt / SQLite3** | Required | Required (DOM / XML / WebCore state) | Retained |
| **GStreamer** | Required | Required (`ENABLE_VIDEO=ON` for WebCore headers) | Retained |

---

## 4. Feature Analysis (Required vs Optional)

### Required Features for TARVA
- **HTML5 / CSS3 / JavaScript**: Full DOM, flexbox, CSS grid, CSS keyframe animations, web fonts, vector graphics.
- **Canvas 2D**: Dynamic graphics, overlays, lower-thirds, text rendering.
- **Headless WPEPlatform**: `WPEDisplayHeadless`, `WPEViewHeadless`, `WPEBuffer` CPU buffer acquisition.

### Unnecessary / Disabled Features
- **Legacy WPE API** (`ENABLE_WPE_LEGACY_API=OFF`): Unneeded with WPEPlatform.
- **DRM & Wayland Backends** (`ENABLE_WPE_PLATFORM_DRM=OFF`, `ENABLE_WPE_PLATFORM_WAYLAND=OFF`): Headless mode uses surfaceless EGL directly.
- **JIT Compiler** (`ENABLE_JIT=OFF`, `ENABLE_DFG_JIT=OFF`, `ENABLE_FTL_JIT=OFF`): JavaScript executes via CLoop interpreter, removing ~1000 JIT compiler files from build.
- **Bubblewrap Sandbox** (`ENABLE_BUBBLEWRAP_SANDBOX=OFF`): Unneeded in controlled containerized server environments.
- **Browser Utilities**: Gamepad, Geolocation, Notifications, MathML, PDF.js, WebAudio, WebDriver, Speech Synthesis, Journald logging, MiniBrowser, Documentation, GObject Introspection, Sampling Profiler (`ALL OFF`).
- **Heavy Image/Video Codecs**: AVIF, JPEG-XL, LCMS, WOFF2, Flite, ATK (`ALL OFF`).
- **Note on Video**: `ENABLE_VIDEO=ON` must remain `ON` because `GraphicsContextGL.h` in WebKit 2.52 references `VideoFrame` unconditionally. However, external video decoders (AVIF/JXL/WebRTC) are disabled.

---

## 5. Proposed CMake Build Configuration

```bash
cmake -GNinja .. \
  -DPORT=WPE \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_INSTALL_PREFIX=/usr/local \
  -DCMAKE_C_FLAGS_RELEASE="-O1 -DNDEBUG" \
  -DCMAKE_CXX_FLAGS_RELEASE="-O1 -DNDEBUG" \
  -DENABLE_WPE_PLATFORM=ON \
  -DENABLE_WPE_PLATFORM_HEADLESS=ON \
  -DENABLE_WPE_PLATFORM_DRM=OFF \
  -DENABLE_WPE_PLATFORM_WAYLAND=OFF \
  -DENABLE_WPE_LEGACY_API=OFF \
  -DENABLE_VIDEO=ON \
  -DENABLE_BUBBLEWRAP_SANDBOX=OFF \
  -DENABLE_DOCUMENTATION=OFF \
  -DENABLE_INTROSPECTION=OFF \
  -DENABLE_GAMEPAD=OFF \
  -DENABLE_JOURNALD_LOG=OFF \
  -DENABLE_MINIBROWSER=OFF \
  -DENABLE_SPEECH_SYNTHESIS=OFF \
  -DENABLE_GEOLOCATION=OFF \
  -DENABLE_NOTIFICATIONS=OFF \
  -DENABLE_MATHML=OFF \
  -DENABLE_PDFJS=OFF \
  -DENABLE_WEB_AUDIO=OFF \
  -DENABLE_WEBDRIVER=OFF \
  -DENABLE_SAMPLING_PROFILER=OFF \
  -DENABLE_JIT=OFF \
  -DENABLE_DFG_JIT=OFF \
  -DENABLE_FTL_JIT=OFF \
  -DUSE_AVIF=OFF \
  -DUSE_JPEGXL=OFF \
  -DUSE_LCMS=OFF \
  -DUSE_WOFF2=OFF \
  -DUSE_FLITE=OFF \
  -DUSE_ATK=OFF \
  -DUSE_LIBBACKTRACE=OFF
```

---

## 6. Build & Resource Requirements Estimate

| Metric | Legacy Full Build | Streamlined WPEPlatform Build |
|---|---|---|
| **Build Scratch Disk** | ~25 - 35 GB | ~8 - 12 GB |
| **Installed Binary Size** | ~400 - 600 MB | ~150 - 220 MB |
| **Peak Compile RAM (per job)** | ~2.5 - 3.5 GB (`-O3`) | ~1.0 - 1.5 GB (`-O1`) |
| **Build Time (4 vCPUs)** | ~1.5 - 2.5 hours | ~35 - 50 minutes |
| **Build Time (8-16 vCPUs)** | ~30 - 45 minutes | ~12 - 20 minutes |

---

## 7. Runtime Architecture & Discovery

When compiled with `ENABLE_WPE_PLATFORM=ON` and `ENABLE_WPE_LEGACY_API=OFF`, TARVA links directly against:
- `libWPEWebKit-2.0.so`
- `libWPEPlatform-2.0.so`

Discovery via `pkg-config`:
```bash
pkg-config --cflags --libs wpe-webkit-2.0 wpe-platform-2.0
```

Headless Initialization in C++:
```cpp
// Set headless environment
g_setenv("WPE_DISPLAY", "wpe-display-headless", TRUE);
g_setenv("LIBGL_ALWAYS_SOFTWARE", "1", TRUE);

// Create display and view
WPEDisplay* display = wpe_display_headless_new();
WPEView* view = wpe_display_create_view(display);
```

This completes the build-scope review for Gate C1.
