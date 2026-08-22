#!/bin/bash
# Download ICU 74 runtime libs needed by WPE WebKit 2.52.5
# The WPE artifact was built on Ubuntu 24.04 (ICU 74) but this host has ICU 78.
# This script downloads and extracts ICU 74 to /usr/local/icu74/ without sudo.

set -e

DEST="/usr/local/icu74"
mkdir -p "$DEST"
cd /tmp

echo "Downloading ICU 74 packages..."
# ICU 74 from Ubuntu 24.04 noble
for pkg in libicu74 libicu-dev; do
    url="http://archive.ubuntu.com/ubuntu/pool/main/i/icu/${pkg}_74.2-3ubuntu3_amd64.deb"
    file="$(basename $url)"
    if [ ! -f "$file" ]; then
        curl -sLO "$url" || true
    fi
    if [ -f "$file" ]; then
        echo "Extracting $file..."
        dpkg-deb -x "$file" "$DEST"
    else
        echo "WARN: Could not download $file"
    fi
done

# Also get libxml2 2.9 if needed
for pkg in libxml2 libxml2-dev; do
    url="http://archive.ubuntu.com/ubuntu/pool/main/libx/libxml2/${pkg}_2.9.14+dfsg-1.3ubuntu3_amd64.deb"
    file="$(basename $url)"
    if [ ! -f "$file" ]; then
        curl -sLO "$url" || true
    fi
    if [ -f "$file" ]; then
        echo "Extracting $file..."
        dpkg-deb -x "$file" "$DEST"
    fi
done

echo ""
echo "=== Installed ICU 74 libs ==="
find "$DEST" -name "libicu*.so*" -o -name "libxml2*" | head -20
echo ""
echo "=== Symlinks for versioned libs ==="
ls -la "$DEST"/usr/lib/x86_64-linux-gnu/libicu*.so* 2>/dev/null | head -10

echo ""
echo "To use, set LD_LIBRARY_PATH:"
echo "  export LD_LIBRARY_PATH=$DEST/usr/lib/x86_64-linux-gnu:\${LD_LIBRARY_PATH:-}"
