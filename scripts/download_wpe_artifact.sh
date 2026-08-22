#!/bin/bash
# Download the WPE WebKit 2.52.5 prebuilt artifact from CI run #32551896254
# Usage: GITHUB_TOKEN=ghp_xxxxx bash scripts/download_wpe_artifact.sh

set -e

REPO="naveen-me/forge"
ARTIFACT_ID="9472006562"
ARTIFACT_NAME="wpewebkit-2.52.5-ubuntu24.04-x86_64"
DEST_DIR="/tmp/wpe-artifact"

if [ -z "$GITHUB_TOKEN" ]; then
    echo "ERROR: Set GITHUB_TOKEN first"
    echo "  export GITHUB_TOKEN=ghp_xxxxxxxxxxxx"
    exit 1
fi

mkdir -p "$DEST_DIR"

echo "Downloading artifact ${ARTIFACT_NAME} (id=${ARTIFACT_ID})..."
curl -sL \
    -H "Authorization: token ${GITHUB_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -o "${DEST_DIR}/${ARTIFACT_NAME}.zip" \
    "https://api.github.com/repos/${REPO}/actions/artifacts/${ARTIFACT_ID}/zip"

if file "${DEST_DIR}/${ARTIFACT_NAME}.zip" | grep -q "Zip"; then
    echo "Download OK. Extracting..."
    cd "$DEST_DIR"
    unzip -o "${ARTIFACT_NAME}.zip"
    echo ""
    echo "=== Extracted contents ==="
    find . -type f -exec ls -lh {} \;
    echo ""
    echo "=== SHA-256 ==="
    sha256sum "${ARTIFACT_NAME}.tar.gz" 2>/dev/null || sha256sum ./*.tar.gz 2>/dev/null || echo "No tar.gz found"
    echo ""
    echo "Next step: sudo tar xzf ${DEST_DIR}/*.tar.gz -C /usr/local/"
else
    echo "ERROR: Download failed. Response:"
    cat "${DEST_DIR}/${ARTIFACT_NAME}.zip"
    exit 1
fi
