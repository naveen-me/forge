#!/usr/bin/env bash
# ============================================================================
# TARVA C1 build-host environment check
#
# Inspects ONLY — never installs, never modifies anything. Reports whether the
# current machine appears suitable for the C1 gate (headless WPE WebKit build
# -> CPU-readable RGBA buffer at 1080p).
#
# Reference thresholds: docs/C1_BUILD_REQUIREMENTS.md
#
# Exit codes:
#   0  suitable (all required checks pass at the recommended level)
#   1  clearly below the recommended C1 requirements (one or more FAIL)
#   2  usage error
# ============================================================================
set -u

REC_THREADS=8          # recommended: >= 8 threads
MIN_THREADS=4          # hard floor
REC_RAM_GIB=16         # recommended: >= 16 GiB total RAM
MIN_RAM_GIB=8          # hard floor
REC_DISK_GIB=50        # recommended free disk
MIN_DISK_GIB=30        # hard floor
GCC_MIN_MAJOR=13       # C++20
CLANG_MIN_MAJOR=15     # C++20
CMAKE_MIN="3.16"

FAILS=0
WARNS=0

say()  { printf '%s\n' "$*"; }
pass() { printf '  [PASS] %s\n' "$*"; }
warn() { printf '  [WARN] %s\n' "$*"; WARNS=$((WARNS+1)); }
fail() { printf '  [FAIL] %s\n' "$*"; FAILS=$((FAILS+1)); }

say "TARVA C1 build-host environment check"
say "======================================"
say ""

# --- OS ---------------------------------------------------------------------
say "OS"
if [ "$(uname -s)" != "Linux" ]; then
    fail "not Linux (uname: $(uname -s))"
else
    pass "Linux $(uname -m)"
fi
if [ "$(uname -m)" != "x86_64" ]; then
    warn "architecture is $(uname -m); x86_64 is the tested target"
fi
if [ -r /etc/os-release ]; then
    # shellcheck disable=SC1091
    . /etc/os-release
    say "  distro: ${PRETTY_NAME:-$NAME $VERSION_ID}"
    case "${ID:-}" in
        ubuntu|debian)
            major="${VERSION_ID%%.*}"
            if [ "$ID" = "ubuntu" ] && [ "$major" -lt 24 ]; then
                warn "Ubuntu $VERSION_ID is older than the tested 24.04/26.04"
            fi
            ;;
        *) warn "distro '$ID' not in the tested set (Ubuntu/Debian)" ;;
    esac
else
    warn "cannot read /etc/os-release"
fi
say ""

# --- CPU --------------------------------------------------------------------
say "CPU"
threads=$(nproc 2>/dev/null || echo 0)
cores=$(awk '/^physical id/{ph[$NF]=1} /^core id/{co[$NF]=1} END{print length(ph)*length(co)}' /proc/cpuinfo 2>/dev/null)
[ "${cores:-0}" = "0" ] && cores="unknown"
say "  threads: $threads, physical cores: $cores"
if [ "$threads" -lt "$MIN_THREADS" ]; then
    fail "only $threads thread(s) — below the $MIN_THREADS-thread hard floor"
elif [ "$threads" -lt "$REC_THREADS" ]; then
    fail "$threads threads is below the recommended $REC_THREADS for a C1 host (multi-hour WPE WebKit build)"
else
    pass "threads >= $REC_THREADS"
fi
say ""

# --- RAM --------------------------------------------------------------------
say "RAM"
if command -v free >/dev/null 2>&1; then
    mem_total=$(free -b | awk '/^Mem:/{print $2}')
    mem_avail=$(free -b | awk '/^Mem:/{print $7}')
else
    mem_total=$(awk '/MemTotal/{print $2*1024}' /proc/meminfo 2>/dev/null || echo 0)
    mem_avail=$mem_total
fi
ram_gib=$(awk -v b="$mem_total" 'BEGIN{printf "%.1f", b/1073741824}')
avail_gib=$(awk -v b="$mem_avail" 'BEGIN{printf "%.1f", b/1073741824}')
say "  total: ${ram_gib} GiB, available: ${avail_gib} GiB"
if awk -v a="$mem_total" -v m="$((MIN_RAM_GIB*1073741824))" 'BEGIN{exit !(a < m)}'; then
    fail "RAM below the $MIN_RAM_GIB GiB hard floor"
elif awk -v a="$mem_total" -v m="$((REC_RAM_GIB*1073741824))" 'BEGIN{exit !(a < m)}'; then
    fail "RAM below the recommended $REC_RAM_GIB GiB for a C1 host (WPE WebKit -j8 build can peak 10-16 GiB)"
else
    pass "RAM >= $REC_RAM_GIB GiB"
fi
if [ -r /proc/meminfo ]; then
    swap_kb=$(awk '/SwapTotal/{print $2}' /proc/meminfo)
    if [ "${swap_kb:-0}" -gt 0 ] && [ "$swap_kb" -lt 1048576 ]; then
        warn "less than 1 GiB swap configured"
    fi
fi
say ""

# --- Disk -------------------------------------------------------------------
say "Disk"
disk_avail_bytes=$(df -B1 --output=avail / 2>/dev/null | tail -1 | tr -d ' ')
if [ -z "${disk_avail_bytes:-}" ] || ! awk -v b="${disk_avail_bytes:-0}" 'BEGIN{exit !(b+0>0)}'; then
    disk_avail_bytes=$(df -Pk / | awk 'NR==2{print $4*1024}')
fi
disk_gib=$(awk -v b="${disk_avail_bytes:-0}" 'BEGIN{printf "%.1f", b/1073741824}')
say "  free on /: ${disk_gib} GiB"
if awk -v b="${disk_avail_bytes:-0}" -v m="$((MIN_DISK_GIB*1073741824))" 'BEGIN{exit !(b < m)}'; then
    fail "free disk below the $MIN_DISK_GIB GiB hard floor (WPE WebKit build needs ~30 GiB)"
elif awk -v b="${disk_avail_bytes:-0}" -v m="$((REC_DISK_GIB*1073741824))" 'BEGIN{exit !(b < m)}'; then
    fail "free disk below the recommended $REC_DISK_GIB GiB for a C1 host"
else
    pass "free disk >= $REC_DISK_GIB GiB"
fi
say ""

# --- Compiler ---------------------------------------------------------------
say "Compiler (C++20 required)"
cxx=""
if command -v g++ >/dev/null 2>&1; then
    gver=$(g++ --version | head -1 | grep -oE '[0-9]+' | head -1)
    say "  g++ $(g++ --version | head -1)"
    if [ "${gver:-0}" -ge "$GCC_MIN_MAJOR" ]; then
        pass "g++ >= $GCC_MIN_MAJOR"
        cxx="g++"
    else
        fail "g++ $gver is older than $GCC_MIN_MAJOR (C++20 required)"
    fi
else
    warn "g++ not found"
fi
if command -v clang++ >/dev/null 2>&1; then
    cver=$(clang++ --version | head -1 | grep -oE '[0-9]+' | head -1)
    say "  clang++ $(clang++ --version | head -1)"
    if [ -z "$cxx" ] && [ "${cver:-0}" -ge "$CLANG_MIN_MAJOR" ]; then
        pass "clang++ >= $CLANG_MIN_MAJOR"
        cxx="clang++"
    elif [ "${cver:-0}" -lt "$CLANG_MIN_MAJOR" ]; then
        warn "clang++ $cver is older than $CLANG_MIN_MAJOR"
    fi
else
    [ -n "$cxx" ] || warn "clang++ not found"
fi
[ -n "$cxx" ] || fail "no C++20 compiler found"
say ""

# --- CMake / Ninja ----------------------------------------------------------
say "Build tooling"
if command -v cmake >/dev/null 2>&1; then
    cmake_ver=$(cmake --version | head -1 | grep -oE '[0-9]+\.[0-9]+' | head -1)
    say "  cmake $cmake_ver"
    cmake_major="${cmake_ver%%.*}"
    if [ "$cmake_major" -ge 4 ]; then
        warn "CMake $cmake_ver: WPE WebKit's build is incompatible with CMake 4.x (use 3.28-3.31; bootstrap_env.sh pins 3.31.7)"
    elif awk -v v="$cmake_ver" -v m="$CMAKE_MIN" 'BEGIN{exit !(v+0 < m+0)}'; then
        fail "CMake $cmake_ver is older than $CMAKE_MIN"
    else
        pass "cmake >= $CMAKE_MIN (and < 4.x)"
    fi
else
    fail "cmake not found"
fi
if command -v ninja >/dev/null 2>&1; then
    pass "ninja $(ninja --version 2>/dev/null | head -1) present (required for libwpe/WPEBackend-fdo meson builds)"
else
    fail "ninja not found (required by the meson builds for libwpe/WPEBackend-fdo)"
fi
if command -v meson >/dev/null 2>&1; then
    say "  meson $(meson --version 2>/dev/null)"
else
    warn "meson not found (needed to build libwpe/WPEBackend-fdo; distro package 'meson' or pip)"
fi
if command -v docker >/dev/null 2>&1; then
    say "  docker: present ($(docker version --format '{{.Server.Version}}' 2>/dev/null || echo 'daemon not reachable'))"
else
    warn "docker not found (optional; a plain host build works and is preferred for C1)"
fi
say ""

# --- GPU / display ----------------------------------------------------------
say "GPU / display (C1 must run CPU-only, no X)"
if command -v lspci >/dev/null 2>&1; then
    gpu_line=$(lspci 2>/dev/null | grep -Ei 'vga|3d|display' | head -3)
    if [ -n "$gpu_line" ]; then
        say "  $gpu_line"
        if echo "$gpu_line" | grep -qiE 'nvidia|radeon|amd|rtx|gtx'; then
            warn "discrete GPU detected — C1 targets the CPU path (llvmpipe); this is informational only"
        else
            say "  (integrated/software GPU — fine for the CPU-only path)"
        fi
    fi
else
    warn "lspci not available; cannot enumerate GPUs (informational)"
fi
if [ -d /dev/dri ]; then
    say "  /dev/dri present: $(ls /dev/dri 2>/dev/null | tr '\n' ' ')"
else
    say "  /dev/dri absent (fine — CPU path uses Mesa llvmpipe)"
fi
say "  DISPLAY=${DISPLAY:-<unset>}"
if [ -n "${DISPLAY:-}" ] || command -v Xvfb >/dev/null 2>&1; then
    warn "X server/Xvfb available — C1 must be validated WITHOUT X (use LIBGL_ALWAYS_SOFTWARE=1, EGL_PLATFORM=surfaceless); informational only"
else
    pass "no X server / Xvfb detected (matches the no-X requirement)"
fi
say ""

# --- Build deps (informational) ---------------------------------------------
say "Build dependencies (informational — installed by the C1 build, not blockers)"
for mod in wpe-1.0 wpebackend-fdo-1.0 wpe-webkit-2.0 gpac libavcodec libavformat libswscale libswresample libavutil cairo wayland-server nlohmann_json; do
    if pkg-config --exists "$mod" 2>/dev/null; then
        say "  $mod: $(pkg-config --modversion "$mod" 2>/dev/null)"
    else
        say "  $mod: not present (expected on a fresh C1 host — will be built/installed)"
    fi
done
say ""

# --- Verdict ----------------------------------------------------------------
say "======================================"
if [ "$FAILS" -gt 0 ]; then
    say "VERDICT: NOT SUITABLE for C1 ($FAILS FAIL, $WARNS WARN)"
    say "This host is clearly below the recommended C1 requirements (docs/C1_BUILD_REQUIREMENTS.md)."
    say "Run C1 on a capable build host or CI instead."
    exit 1
fi
if [ "$WARNS" -gt 0 ]; then
    say "VERDICT: SUITABLE for C1, with $WARNS warnings (review above)"
else
    say "VERDICT: SUITABLE for C1"
fi
exit 0
