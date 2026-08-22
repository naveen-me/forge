#!/bin/bash
# Re-download the fixed WPE artifact after CI run with libexec fix
# Run this after CI run #32568990874 (or latest) completes successfully
#
# Usage:
#   export GITHUB_TOKEN=ghp_your_token_here
#   bash scripts/reinstall_wpe_artifact.sh

set -e

REPO="naveen-me/forge"
DEST_DIR="/tmp/wpe-artifact"

if [ -z "$GITHUB_TOKEN" ]; then
    echo "ERROR: Set GITHUB_TOKEN first"
    exit 1
fi

# Find the latest successful run
echo "Finding latest successful run..."
RUN_ID=$(curl -sL \
    -H "Authorization: token ${GITHUB_TOKEN}" \
    "https://api.github.com/repos/${REPO}/actions/runs?status=success&per_page=1" | \
    python3 -c "import json,sys; d=json.load(sys.stdin); print(d['workflow_runs'][0]['id'])")
echo "Latest successful run: #${RUN_ID}"

# Find the wpewebkit artifact
ARTIFACT_ID=$(curl -sL \
    -H "Authorization: token ${GITHUB_TOKEN}" \
    "https://api.github.com/repos/${REPO}/actions/runs/${RUN_ID}/artifacts" | \
    python3 -c "import json,sys; d=json.load(sys.stdin); arts=[a for a in d['artifacts'] if 'wpewebkit' in a['name']]; print(arts[0]['id']) if arts else print('NONE')")
echo "Artifact ID: ${ARTIFACT_ID}"

if [ "$ARTIFACT_ID" = "NONE" ]; then
    echo "ERROR: No wpewebkit artifact found in latest run"
    exit 1
fi

# Download
mkdir -p "$DEST_DIR"
echo "Downloading artifact..."
curl -sL \
    -H "Authorization: token ${GITHUB_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -o "${DEST_DIR}/wpewebkit.zip" \
    "https://api.github.com/repos/${REPO}/actions/artifacts/${ARTIFACT_ID}/zip"

if ! file "${DEST_DIR}/wpewebkit.zip" | grep -q "Zip"; then
    echo "ERROR: Download failed"
    cat "${DEST_DIR}/wpewebkit.zip"
    exit 1
fi

# Extract
cd "$DEST_DIR"
unzip -o wpewebkit.zip

# Verify libexec exists
echo ""
echo "=== Checking for libexec binaries ==="
if tar tzf wpewebkit-2.52.5-ubuntu24.04-x86_64.tar.gz | grep -q "libexec"; then
    echo "PASS: libexec/ found in tarball"
    tar tzf wpewebkit-2.52.5-ubuntu24.04-x86_64.tar.gz | grep libexec
else
    echo "FAIL: libexec/ NOT found in tarball — artifact is still incomplete"
    echo "Contents:"
    tar tzf wpewebkit-2.52.5-ubuntu24.04-x86_64.tar.gz
    exit 1
fi

# Install
echo ""
echo "=== Installing ==="
# Remove old WPE files first
rm -f /usr/local/lib/libWPEWebKit-2.0.so*
rm -rf /usr/local/include/wpe-webkit-2.0
rm -f /usr/local/pkgconfig/wpe-*.pc
rm -rf /usr/local/libexec/wpe-webkit-2.0
rm -rf /usr/local/share/wpe-webkit-2.0

tar xzf wpewebkit-2.52.5-ubuntu24.04-x86_64.tar.gz -C /usr/local/
ldconfig 2>/dev/null || true

echo ""
echo "=== Verification ==="
ls -lh /usr/local/lib/libWPEWebKit-2.0.so*
ls -lh /usr/local/libexec/wpe-webkit-2.0/
pkg-config --modversion wpe-webkit-2.0 wpe-platform-2.0 2>/dev/null || \
    PKG_CONFIG_PATH=/usr/local/pkgconfig pkg-config --modversion wpe-webkit-2.0 wpe-platform-2.0

echo ""
echo "Done. Run the test:"
echo "  export LD_LIBRARY_PATH=/tmp/icu74/extracted/usr/lib/x86_64-linux-gnu:\$LD_LIBRARY_PATH"
echo "  export LIBGL_ALWAYS_SOFTWARE=1"
echo "  ./build/test_c1_4"
