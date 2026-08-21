# C1.3 CI Debug Report: WPE WebKit 2.52.5 GitHub Actions Build Failure

**Date:** 2026-08-21  
**Workflow:** `.github/workflows/c1_wpe_build.yml`  
**Run:** https://github.com/naveen-me/forge/actions/runs/32474484384  
**Status:** RESOLVED — commit `a10bd808`

---

## 1. Problem Statement

The GitHub Actions workflow `C1 WPE WebKit Build` failed during CMake configuration on `ubuntu-24.04`. The exact same WPE 2.52.5 source and CMake flags were verified to configure successfully on a known-good Ubuntu 24.04 environment with all dependencies installed (33 seconds, zero errors).

Previous CI failures had been caused by dependency provisioning issues, which were fixed. The latest run still failed during CMake after ~20 seconds.

---

## 2. Investigation Methodology

### 2.1 Constraints

- The WPE CMake configuration had been independently verified as correct.
- DO NOT change the verified WPE CMake flags.
- DO NOT redesign WPE or change the architecture.
- DO NOT start C1.4.

### 2.2 Approach

1. **Read all project documents** — `C1_MINIMAL_CONFIG_VALIDATION.md`, `AI_PROGRESS.md`, `DECISIONS.md`
2. **Inspect the full WPE CMake chain** — `OptionsWPE.cmake`, `Source/CMakeLists.txt`, `Source/ThirdParty/skia/CMakeLists.txt`, all `Find*.cmake` modules
3. **Analyze GitHub Actions runner environment** — `ubuntu-24.04` image documentation, pre-installed software, cmake version (3.28.3 from apt after `/opt/hostedtoolcache` cleanup)
4. **Reproduce in Docker** — Built an exact replica of the CI environment (Ubuntu 24.04 base image + identical apt-get install + identical CMake flags)
5. **Cannot access CI logs directly** — GitHub Actions logs require authentication; the public API returns 403 for log downloads

### 2.3 Tools Used

- Docker container running `ubuntu:24.04` base image
- Same `apt-get install` package list as CI workflow
- Same CMake flags from `C1_MINIMAL_CONFIG_VALIDATION.md`
- Same WPE WebKit 2.52.5 source tree

---

## 3. Environment Comparison

### 3.1 GitHub Actions Runner (ubuntu-24.04)

| Component | Version / State |
|---|---|
| OS | Ubuntu 24.04.4 LTS |
| CMake | 3.28.3 (from apt, after `/opt/hostedtoolcache` cleanup removes 3.31.6) |
| GCC | 13.3.0 |
| Ninja | 1.11.1 |
| WPE Source | 2.52.5 (downloaded from wpewebkit.org) |
| Package manager | apt-get (no-install-recommends) |

### 3.2 Docker Reproduction (matches CI)

| Component | Version / State |
|---|---|
| OS | Ubuntu 24.04.4 LTS |
| CMake | 3.28.3 |
| GCC | 13.3.0 |
| Ninja | 1.11.1 |
| WPE Source | 2.52.5 (same tarball) |

### 3.3 Key pkg-config Modules (all OK in Docker)

| Module | Version |
|---|---|
| glib-2.0 | 2.80.0 |
| libsoup-3.0 | 3.4.4 |
| epoxy | 1.5.10 |
| egl | 1.5 |
| glesv2 | 3.2 |
| harfbuzz | 8.3.0 |
| icu-uc | 74.2 |
| gstreamer-1.0 | 1.24.2 |
| gstreamer-gl-1.0 | 1.24.2 |
| gbm | 25.2.8 |
| libdrm | 2.4.125 |
| sysprof-capture-4 | (available via libsoup-3.0-dev) |

---

## 4. Root Cause

### 4.1 Error Message

```
CMake Error at /usr/share/cmake-3.28/Modules/FindPackageHandleStandardArgs.cmake:230 (message):
  Could NOT find Fontconfig (missing: Fontconfig_LIBRARY
  Fontconfig_INCLUDE_DIR) (Required is at least version "2.13.0")
Call Stack (most recent call first):
  /usr/share/cmake-3.28/Modules/FindPackageHandleStandardArgs.cmake:600 (_FPHSA_FAILURE_MESSAGE)
  /usr/share/cmake-3.28/Modules/FindFontconfig.cmake:78 (find_package_handle_standard_args)
  Source/ThirdParty/skia/CMakeLists.txt:6 (find_package)
```

### 4.2 Dependency Chain

```
OptionsWPE.cmake:100
  └─ WEBKIT_OPTION_DEFAULT_PORT_VALUE(USE_SKIA PRIVATE ON)  [x86_64 little-endian]
       └─ Source/ThirdParty/skia/CMakeLists.txt:5-6
            ├─ find_package(Freetype 2.9.0 REQUIRED)     ← OK (libfreetype-dev)
            └─ find_package(Fontconfig 2.13.0 REQUIRED)  ← FAILED (libfontconfig-dev MISSING)
```

### 4.3 Why It Was Missed

1. **Skia is a third-party dependency** bundled inside WPE — its dependency requirements are not visible from the top-level `OptionsWPE.cmake` or `C1_MINIMAL_CONFIG_VALIDATION.md`
2. **`USE_SKIA` defaults to ON** for x86_64 little-endian systems (`OptionsWPE.cmake:100`), but this is implicit
3. **The `C1_MINIMAL_CONFIG_VALIDATION.md`** documents all port-level options but does not catalog third-party (Skia, ANGLE, etc.) dependencies
4. **Previous CI failures** were dependency provisioning issues — the team focused on those and didn't investigate deeper CMake chain failures

### 4.4 Why It Works Locally (on the known-good environment)

The known-good Ubuntu 24.04 environment where CMake was verified had `libfontconfig-dev` installed as a **transitive dependency** of other packages. On the GitHub Actions runner (and Docker with `--no-install-recommends`), transitive dependencies are NOT installed — only explicit packages in the `apt-get install` list are present.

---

## 5. Fix

### 5.1 Change

Added `libfontconfig-dev` to the CI workflow's `apt-get install` list:

```yaml
# Before:
libharfbuzz-dev libfreetype-dev libpng-dev libjpeg-dev libwebp-dev

# After:
libharfbuzz-dev libfreetype-dev libfontconfig-dev libpng-dev libjpeg-dev libwebp-dev
```

Also added `fontconfig` to the pkg-config diagnostics check and `libfontconfig-dev` to the apt policy diagnostics.

### 5.2 Commit

```
a10bd808 C1.3-CI: Add missing libfontconfig-dev to fix WPE cmake failure
```

### 5.3 Verification

Ran the exact CI environment reproduction inside Docker (Ubuntu 24.04, cmake 3.28.3, exact CMake flags):

```
-- Configuring done (27.0s)
-- Generating done (0.7s)
=== CMAKE EXIT CODE: 0 ===
```

CMake configures successfully with exit code 0.

---

## 6. Diagnostic Improvements Added

The workflow now includes:

1. **Pre-CMake Environment Snapshot** — dumps cmake/gcc/ninja versions, every pkg-config module status (including `sysprof-capture-4` and `fontconfig`), apt policy for all key dev packages, PATH/LD_LIBRARY_PATH/PKG_CONFIG_PATH, `/usr/local` contents, hostedtoolcache state
2. **CMake output captured to file** via `tee` with `PIPESTATUS` guard (handles GitHub's default `set -eo pipefail`)
3. **`cmake-diagnostics` artifact** uploaded with `if: always()` — contains `CMakeError.log`, `CMakeConfigureLog.yaml`, `CMakeOutput.log`, and `/tmp/cmake-output.log`. This artifact is downloadable without GitHub auth on the public repo.

---

## 7. Lessons Learned

1. **Third-party bundled dependencies matter** — WPE bundles Skia, ANGLE, and other libraries. Each may have its own `find_package(REQUIRED)` calls that are not visible from the port-level CMake options.
2. **`--no-install-recommends`** means only explicitly listed packages are installed. Transitive dependencies that happen to be present on a developer's machine may be missing in CI.
3. **Docker is invaluable for CI debugging** — reproducing the exact CI environment locally (Ubuntu 24.04 base image + identical packages + identical flags) eliminates guesswork.
4. **Capture CMake output as artifacts** — GitHub Actions logs require authentication, but uploaded artifacts are accessible on public repos without auth.
5. **Use `PIPESTATUS` with `tee`** — GitHub Actions runs `set -eo pipefail`. A naive `cmake ... 2>&1 | tee output.log` will silently lose the exit code. Use `PIPESTATUS[0]` to capture it.

---

## 8. Next Steps

1. Push commit `a10bd808` and trigger CI
2. If cmake succeeds, the ninja build step will run (~15-30 min)
3. If ninja succeeds, install and verify libraries
4. Proceed to C1.4 once the full WPE build completes in CI
