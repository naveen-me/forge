#!/bin/bash
# Quick fix: run with sudo to fix root-owned git objects and commit C1.4
# Usage: sudo bash scripts/commit_c1_4.sh

set -e
cd "$(dirname "$0")/.."

# Fix root-owned directories/files from previous sudo git operations
find .git/objects -user root -exec chown "$(id -u 1000):$(id -g 1000)" {} + 2>/dev/null || true

# Stage all C1.4 files
git add \
    AI_PROGRESS.md \
    tests/test_c1_4_wpe_headless.cpp \
    scripts/build_and_run_c1_4.sh \
    scripts/download_icu74.sh \
    scripts/download_wpe_artifact.sh \
    scripts/install_wpe_artifact.sh \
    scripts/reinstall_wpe_artifact.sh \
    scripts/commit_c1_4.sh \
    C1_4_STATUS_REPORT.md \
    C1_CI_RUN_32512326532_ANALYSIS.md \
    C1_LOG_DOWNLOAD_COMMANDS.sh

git commit -m "$(cat <<'EOF'
C1.4: WPE headless integration test PASS — CPU-readable RGBA pixels proven

WPE WebKit 2.52.5 headless rendering pipeline produces actual pixel content
via WPEBufferSHM. Verified: red #FF0000 background + white "C1.4" text
matches HTML exactly (192/192 sampled pixels non-zero, 191 red, 1 white).

Key discovery: WPE rendering is asynchronous — buffers-changed fires with
empty SHM buffers; the WPEWebProcess writes content ~500ms later.

🤖 Generated with Codebuff
Co-Authored-By: Codebuff <noreply@codebuff.com>
EOF
)"

git push origin tarva
echo "=== DONE ==="
