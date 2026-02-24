#!/bin/bash
REPORT_DIR="$1"
PORT=9090

if [ ! -f "$REPORT_DIR/index.html" ]; then
  echo "[Allure] Report not found at: $REPORT_DIR"
  exit 0
fi

# Kill any previous report server on this port
lsof -ti :$PORT 2>/dev/null | xargs kill -9 2>/dev/null || true

# Detect OS for open command
open_browser() {
  local url="$1"
  case "$(uname -s)" in
    Darwin)  open "$url" ;;
    Linux)   xdg-open "$url" 2>/dev/null || sensible-browser "$url" 2>/dev/null ;;
    MINGW*|MSYS*|CYGWIN*) start "$url" ;;
    *)       echo "[Allure] Could not detect OS. Open manually: $url" ;;
  esac
}

# Try python3, then python, then fall back to direct open
if command -v python3 &>/dev/null; then
  python3 -m http.server $PORT --directory "$REPORT_DIR" >/dev/null 2>&1 &
elif command -v python &>/dev/null; then
  (cd "$REPORT_DIR" && python -m http.server $PORT) >/dev/null 2>&1 &
else
  echo "[Allure] Python not found. Opening report directly (some widgets may not load)."
  open_browser "file://$REPORT_DIR/index.html"
  exit 0
fi

SERVER_PID=$!
sleep 1

open_browser "http://localhost:$PORT"

echo ""
echo "════════════════════════════════════════════════════════"
echo "  Allure Report: http://localhost:$PORT"
echo "  Stop server:   kill $SERVER_PID"
echo "════════════════════════════════════════════════════════"
echo ""
