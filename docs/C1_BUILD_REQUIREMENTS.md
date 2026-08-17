# C1 Build Host Requirements

Reference: `docs/CORRECTION_PLAN.md` Phase C1, `DECISIONS.md` ADR-020, `scripts/bootstrap_env.sh`,
`scripts/c1_environment_check.sh`.

C1 proves: **headless WPE (WPEPlatform / `WPEDisplayHeadless`) renders a controlled page to a
CPU-readable 1920x1080 RGBA buffer with no X server, no GPU, no Xvfb.** This gate must be
executed on a capable build host because WPE WebKit is not packaged for Ubuntu 24.04/26.04 and
must be built from source.

The current development host (4 threads, 10 GiB RAM, swap in use) is **not** suitable — see the
blocker record in `AI_PROGRESS.md` (Workstream A, 0.3). Run `scripts/c1_environment_check.sh`
on any candidate host first; it only inspects and reports.

---

## 1. Recommended build host

| Resource | Recommended | Minimum viable | Notes |
|---|---|---|---|
| CPU | >= 8 physical cores / 16 threads, x86_64 | 4 cores / 8 threads | WPE WebKit build is highly parallel; `-j$(nproc)` on 8+ cores is the sweet spot |
| RAM | >= 16 GiB free | 8 GiB (+ swap) | WPE WebKit parallel compile peaks ~10-16 GiB with `-j8` |
| Free disk | >= 50 GiB | >= 30 GiB | Source + build dirs; WebKit build dir alone can exceed 15 GiB |
| OS | Ubuntu 24.04 LTS or 26.04 LTS (x86_64) | Debian 12/13, or any glibc-based distro with GCC >= 13 | `wpewebkit` / `libwpe` / `wpebackend-fdo` / `gpac` are NOT packaged — all built from source |
| Compiler | GCC >= 13 or Clang >= 15 (C++20 required) | same | Ubuntu 24.04 ships GCC 13; 26.04 ships GCC 15 |
| CMake | 3.28 - 3.31 | >= 3.16 | **Avoid CMake 4.x**: WPE WebKit's build is incompatible with it (FindRuby legacy path). `bootstrap_env.sh` pins 3.31.7 for this reason |
| Ninja | required for `libwpe`/`WPEBackend-fdo` (meson), recommended for WPE WebKit | — | meson needs ninja |

Expected build time for the full source stack (8 cores): **~1.5 - 3 h** total, dominated by
WPE WebKit (~1 - 2.5 h). On 16+ cores: ~30 - 60 min. On the "minimum viable" 4-core host:
expect many hours (this is why the current host is excluded).

## 2. Software stack versions (build from source)

| Component | Version | Build system | Produces |
|---|---|---|---|
| libwpe | >= 1.16 (reference: 1.17.0) | meson/ninja | `libwpe.so`, `wpe-1.0.pc` |
| WPEBackend-fdo | >= 1.16 (reference: 1.17.0) | meson/ninja | `libWPEBackend-fdo.so`, `wpebackend-fdo-1.0.pc` |
| WPE WebKit | **>= 2.52** (provides WPEPlatform incl. `WPEDisplayHeadless`) | cmake/ninja | `libwpewebkit-2.0.so*`, headers, `wpe-webkit-2.0.pc` |
| GPAC | current upstream dev (reference: 26.08-DEV) | cmake | `libgpac.so*`, `gpac.pc` (compositor filter) |
| FFmpeg | dev libs >= 6.0 (24.04: 6.1, 26.04: 8.0) | distro package | `libavcodec/format/swscale/swresample/avutil` dev packages |
| Wayland | `libwayland-dev`, `wayland-protocols` | distro package | build deps for WPE stack |

Reference versions observed on the 2026-08-17 dev host (Ubuntu 26.04): g++ 15.2.0,
cmake 4.2.3 (avoid), ninja 1.13.2, ffmpeg 8.0.1, libwpe 1.17.0, wpebackend-fdo 1.17.0,
GPAC 26.08-DEV, libavcodec 62.

## 3. Toolchain / build dependencies

Mirror the package set in `scripts/bootstrap_env.sh` (toolchain + WPE/GPAC/FFmpeg deps):

- `build-essential binutils file make cmake ninja-build pkgconf git curl python3 perl ruby bison flex gperf unifdef`
- WPE WebKit build/runtime deps: `libglib2.0-dev libsoup-3.0-dev libtasn1-6-dev libgcrypt20-dev libgpg-error-dev libxml2-dev libxslt1-dev libsqlite3-dev libjpeg-dev libpng-dev libwebp-dev libharfbuzz-dev libfreetype-dev libwoff-dev libhyphen-dev libseccomp-dev libgstreamer1.0-dev libgstreamer-plugins-base1.0-dev`
- GL/EGL headless software path: `libepoxy-dev libegl1-mesa-dev libgles2-mesa-dev libgbm-dev libgl1-mesa-dev libegl1 libgles2 libgl1 libegl-mesa0 libglx-mesa0 libgl1-mesa-dri` (llvmpipe)
- Wayland: `libxkbcommon-dev libwayland-dev wayland-protocols libdrm-dev libcairo2-dev`
- TARVA media/output: `libavcodec-dev libavformat-dev libswscale-dev libswresample-dev libavutil-dev nlohmann-json3-dev zlib1g-dev libssl-dev libpng-dev libjpeg-dev ffmpeg`

On a root-enabled host, install these with apt and skip `bootstrap_env.sh`; on a non-root
host, run `./scripts/bootstrap_env.sh all` (downloads/extracts .debs into a user prefix).

## 4. Headless CPU-only build/run environment

Environment variables for the WPE/GPAC build and for running the headless engine:

```bash
# --- build ---
export PKG_CONFIG_PATH=/usr/local/lib/x86_64-linux-gnu/pkgconfig:/usr/local/lib/pkgconfig:${PKG_CONFIG_PATH:-}
export LD_LIBRARY_PATH=/usr/local/lib/x86_64-linux-gnu:/usr/local/lib:${LD_LIBRARY_PATH:-}
export CMAKE_PREFIX_PATH=/usr/local:${CMAKE_PREFIX_PATH:-}

# --- run (headless, CPU-only, no X) ---
export LIBGL_ALWAYS_SOFTWARE=1        # force Mesa llvmpipe software GL
export GALLIUM_DRIVER=llvmpipe        # explicit software rasterizer (optional)
export EGL_PLATFORM=surfaceless       # no display server needed
export WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1   # only if the bwrap sandbox is unavailable
```

Constraints (from `DECISIONS.md` ADR-020 and `docs/CORRECTION_PLAN.md` C1):

- One WPE display per process; the display/view must be constructed and driven from one thread
  pumping the glib main context.
- Headless toplevel resize is a no-op on 2.52: choose the layer width/height at construction time.
- The C1 path must be CPU-readable buffer (shared-memory / CPU-mapped `WPEBuffer` contents), not
  GPU DMABUF. Mesa llvmpipe (`LIBGL_ALWAYS_SOFTWARE=1`, surfaceless EGL) is the fallback if
  headless EGL/GBM bootstrap demands a GL context — still CPU-only, still no X.
- No Xvfb, no X server, no discrete GPU. WPE WebKit configure options that must be verified
  against the exact version at build time (per `AGENTS.md`: research upstream yourself):
  headless display enabled (`WPEDisplayHeadless` available), and whether `-DENABLE_JIT=OFF`
  (or similar) is desired for security/stability.

## 5. Expected build artifacts

After a successful C1 host build, the following must exist:

- `libwpe.so*` + `wpe-1.0.pc` (pkg-config visible)
- `libWPEBackend-fdo.so*` + `wpebackend-fdo-1.0.pc`
- WPE WebKit: `libwpewebkit-2.0.so*`, headers, `wpe-webkit-2.0.pc`
- GPAC: `libgpac.so*` + `gpac.pc`
- TARVA: `build/tarva_playout`, `build/run_poc_benchmark`, all `build/test_*` binaries
- C1 evidence (produced by the gate run, not the build): a committed capture-cost measurement
  (per-frame WPE capture well under 33 ms at 1080p) per `docs/CORRECTION_PLAN.md` Gate C1.

## 6. Verifying a candidate host

Run `scripts/c1_environment_check.sh` on the candidate host. It inspects only (CPU, RAM, disk,
OS, compiler, CMake, Ninja, Docker, GPU, display, build deps) and exits non-zero when the host
is clearly below the recommended requirements. It installs nothing.
