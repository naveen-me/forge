#!/bin/bash
# ============================================================
# Commands to download and inspect logs from Run 32512326532
# Replace GITHUB_TOKEN with your actual token before running
# ============================================================

# --- SETUP ---
export GITHUB_TOKEN="YOUR_TOKEN_HERE"   # <-- PUT YOUR TOKEN HERE
REPO="naveen-me/forge"
RUN_ID="32512326532"
JOB_ID="96866084647"
ARTIFACT_ID="9457534151"

# --- 1. Download the cmake-diagnostics artifact ---
echo "=== Downloading cmake-diagnostics artifact ==="
mkdir -p /tmp/ci-logs
curl -sL -H "Authorization: token $GITHUB_TOKEN" \
  -o /tmp/ci-logs/cmake-diagnostics.zip \
  "https://api.github.com/repos/${REPO}/actions/artifacts/${ARTIFACT_ID}/zip"

cd /tmp/ci-logs
unzip -o cmake-diagnostics.zip
echo ""
echo "=== Files extracted ==="
ls -lh /tmp/ci-logs/
echo ""

# Show the cmake output log (the actual cmake configure output)
echo "============================================"
echo "=== /tmp/cmake-output.log (CMake configure) ==="
echo "============================================"
cat /tmp/ci-logs/tmp/cmake-output.log
echo ""

# --- 2. Download the full job logs ---
echo "============================================"
echo "=== Downloading full job logs ==="
echo "============================================"
curl -sL -H "Authorization: token $GITHUB_TOKEN" \
  -o /tmp/ci-logs/job-logs.zip \
  "https://api.github.com/repos/${REPO}/actions/jobs/${JOB_ID}/logs"

cd /tmp/ci-logs
unzip -o job-logs.zip
echo ""
echo "=== Log files extracted ==="
ls -lh /tmp/ci-logs/*.txt 2>/dev/null || ls -lh /tmp/ci-logs/
echo ""

# --- 3. Find the Install step failure ---
echo "============================================"
echo "=== Searching for install failure ==="
echo "============================================"
# The job log is one big text file. Search for the failing step.
grep -n -A 20 "Install and Verify" /tmp/ci-logs/Build\ WPE\ WebKit\ 2.52.5\ \(Minimal\ WPEPlatform\).txt 2>/dev/null \
  || grep -rn -A 20 "Install and Verify" /tmp/ci-logs/*.txt 2>/dev/null \
  || grep -rn -A 20 "ninja.*install" /tmp/ci-logs/*.txt 2>/dev/null
echo ""

# --- 4. Show the LAST 100 lines of the build log ---
echo "============================================"
echo "=== Last 100 lines of full job log ==="
echo "============================================"
find /tmp/ci-logs -name "*.txt" -exec sh -c 'echo "--- FILE: {} ---"; tail -100 {}' \;
echo ""

# --- 5. Check for specific errors ---
echo "============================================"
echo "=== Searching for error/failure/exit patterns ==="
echo "============================================"
grep -rn -i "error\|FAILED\|exit code\|no space\|disk\|cannot\|not found" /tmp/ci-logs/*.txt 2>/dev/null | tail -50
echo ""

echo "=== DONE ==="
echo "All logs are in /tmp/ci-logs/"
