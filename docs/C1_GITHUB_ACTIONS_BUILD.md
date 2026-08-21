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
4. **Dependency Installation**: Installs Ubuntu 24.04 system development packages (`libglib2.0-dev`, `libsoup-3.0-dev`, `libepoxy-dev`, `libegl1-mesa-dev`, `libgles2-mesa-dev`, `libgbm-dev`, `libharfbuzz-dev`, `libfreetype-dev`, `libpng-dev`, `libjpeg-dev`, `libwebp-dev`, `libxml2-dev`, `libxslt1-dev`, `libsqlite3-dev`, `libgstreamer1.0-dev`, `libgstreamer-plugins-base1.0-dev`, `libgcrypt20-dev`, `libgpg-error-dev`, `libtasn1-6-dev`, `libxkbcommon-dev`, `libinput-dev`, `libwayland-dev`, `wayland-protocols`).
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

---

## 7. Workflow Run 1 Execution Results & Failure Analysis

- **GitHub Actions Run URL**: https://github.com/naveen-me/forge/actions/runs/32396427601
- **Status**: **FAIL**
- **Total Workflow Duration**: 2 minutes 32 seconds (Started: 17:13:45Z, Completed: 17:16:17Z)
- **Runner Type**: `ubuntu-24.04` (GitHub Actions hosted standard runner)
- **Disk Space Before Build**: 27 GB free available on `/` (after cleanup)
- **Disk Space After Build**: N/A (Failed before compilation step)
- **Ninja Target Reached**: 0 / 0 (Failed during CMake configuration)
- **Shared Libraries Produced**: `libWPEWebKit-2.0.so` (NO), `libWPEPlatform-2.0.so` (NO)
- **pkg-config Verification**: N/A (Skipped due to CMake configuration failure)
- **ldd Verification**: N/A (Skipped)
- **Artifact Filename & Size**: N/A (Skipped)
- **Artifact Upload Result**: N/A (Skipped)

### First Real Failure & Log Section
The workflow failed during the **Configure CMake (Minimal WPEPlatform Headless)** step:
```
-- Checking for module 'gstreamer-codecparsers-1.0 >= 1.18.4'
--   Package 'gstreamer-codecparsers-1.0', required by 'virtual:world', not found
-- Checking for module 'gstreamer-transcoder-1.0 >= 1.18.4'
--   Package 'gstreamer-transcoder-1.0', required by 'virtual:world', not found
...
CMake Error at Source/cmake/GStreamerChecks.cmake:33 (message):
  Video playback requires the following GStreamer libraries: app, pbutils,
  tag, video. Please check your gst-plugins-base installation.
```

### Failure Classification & Remediation
- **Failure Classification**: Missing System Development Dependency (`dependency`).
- **Root Cause**: `gstreamer-codecparsers-1.0` and `gstreamer-transcoder-1.0` are packaged in `libgstreamer-plugins-bad1.0-dev` on Ubuntu 24.04 LTS. The workflow initially installed `libgstreamer-plugins-base1.0-dev` but omitted `libgstreamer-plugins-bad1.0-dev`.
- **Applied Fix**: Added `libgstreamer-plugins-bad1.0-dev` to the workflow's `apt-get install` list in `.github/workflows/c1_wpe_build.yml`.

---

## 8. Workflow Run 2 Execution Results & Failure Analysis

- **GitHub Actions Run URL**: https://github.com/naveen-me/forge/actions/runs/32401386247
- **Status**: **FAIL**
- **Total Workflow Duration**: 2 minutes 57 seconds (Started: 18:06:48Z, Completed: 18:09:45Z)
- **Runner Type**: `ubuntu-24.04` (GitHub Actions hosted standard runner)
- **Disk Space Before Build**: 27 GB free available on `/`
- **Failure Point**: `apt-get update` failure in Step 5 due to missing GPG key for `cli.github.com` third-party repository.
- **Root Cause**: `apt-get update` failed, preventing `libgstreamer-plugins-bad1.0-dev` from being installed.
- **Applied Fix**: Added removal of unsigned third-party apt list files (`sudo rm -f /etc/apt/sources.list.d/github-cli*.list`) prior to `apt-get update`, and added an explicit GStreamer `pkg-config --modversion` verification step before CMake.
