# C1.4 Status Report — Why We Need Another CI Build

## TL;DR

The WPE WebKit build **succeeded** (9312/9312 targets, zero errors).
The artifact was **incomplete** — it was missing 3 critical runtime binaries.
We fixed the CI workflow and pushed. The CI is re-building to produce a complete artifact.

---

## What Happened (Timeline)

### Run #32512326532 — Original Failure
- Build: **SUCCESS** (3h 5m, all 9312 targets)
- Install: **SUCCESS** (558 install targets)
- Verify: **FAILED** — wrong path in `ls` command (`/usr/local/lib/x86_64-linux-gnu/` vs `/usr/local/lib/`)
- Artifact produced: **NO** (verify step failed, upload was skipped)

### Run #32551896254 — Path Fix (commit ac03f390)
- Fixed the hardcoded path from `/usr/local/lib/x86_64-linux-gnu/` to `/usr/local/lib/`
- Build: **SUCCESS** (same 3h build, all 9312 targets)
- Install: **SUCCESS**
- Verify: **SUCCESS** — `pkg-config` returned 2.52.5, `ls` found the .so files
- Artifact produced: **YES** — `wpewebkit-2.52.5-ubuntu24.04-x86_64` (42 MB)

### Artifact Downloaded & Installed
- Downloaded the 42 MB tarball
- Extracted to `/usr/local/`
- Verified: `pkg-config --modversion wpe-webkit-2.0 wpe-platform-2.0` → `2.52.5`
- Verified: `libWPEWebKit-2.0.so` at `/usr/local/lib/`

### Test Compilation
- Wrote `tests/test_c1_4_wpe_headless.cpp`
- Compiled successfully against WPE 2.52.5 headers and libraries
- **Also needed**: ICU 74 runtime libraries (downloaded from Ubuntu 24.04 repos)

### Runtime Test — FAILED
```
Unable to spawn a new child process:
Failed to spawn child process "/usr/local/libexec/wpe-webkit-2.0/WPEWebProcess"
(No such file or directory)
```

---

## Why the Artifact Was Incomplete

The CI workflow's **"Stage Artifacts for Upload"** step only copied:
- `lib/` (shared libraries)
- `include/` (headers)
- `pkgconfig/` (pkg-config files)

It did **NOT** copy:
- `libexec/wpe-webkit-2.0/WPEWebProcess` ← **REQUIRED**
- `libexec/wpe-webkit-2.0/WPENetworkProcess` ← **REQUIRED**
- `libexec/wpe-webkit-2.0/WPEGPUProcess` ← **REQUIRED**
- `lib/wpe-webkit-2.0/injected-bundle/libWPEInjectedBundle.so`
- `share/wpe-webkit-2.0/inspector.gresource`

### Why These Binaries Are Required

WPE WebKit uses a **multi-process architecture** (same as Chrome/Safari):

```
Main Process (your app)
  ├── WPEWebProcess    ← renders HTML/CSS/JS in a sandboxed process
  ├── WPENetworkProcess ← handles HTTP/HTTPS network requests
  └── WPEGPUProcess    ← handles GPU/accelerated compositing
```

When you create a `WebKitWebView`, WPE **spawns child processes** to do the actual rendering. Without these binaries, the library crashes immediately.

This is not optional — it's how WebKit works. The main library (`libWPEWebKit-2.0.so`) is just the API surface. The actual rendering happens in `WPEWebProcess`.

---

## The Fix (commit 9423c2e4)

Updated `.github/workflows/c1_wpe_build.yml` "Stage Artifacts for Upload" step to include:
- `libexec/wpe-webkit-2.0/WPEWebProcess`
- `libexec/wpe-webkit-2.0/WPENetworkProcess`
- `libexec/wpe-webkit-2.0/WPEGPUProcess`
- `lib/wpe-webkit-2.0/injected-bundle/libWPEInjectedBundle.so`
- `share/wpe-webkit-2.0/inspector.gresource`

---

## Current Status

| Item | Status |
|---|---|
| WPE build (9312 targets) | ✅ Complete |
| Path fix | ✅ Pushed (ac03f390) |
| libexec fix | ✅ Pushed (9423c2e4) |
| C1.4 test written | ✅ `tests/test_c1_4_wpe_headless.cpp` |
| C1.4 test compiles | ✅ Clean compilation |
| Fixed artifact | ⏳ CI re-building (~3h) |
| C1.4 test run | ⏳ Blocked on artifact |

---

## What Needs to Happen Next

1. Wait for CI to complete (~3 hours from push)
2. Re-download the fixed artifact (now includes libexec binaries)
3. Re-install to `/usr/local/`
4. Run `bash scripts/build_and_run_c1_4.sh`
5. Record C1.4 PASS/FAIL result

---

## Files Created

| File | Purpose |
|---|---|
| `tests/test_c1_4_wpe_headless.cpp` | C1.4 integration test |
| `scripts/build_and_run_c1_4.sh` | Build + run the test |
| `scripts/reinstall_wpe_artifact.sh` | Re-download fixed artifact |
| `scripts/download_icu74.sh` | Download ICU 74 compatibility libs |
| `scripts/install_wpe_artifact.sh` | Initial WPE artifact install |
