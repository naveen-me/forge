#!/bin/bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$PROJECT_DIR/build_migration"
mkdir -p "$BUILD_DIR"

export PKG_CONFIG_PATH=/usr/local/pkgconfig:/usr/local/lib/pkgconfig:/usr/lib/x86_64-linux-gnu/pkgconfig
export LIBGL_ALWAYS_SOFTWARE=1
export XDG_RUNTIME_DIR=/tmp/runtime-root
mkdir -p "$XDG_RUNTIME_DIR"

GCC_OPTS=(g++ -std=c++20 -O2 -Wall)
GCC_OPTS+=($(pkg-config --cflags gpac wpe-webkit-2.0))
GCC_OPTS+=(-I"$PROJECT_DIR/include")

GCC_LIBS=($(pkg-config --libs gpac wpe-webkit-2.0))
GCC_LIBS+=(-lpthread)

"$PROJECT_DIR/build_migration/test_wpe_production_renderer" 2>&1 || true
