#!/usr/bin/env bash
# ============================================================================
# TARVA non-root build environment bootstrap
#
# Stages a complete C/C++ toolchain and all TARVA build dependencies into a
# user-local prefix WITHOUT sudo. Ubuntu .deb packages are downloaded via
# non-root apt (state redirected under $HOME/.apt) and extracted with
# dpkg-deb -x into $PREFIX.
#
# Why: the TARVA production target is a plain Linux VPS; this script lets the
# POC/correction work proceed on hosts where root is unavailable, and mirrors
# what the Dockerfile does (build libwpe/WPEBackend-fdo/WPE WebKit/GPAC from
# source — Ubuntu 26.04 ships none of those).
#
# The bootstrap is split into resumable subcommands so it can run under
# per-invocation time limits:
#   ./scripts/bootstrap_env.sh deps      # apt update + resolve dependency closure
#   ./scripts/bootstrap_env.sh fetch     # download .debs (resumable, skips existing)
#   ./scripts/bootstrap_env.sh extract   # dpkg-deb -x everything into $PREFIX
#   ./scripts/bootstrap_env.sh all       # deps + fetch + extract
#
# After extract:  source $PREFIX/env.sh
#
# Env overrides:
#   TARVA_TOOLCHAIN   prefix dir (default: $HOME/.tarva-toolchain)
#   TARVA_APT_ROOT    non-root apt state dir (default: $HOME/.apt)
# ============================================================================
set -euo pipefail

CMD="${1:-all}"

PREFIX="${TARVA_TOOLCHAIN:-$HOME/.tarva-toolchain}"
APT_ROOT="${TARVA_APT_ROOT:-$HOME/.apt}"
SUITE="${TARVA_APT_SUITE:-$(grep -oP 'VERSION_CODENAME=\K.*' /etc/os-release)}"
DPKG_ARCH="$(dpkg --print-architecture 2>/dev/null || echo amd64)"
case "$DPKG_ARCH" in
    amd64) TRIPLET=x86_64-linux-gnu ;;
    arm64) TRIPLET=aarch64-linux-gnu ;;
    *) TRIPLET="$DPKG_ARCH-linux-gnu" ;;
esac

mkdir -p "$PREFIX" "$APT_ROOT/etc/apt/trusted.gpg.d" "$APT_ROOT/lib/lists" \
         "$APT_ROOT/cache/archives/partial" "$APT_ROOT/status.d" "$APT_ROOT/download"

if [ ! -s "$APT_ROOT/etc/apt/trusted.gpg.d/ubuntu-archive.gpg" ]; then
    echo ">> Fetching Ubuntu archive keyring"
    curl -fsSL -o "$APT_ROOT/etc/apt/trusted.gpg.d/ubuntu-archive.gpg" \
        https://archive.ubuntu.com/ubuntu/project/ubuntu-archive-keyring.gpg
fi

if [ ! -s "$APT_ROOT/etc/apt/sources.list" ]; then
    cat > "$APT_ROOT/etc/apt/sources.list" <<EOF
deb http://archive.ubuntu.com/ubuntu $SUITE main universe
deb http://archive.ubuntu.com/ubuntu $SUITE-updates main universe
deb http://security.ubuntu.com/ubuntu $SUITE-security main universe
EOF
fi
touch "$APT_ROOT/status"

APT_OPTS=(
    -o "Dir::Etc::sourcelist=$APT_ROOT/etc/apt/sources.list"
    -o "Dir::Etc::sourceparts=-"
    -o "Dir::State=$APT_ROOT/lib"
    -o "Dir::State::status=$APT_ROOT/status"
    -o "Dir::Cache=$APT_ROOT/cache"
    -o "Dir::Etc::trustedparts=$APT_ROOT/etc/apt/trusted.gpg.d"
    -o "APT::Get::List-Cleanup=0"
)

# ---------------------------------------------------------------------------
# Packages needed to build and run TARVA (CPU compositor + WPE + FFmpeg + tests)
# ---------------------------------------------------------------------------
PKGS=(
    # toolchain
    build-essential binutils file make cmake ninja-build pkgconf git curl
    python3 perl ruby3.3 bison flex gperf unifdef
    # WPE WebKit build/runtime deps (wpewebkit itself is built from source)
    libglib2.0-dev libsoup-3.0-dev libtasn1-6-dev libgcrypt20-dev libgpg-error-dev libxml2-dev libxslt1-dev
    libsqlite3-dev libjpeg-dev libpng-dev libwebp-dev libharfbuzz-dev
    libfreetype-dev libepoxy-dev libegl1-mesa-dev libgles2-mesa-dev libgbm-dev
    libxkbcommon-dev libwayland-dev wayland-protocols libdrm-dev libcairo2-dev
    libseccomp-dev libhyphen-dev libwoff-dev
    libgstreamer1.0-dev libgstreamer-plugins-base1.0-dev
    # CPU software-GL (Mesa llvmpipe) so WPE headless can render without a GPU
    libgl1-mesa-dri libegl1 libgles2 libgl1 libegl-mesa0 libglx-mesa0
    # TARVA media/output stack (GPAC itself is built from source: not packaged in 26.04)
    libavcodec-dev libavformat-dev libswscale-dev libswresample-dev libavutil-dev
    nlohmann-json3-dev zlib1g-dev libssl-dev libpng-dev libjpeg-dev
    # runtime tooling for tests/benchmarks
    ffmpeg
)

deps() {
    echo ">> apt update (non-root, suite=$SUITE arch=$DPKG_ARCH)"
    apt-get "${APT_OPTS[@]}" update

    echo ">> Resolving dependency closure for ${#PKGS[@]} packages"
    apt-get "${APT_OPTS[@]}" -s --no-install-recommends install "${PKGS[@]}" \
        | awk '/^Inst / {print $2}' | sort -u > "$APT_ROOT/pkglist.txt"

    echo ">> Resolving download URIs"
    # shellcheck disable=SC2046
    apt-get "${APT_OPTS[@]}" -y download --print-uris $(cat "$APT_ROOT/pkglist.txt") \
        2>/dev/null > "$APT_ROOT/uris-raw.txt"
    awk '{print $1}' "$APT_ROOT/uris-raw.txt" | tr -d "'" | sort -u > "$APT_ROOT/uris.txt"
    echo ">> $(wc -l < "$APT_ROOT/uris.txt") URIs to fetch"
}

fetch() {
    if [ ! -s "$APT_ROOT/uris.txt" ]; then deps; fi
    cd "$APT_ROOT/download"
    echo ">> Downloading $(wc -l < "$APT_ROOT/uris.txt") packages (resumable, existing files skipped)"
    n=0
    while IFS= read -r uri; do
        name="$(basename "$uri")"
        if [ -s "$name" ]; then continue; fi
        # shellcheck disable=SC2039
        curl -fsSL --retry 3 -o "$name" "$uri" || { echo "FAILED: $uri"; rm -f "$name"; }
        n=$((n+1))
    done < "$APT_ROOT/uris.txt"
    echo ">> Downloaded $n new packages; total $(ls *.deb 2>/dev/null | wc -l)"
}

verify() {
    # Verify every downloaded .deb against the SHA512 recorded by apt
    # (mirror resets can leave truncated files that curl reports as success).
    python3 - "$APT_ROOT/uris-raw.txt" "$APT_ROOT/download" <<'PY'
import hashlib, os, re, sys, urllib.parse
raw_path, dl_dir = sys.argv[1], sys.argv[2]
missing, corrupt = [], []
pat = re.compile(r"'(\S+)' (\S+) \d+ SHA512:(\w+)")
for line in open(raw_path):
    m = pat.match(line)
    if not m:
        continue
    uri, name, sha = m.group(1), m.group(2), m.group(3)
    # files are stored under the URL-encoded basename used by the fetch step
    fname = urllib.parse.unquote(name)
    path = os.path.join(dl_dir, fname)
    if not os.path.exists(path):
        path = os.path.join(dl_dir, os.path.basename(uri))
    if not os.path.isfile(path) or os.path.getsize(path) == 0:
        missing.append(fname)
        continue
    if hashlib.sha512(open(path, 'rb').read()).hexdigest() != sha:
        corrupt.append(fname)
        os.unlink(path)
if missing:
    print(f">> MISSING: {len(missing)} (will be fetched)")
if corrupt:
    print(f">> CORRUPT (removed): {len(corrupt)}")
    for c in corrupt:
        print("   ", c)
if not missing and not corrupt:
    print(">> All downloaded packages verified")
PY
}

extract() {
    echo ">> Extracting into $PREFIX"
    # Ubuntu is a merged-usr distribution: /bin, /sbin, /lib are symlinks to
    # /usr/*. Some packages (e.g. base-files) ship those symlinks and others
    # ship files under ./bin/... — pre-create the merged-usr layout so neither
    # case conflicts during flat extraction.
    rm -rf "$PREFIX/bin" "$PREFIX/sbin" "$PREFIX/lib"
    mkdir -p "$PREFIX/usr/bin" "$PREFIX/usr/sbin" "$PREFIX/usr/lib"
    ln -sf usr/bin "$PREFIX/bin"
    ln -sf usr/sbin "$PREFIX/sbin"
    ln -sf usr/lib "$PREFIX/lib"
    for deb in "$APT_ROOT"/download/*.deb; do
        dpkg-deb -x "$deb" "$PREFIX" || echo "WARN: failed to extract $(basename "$deb")"
    done
    # Repair .so -> .so.N symlinks whose targets live in other packages
    find "$PREFIX" -type l \( -name '*.so' -o -name '*.so.*' \) | while read -r l; do
        target="$(readlink "$l" || true)"
        if [ -n "$target" ] && [ ! -e "$(dirname "$l")/$target" ]; then
            ln -sf "$(basename "$target")" "$l" || true
        fi
    done
    # Prune broken non-lib symlinks (e.g. /bin/ruby from the ruby metapackage
    # that only makes sense under a merged /usr) so they cannot shadow the
    # real binaries in $PREFIX/usr/bin during cmake/prefix searches.
    find "$PREFIX/bin" -maxdepth 1 -type l ! -exec test -e {} \; -delete 2>/dev/null || true

    # Ubuntu 26.04's cmake 4.2.3 is incompatible with WPE WebKit's build
    # (FindRuby's legacy path uses string(REGEX_REPLACE ...) which cmake 4.x
    # removed). Install the cmake 3.31 prebuilt under usr/local and drop the
    # distro cmake binaries so PATH resolves to the working one.
    if [ ! -x "$PREFIX/usr/local/bin/cmake" ]; then
        curl -fsSL -o "$APT_ROOT/download/cmake-3.31.tar.gz" \
            https://github.com/Kitware/CMake/releases/download/v3.31.7/cmake-3.31.7-linux-x86_64.tar.gz
        mkdir -p "$PREFIX/usr/local"
        tar xzf "$APT_ROOT/download/cmake-3.31.tar.gz" -C "$PREFIX/usr/local" --strip-components=1
    fi
    rm -f "$PREFIX/usr/bin/cmake" "$PREFIX/usr/bin/cpack" "$PREFIX/usr/bin/ctest"

    cat > "$PREFIX/env.sh" <<EOF
export TARVA_TOOLCHAIN="$PREFIX"
export PATH="$PREFIX/usr/local/bin:$PREFIX/usr/bin:$PREFIX/bin:\$PATH"
export PKG_CONFIG_PATH="$PREFIX/usr/lib/$TRIPLET/pkgconfig:$PREFIX/usr/lib/pkgconfig:$PREFIX/usr/share/pkgconfig:\${PKG_CONFIG_PATH:-}"
export LD_LIBRARY_PATH="$PREFIX/usr/lib/$TRIPLET:$PREFIX/usr/lib:\${LD_LIBRARY_PATH:-}"
export CPATH="$PREFIX/usr/include:\${CPATH:-}"
export LIBRARY_PATH="$PREFIX/usr/lib/$TRIPLET:$PREFIX/usr/lib:\${LIBRARY_PATH:-}"
export CMAKE_PREFIX_PATH="$PREFIX:\${CMAKE_PREFIX_PATH:-}"
export CMAKE_INCLUDE_PATH="$PREFIX/usr/include"
export CMAKE_LIBRARY_PATH="$PREFIX/usr/lib/$TRIPLET:$PREFIX/usr/lib"
export PKG_CONFIG="$PREFIX/usr/bin/pkgconf"
export PKG_CONFIG_LIBDIR="$PREFIX/usr/lib/$TRIPLET/pkgconfig:$PREFIX/usr/lib/pkgconfig:$PREFIX/usr/share/pkgconfig"
# Ruby stdlib lives under the prefix (compiled-in absolute paths point at /usr);
export RUBYLIB="$PREFIX/usr/lib/ruby/3.3.0:$PREFIX/usr/lib/$TRIPLET/ruby/3.3.0"
EOF
    echo ">> Done. Toolchain staged in $PREFIX"
    echo ">> Next: source $PREFIX/env.sh"
}

case "$CMD" in
    deps)    deps ;;
    fetch)   fetch ;;
    verify)  verify ;;
    extract) extract ;;
    all)     deps && fetch && verify && extract ;;
    *) echo "usage: $0 {deps|fetch|verify|extract|all}"; exit 1 ;;
esac
