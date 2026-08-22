#!/bin/bash
# ============================================================
# Download and install WPE WebKit 2.52.5 prebuilt artifact
# from CI run #32551896254 (commit ac03f390)
#
# Usage:
#   export GITHUB_TOKEN=ghp_your_token_here
#   bash scripts/install_wpe_artifact.sh
#
# Expected SHA-256: 45ac2ed08d345db72d6b35d45d9da0775451cf5fe6d0beeb0102439d7ef807b4
# ============================================================

set -e

REPO="naveen-me/forge"
ARTIFACT_ID="9472006562"
ARTIFACT_NAME="wpewebkit-2.52.5-ubuntu24.04-x86_64"
DEST_DIR="/tmp/wpe-artifact"

# --- Step 1: Check token ---
if [ -z "$GITHUB_TOKEN" ]; then
    echo "ERROR: Set GITHUB_TOKEN first"
    echo "  export GITHUB_TOKEN=ghp_xxxxxxxxxxxx"
    exit 1
fi

# --- Step 2: Download ---
mkdir -p "$DEST_DIR"
echo "Downloading artifact ${ARTIFACT_NAME} (id=${ARTIFACT_ID})..."
curl -sL \
    -H "Authorization: token ${GITHUB_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -o "${DEST_DIR}/${ARTIFACT_NAME}.zip" \
    "https://api.github.com/repos/${REPO}/actions/artifacts/${ARTIFACT_ID}/zip"

if ! file "${DEST_DIR}/${ARTIFACT_NAME}.zip" | grep -q "Zip"; then
    echo "ERROR: Download failed. Response:"
    cat "${DEST_DIR}/${ARTIFACT_NAME}.zip"
    exit 1
fi
echo "Download OK."

# --- Step 3: Extract ---
cd "$DEST_DIR"
unzip -o "${ARTIFACT_NAME}.zip"
echo ""
echo "=== Extracted contents ==="
find . -type f -exec ls -lh {} \;

# --- Step 4: SHA-256 check ---
echo ""
echo "=== SHA-256 checksum ==="
sha256sum "${ARTIFACT_NAME}.tar.gz" 2>/dev/null || sha256sum ./*.tar.gz 2>/dev/null
echo "Expected: 45ac2ed08d345db72d6b35d45d9da0775451cf5fe6d0beeb0102439d7ef807b4"

# --- Step 5: Install ---
echo ""
echo "=== Installing to /usr/local/ ==="
sudo tar xzf "${DEST_DIR}/${ARTIFACT_NAME}.tar.gz" -C /usr/local/
sudo ldconfig

# --- Step 6: Verify ---
echo ""
echo "=== Verification ==="
echo "pkg-config:"
pkg-config --modversion wpe-webkit-2.0 wpe-platform-2.0 || true
echo ""
echo "Shared libraries:"
ls -lh /usr/local/lib/libWPEWebKit-2.0.so* 2>/dev/null || echo "NOT FOUND at /usr/local/lib/"
echo ""
echo "Headers:"
ls /usr/local/include/wpe-webkit-2.0/wpe/ 2>/dev/null | head -5
echo ""
echo "Done. WPE WebKit 2.52.5 installed."
