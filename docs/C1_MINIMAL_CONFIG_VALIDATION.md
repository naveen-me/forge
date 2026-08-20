# WPE WebKit 2.52.5 Minimal Build Configuration & API Validation (Gate C1)

## 1. Verified CMake Options

Every option in this table has been directly verified against the WPE WebKit 2.52.5 source tree (`Source/cmake/OptionsWPE.cmake` and `Source/cmake/WebKitFeatures.cmake`).

| CMake Option | Scope/Type | Default Value | Verified Support in 2.52.5 | Unnecessary Dependency Eliminated |
|---|---|---|---|---|
| `ENABLE_WPE_PLATFORM` | PUBLIC | `${ENABLE_DEVELOPER_MODE}` (OFF) | **VERIFIED** (`OptionsWPE.cmake:111`) | Required: explicitly set `ON` to enable WPEPlatform (`libWPEPlatform-2.0.so`). |
| `ENABLE_WPE_PLATFORM_HEADLESS` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:113`) | Provides `WPEDisplayHeadless` / `WPEViewHeadless`. |
| `ENABLE_WPE_PLATFORM_DRM` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:112`) | Setting `OFF` eliminates DRM/KMS device dependencies (`libdrm`, `udev`). |
| `ENABLE_WPE_PLATFORM_WAYLAND` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:114`) | Setting `OFF` eliminates Wayland display server dependencies (`libwayland-client/server`). |
| `ENABLE_WPE_LEGACY_API` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:115`) | Setting `OFF` eliminates legacy `libwpe` API and `WPEBackend-fdo` dependencies! |
| `ENABLE_VIDEO` | PRIVATE | `ON` | **VERIFIED** (`WebKitFeatures.cmake:228`) | **MUST REMAIN ON**: Setting `OFF` breaks compilation in `GraphicsContextGL.h:1675` (`VideoFrame` reference). |
| `ENABLE_BUBBLEWRAP_SANDBOX` | PUBLIC | `ON` (in WPE port) | **VERIFIED** (`OptionsWPE.cmake:144`) | Setting `OFF` eliminates `bubblewrap` executable runtime dependency. |
| `ENABLE_DOCUMENTATION` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:108`) | Setting `OFF` eliminates documentation build tools (`gtk-doc`/gi-docgen). |
| `ENABLE_INTROSPECTION` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:109`) | Setting `OFF` eliminates `g-ir-scanner` (GObject Introspection) build dependency. |
| `ENABLE_GAMEPAD` | PRIVATE | `ON` (in WPE port) | **VERIFIED** (`OptionsWPE.cmake:74`) | Setting `OFF` eliminates `libmanette` dependency. |
| `ENABLE_JOURNALD_LOG` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:110`) | Setting `OFF` eliminates `systemd-journald` logging dependency. |
| `ENABLE_MINIBROWSER` | PUBLIC | `ON` (in WPE port) | **VERIFIED** (`OptionsWPE.cmake:156`) | Setting `OFF` eliminates MiniBrowser binary compilation. |
| `ENABLE_SPEECH_SYNTHESIS` | PUBLIC | `ON` (in WPE port) | **VERIFIED** (`OptionsWPE.cmake:57`) | Setting `OFF` eliminates Flite (`USE_FLITE`) and Spiel speech synthesis dependencies. |
| `ENABLE_GEOLOCATION` | PRIVATE | `ON` | **VERIFIED** (`WebKitFeatures.cmake:169`) | Setting `OFF` eliminates Geolocation API dependencies. |
| `ENABLE_NOTIFICATIONS` | PRIVATE | `ON` | **VERIFIED** (`OptionsWPE.cmake:83`) | Setting `OFF` eliminates desktop notification dependencies. |
| `ENABLE_MATHML` | PRIVATE | `ON` | **VERIFIED** (`WebKitFeatures.cmake:184`) | Setting `OFF` eliminates MathML layout engine code. |
| `ENABLE_PDFJS` | PUBLIC | `ON` (in WPE port) | **VERIFIED** (`OptionsWPE.cmake:54`) | Setting `OFF` eliminates PDF.js viewer embedding. |
| `ENABLE_WEB_AUDIO` | PUBLIC | `ON` | **VERIFIED** (`GStreamerDefinitions.cmake:2`) | Setting `OFF` eliminates Web Audio processing pipeline inside WebKit. |
| `ENABLE_WEBDRIVER` | PUBLIC | `ON` (in WPE port) | **VERIFIED** (`OptionsWPE.cmake:55`) | Setting `OFF` eliminates WebDriver service binary (`WPEWebDriver`). |
| `ENABLE_SAMPLING_PROFILER` | PRIVATE | `ON` | **VERIFIED** (`WebKitFeatures.cmake:213`) | Setting `OFF` eliminates JSC sampling profiler code. |
| `ENABLE_JIT` | PRIVATE | `ON` | **VERIFIED** (`WebKitFeatures.cmake:178`) | Setting `OFF` disables JavaScript JIT compiler tiers and removes ~1000 JIT files. |
| `ENABLE_DFG_JIT` | PRIVATE | `ON` | **VERIFIED** (`WebKitFeatures.cmake:161`) | Setting `OFF` disables Data Flow Graph JIT. |
| `ENABLE_FTL_JIT` | PRIVATE | `ON` | **VERIFIED** (`WebKitFeatures.cmake:165`) | Setting `OFF` disables Faster Than Light JIT. |
| `USE_AVIF` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:59`) | Setting `OFF` eliminates `libavif` dependency. |
| `USE_JPEGXL` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:61`) | Setting `OFF` eliminates `libjxl` dependency. |
| `USE_LCMS` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:60`) | Setting `OFF` eliminates `liblcms2` dependency. |
| `USE_WOFF2` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:62`) | Setting `OFF` eliminates `libwoff2dec` dependency. |
| `USE_FLITE` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:119`) | Setting `OFF` eliminates Flite dependency. |
| `USE_ATK` | PUBLIC | `ON` | **VERIFIED** (`OptionsWPE.cmake:118`) | Setting `OFF` eliminates ATK accessibility dependency. |
| `USE_LIBBACKTRACE` | PUBLIC | `ON` (in WPE port) | **VERIFIED** (`OptionsWPE.cmake:121`) | Setting `OFF` eliminates `libbacktrace` dependency. |

---

## 2. Invalid / Unsupported Options in WPE WebKit 2.52.5

- **`ENABLE_VIDEO=OFF`**: **INVALID / BROKEN**. In WPE WebKit 2.52.5, configuring `-DENABLE_VIDEO=OFF` breaks compilation in `GraphicsContextGL.h:1675` because `VideoFrame` is referenced without an `#if ENABLE(VIDEO)` preprocessor guard. `ENABLE_VIDEO=ON` **must** be set to `ON`.
- **`ENABLE_C_LOOP=ON`**: **UNNECESSARY**. Disabling JIT (`-DENABLE_JIT=OFF`) automatically enables CLoop interpreter in JavaScriptCore.

---

## 3. Verified WPEPlatform C APIs

All function signatures below were verified directly from WPE WebKit 2.52.5 source headers (`Source/WebKit/WPEPlatform/wpe/`):

### Display & View Management (`WPEDisplay.h`, `WPEView.h`, `headless/WPEDisplayHeadless.h`)
- **`WPEDisplay *wpe_display_headless_new (void);`** — Creates a new headless WPE display.
- **`gboolean wpe_display_connect (WPEDisplay *display, GError **error);`** — Connects the display.
- **`WPEView *wpe_view_new (WPEDisplay *display);`** — Creates a new view associated with the display.
- **`void wpe_view_resized (WPEView *view, int width, int height);`** — Notifies the view of size changes.
- **`void wpe_view_map (WPEView *view);`** — Maps the view for rendering.

### Buffer Acquisition (`WPEBuffer.h`, `WPEBufferSHM.h`)
- **`GBytes *wpe_buffer_import_to_pixels (WPEBuffer *buffer, GError **error);`** — Imports/maps `WPEBuffer` to CPU-readable pixel bytes.
- **`GBytes *wpe_buffer_shm_get_data (WPEBufferSHM *buffer);`** — Returns raw pixel data from shared-memory buffer.
- **`int wpe_buffer_get_width (WPEBuffer *buffer);`**
- **`int wpe_buffer_get_height (WPEBuffer *buffer);`**
- **`guint wpe_buffer_shm_get_stride (WPEBufferSHM *buffer);`**

---

## 4. Required Headers

When compiling TARVA against WPEPlatform Headless:
```cpp
#include <glib.h>
#include <wpe/wpe-platform.h>
#include <wpe/headless/WPEDisplayHeadless.h>
#include <wpe/webkit/wpe-webkit.h>
```

---

## 5. Required pkg-config Modules

When linking TARVA against the minimal WPE 2.52.5 stack:
```bash
pkg-config --cflags --libs wpe-webkit-2.0 wpe-platform-2.0
```
*(Note: `wpe-1.0` and `wpebackend-fdo-1.0` are completely unneeded when `ENABLE_WPE_LEGACY_API=OFF`).*

---

## 6. Final Exact CMake Command

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

## 7. Final Dependency List

### Build-time System Packages (Ubuntu 24.04 LTS)
`build-essential`, `cmake`, `ninja-build`, `pkg-config`, `git`, `curl`, `python3`, `perl`, `ruby`, `bison`, `flex`, `gperf`, `unifdef`, `libglib2.0-dev`, `libsoup-3.0-dev`, `libepoxy-dev`, `libegl1-mesa-dev`, `libgles2-mesa-dev`, `libgbm-dev`, `libharfbuzz-dev`, `libfreetype-dev`, `libpng-dev`, `libjpeg-dev`, `libwebp-dev`, `libxml2-dev`, `libxslt1-dev`, `libsqlite3-dev`, `libgstreamer1.0-dev`, `libgstreamer-plugins-base1.0-dev`, `nlohmann-json3-dev`.

### Runtime Libraries Produced
- `libWPEWebKit-2.0.so` (installed to `/usr/local/lib/x86_64-linux-gnu`)
- `libWPEPlatform-2.0.so` (installed to `/usr/local/lib/x86_64-linux-gnu`)
- `wpe-webkit-2.0.pc` and `wpe-platform-2.0.pc`
