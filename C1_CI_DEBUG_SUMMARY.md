# C1.3 CI Debug Summary — 2026-08-21

## What was committed

A new `.github/workflows/c1_wpe_build.yml` was committed to the `tarva` branch.

## What I investigated

- The full WPE WebKit 2.52.5 CMake configuration chain (`OptionsWPE.cmake`, `Source/CMakeLists.txt`, `Find*.cmake` modules)
- The GitHub Actions runner image (`ubuntu-24.04`, image `20260816.277.1`)
  - cmake 3.31.6 from `/opt/hostedtoolcache` (removed by cleanup)
  - Then cmake 3.28.3 from apt after `apt-get install cmake`
- All `find_package` calls in the cmake chain, including hard `FATAL_ERROR` gates for:
  - `libsysprof-capture-4` (via `Source/CMakeLists.txt`)
  - `GStreamer` components (including `gstreamer-gl-1.0`)
  - `Epoxy`, `GLib`, `ICU`, etc.
- Verified the exact same cmake command **succeeds locally** (cmake 4.2.3 on this machine)
- Confirmed the CMake flags are identical to `C1_MINIMAL_CONFIG_VALIDATION.md`

## What I couldn't do

- Access the GitHub Actions logs (require authentication; the public API returns 403)
- Reproduce in Docker (Docker is not installed on this machine)

## What the new workflow adds

1. **Pre-CMake Environment Snapshot** — dumps cmake/gcc/ninja versions, every pkg-config module status (including `sysprof-capture-4`), apt policy for all key dev packages, PATH/LD_LIBRARY_PATH/PKG_CONFIG_PATH, `/usr/local` contents, hostedtoolcache state
2. **CMake output captured to file** via `tee` with `PIPESTATUS` guard (handles GitHub's default `set -eo pipefail`)
3. **`cmake-diagnostics` artifact** uploaded with `if: always()` — contains `CMakeError.log`, `CMakeConfigureLog.yaml`, `CMakeOutput.log`, and `/tmp/cmake-output.log`. This artifact is downloadable without GitHub auth on the public repo.

## Key suspicious areas to check in the artifact

1. `sysprof-capture-4` — cmake does a hard FATAL_ERROR if not found (`Source/CMakeLists.txt:44-48`)
2. `gstreamer-gl-1.0` — required when `ENABLE_VIDEO=ON`, needs EGL+GLES2 headers
3. Any pkg-config module that shows `MISS` in the environment snapshot
4. The exact cmake error line in `CMakeError.log` or stdout

## Next step

Push this commit and run the workflow. The `cmake-diagnostics` artifact will contain the **exact error** that CMake produces. Download it and share the contents.
