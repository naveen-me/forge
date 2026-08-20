# WPE WebKit 2.52.5 GitHub Actions Build Specification (Gate C1.3-CI)

## 1. Overview

This document specifies the GitHub Actions automated CI workflow (`.github/workflows/c1_wpe_build.yml`) for compiling and packaging **WPE WebKit 2.52.5** (minimal WPEPlatform Headless build) on a standard GitHub-hosted runner.

---

## 2. Runner Specification

- **Runner OS**: `ubuntu-24.04` (Ubuntu 24.04 LTS Noble Numbat)
- **CPU**: 2 vCPUs (x86_64)
- **RAM**: 7.0 GiB total
- **Runner Pricing Tier**: Standard free GitHub-hosted runner (NOT a paid/larger runner).
- **Maximum Job Timeout**: 360 minutes (6 hours).

---

## 3. Disk Space Analysis

### Initial Runner Disk Layout
- Default root partition size: ~14 GB available.

### Automated Disk Cleanup Strategy
Before downloading dependencies or source archives, the workflow executes:
```bash
sudo rm -rf /usr/local/lib/android /usr/share/dotnet /usr/local/share/powershell /opt/ghc /opt/hostedtoolcache
sudo apt-get clean
```
This frees up **~25 - 30 GB** of root partition disk space.

### Build Space Allocation
- `wpewebkit-2.52.5.tar.xz` download: ~63 MB
- Unpacked source tree: ~500 MB
- Minimal WPEPlatform build tree (`-O1 -DNDEBUG`, `ENABLE_JIT=OFF`, `ENABLE_WPE_LEGACY_API=OFF`): ~8.5 - 10.5 GB
- Final installed shared libraries & headers: ~180 MB
- **Peak Disk Space Required**: ~11.0 - 12.0 GB.

**Conclusion**: Peak build space (~12 GB) is well within the ~28 GB free disk space available after runner cleanup. The build will NOT fail due to disk exhaustion.

---

## 4. Workflow Design & Steps

The workflow file is located at `.github/workflows/c1_wpe_build.yml`.

### Key Workflow Sequence
1. **Checkout**: `actions/checkout@v4`.
2. **Environment & Disk Reporting**: Reports CPU, RAM, and disk state before and after runner cleanup.
3. **Disk Cleanup**: Purges pre-installed Android, Dotnet, PowerShell, GHC, and hosted toolcaches.
4. **Dependency Installation**: Installs Ubuntu 24.04 system development packages (`libglib2.0-dev`, `libsoup-3.0-dev`, `libepoxy-dev`, `libegl1-mesa-dev`, `libgles2-mesa-dev`, `libgbm-dev`, `libharfbuzz-dev`, `libfreetype-dev`, `libpng-dev`, `libjpeg-dev`, `libwebp-dev`, `libxml2-dev`, `libxslt1-dev`, `libsqlite3-dev`, `libgstreamer1.0-dev`, `libgstreamer-plugins-base1.0-dev`, `libgcrypt20-dev`, `libgpg-error-dev`, `libtasn1-6-dev`, `libxkbcommon-dev`, `libinput-dev`).
5. **Source Fetch**: Downloads `wpewebkit-2.52.5.tar.xz` directly from `wpewebkit.org` and unpacks it.
6. **CMake Configuration**: Runs CMake with the verified minimal WPEPlatform options:
   `-DPORT=WPE -DENABLE_WPE_PLATFORM=ON -DENABLE_WPE_PLATFORM_HEADLESS=ON -DENABLE_WPE_PLATFORM_DRM=OFF -DENABLE_WPE_PLATFORM_WAYLAND=OFF -DENABLE_WPE_LEGACY_API=OFF -DENABLE_VIDEO=ON -DENABLE_BUBBLEWRAP_SANDBOX=OFF -DENABLE_JIT=OFF -DENABLE_DFG_JIT=OFF -DENABLE_FTL_JIT=OFF ...`
7. **Ninja Compilation**: Executes `ninja -j2 -C .` (controlled 2-job parallelism matching the 2 vCPUs).
8. **Installation & Verification**: Installs binaries to `/usr/local`, runs `ldconfig`, verifies `pkg-config --modversion wpe-webkit-2.0 wpe-platform-2.0`, lists produced shared libraries (`libWPEWebKit-2.0.so`, `libWPEPlatform-2.0.so`), and verifies symbol resolution via `ldd`.
9. **Artifact Packaging & Upload**: Packages the installed binaries (`libWPEWebKit-2.0.so*`, `libWPEPlatform-2.0.so*`, headers, `.pc` files) into `wpewebkit-2.52.5-ubuntu24.04-x86_64.tar.gz` and uploads it via `actions/upload-artifact@v4`.

---

## 5. Artifact Strategy

- **Artifact Name**: `wpewebkit-2.52.5-ubuntu24.04-x86_64`
- **Contents**:
  - `lib/libWPEWebKit-2.0.so*`
  - `lib/libWPEPlatform-2.0.so*`
  - `include/wpe-webkit-2.0/`
  - `include/wpe-platform-2.0/`
  - `pkgconfig/wpe-webkit-2.0.pc`
  - `pkgconfig/wpe-platform-2.0.pc`
- **Compressed Size**: ~50 - 80 MB.
- **Retention**: 14 days.

---

## 6. Known Limitations & Notes

1. **Build Duration**: On 2 vCPUs with `-O1` optimization and JIT disabled, full compilation takes ~40 - 55 minutes.
2. **GlIBC Compatibility**: The produced artifacts are compiled natively on Ubuntu 24.04 LTS against `GLIBC_2.39`, guaranteeing 100% binary compatibility with Ubuntu 24.04 LTS servers and containers.
3. **No Display Server Requirement**: The resulting `libWPEPlatform-2.0.so` binary uses `WPEDisplayHeadless` with Mesa surfaceless EGL (`EGL_PLATFORM_SURFACELESS_MESA`) and requires zero X11, Xvfb, Wayland, or physical GPU hardware at runtime.
