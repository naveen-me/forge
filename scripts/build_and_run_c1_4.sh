#!/bin/bash
# Build and run the C1.4 WPE Headless Integration Test
#
# Prerequisites:
#   1. WPE WebKit 2.52.5 installed (run scripts/install_wpe_artifact.sh)
#   2. ICU 74 libs extracted (run scripts/download_icu74.sh or extract manually)
#
# Usage:
#   bash scripts/build_and_run_c1_4.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
TEST_SRC="$PROJECT_DIR/tests/test_c1_4_wpe_headless.cpp"
TEST_BIN="/tmp/test_c1_4"
ICU_DIR="/tmp/icu74/extracted/usr/lib/x86_64-linux-gnu"

# Check prerequisites
if [ ! -f "$TEST_SRC" ]; then
    echo "ERROR: Test source not found: $TEST_SRC"
    exit 1
fi

export PKG_CONFIG_PATH="/usr/local/pkgconfig:${PKG_CONFIG_PATH:-}"

if ! pkg-config --exists wpe-webkit-2.0 wpe-platform-2.0 2>/dev/null; then
    echo "ERROR: WPE pkg-config modules not found"
    echo "  PKG_CONFIG_PATH=$PKG_CONFIG_PATH"
    exit 1
fi

if [ ! -d "$ICU_DIR" ]; then
    echo "ERROR: ICU 74 libs not found at $ICU_DIR"
    echo "  Run: bash scripts/download_icu74.sh"
    exit 1
fi

echo "=== Building C1.4 test ==="
g++ -std=c++20 -o "$TEST_BIN" "$TEST_SRC" \
    $(pkg-config --cflags --libs wpe-webkit-2.0 wpe-platform-2.0) \
    -L"$ICU_DIR" \
    -Wl,-rpath,"$ICU_DIR" \
    2>&1

echo "=== Running C1.4 test ==="
echo ""
export LD_LIBRARY_PATH="$ICU_DIR:${LD_LIBRARY_PATH:-}"
export LIBGL_ALWAYS_SOFTWARE=1
export WPE_DISPLAY=wpe-display-headless
export WEBKIT_INJECTED_BUNDLE_PATH="/usr/local/lib/"
export WEBKIT_DISABLE_SANDBOX_THIS_IS_DANGEROUS=1

"$TEST_BIN" 2>&1
EXIT_CODE=$?

echo ""
echo "=== Exit code: $EXIT_CODE ==="
exit $EXIT_CODE
