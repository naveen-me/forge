#!/bin/bash
# Commit C1.5 results. Run with sudo to fix root-owned git objects.
# Usage: sudo bash scripts/commit_c1_5.sh

set -e
cd "$(dirname "$0")/.."

# Fix root-owned directories/files from previous sudo git operations
echo "=== Fixing git permissions ==="
find .git/objects -user root -exec chown "$(id -u 1000):$(id -g 1000)" {} + 2>/dev/null || true

# Reset the index to a clean state
git read-tree HEAD

# Stage C1.5 files
echo "=== Staging ==="
git add \
    tests/test_c1_5_perf.cpp \
    AI_PROGRESS.md \
    C1_5_FINAL_REPORT.md

git diff --cached --stat

# Commit
echo "=== Committing ==="
git commit -m "$(cat <<'EOF'
C1.5: WPE 1080p30 performance gate — 30.71 FPS sustained

Benchmark: 1920x1080 rendering with 20 animated balls via requestAnimationFrame.
460 settled frames in 15s measurement window, 0% drops.

Results on i3-1005G1 @ 1.2GHz (Mesa llvmpipe, CPU-only):
  - 30.71 FPS average (≥30 target)
  - P50: 31.92ms, P95: 38.98ms
  - 0 dropped frames
  - 105.5 MB peak RSS, 1.9ms CPU per frame

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>
EOF
)"

# Push
echo "=== Pushing ==="
git push origin tarva
echo "=== DONE ==="
