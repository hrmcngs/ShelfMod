#!/usr/bin/env bash
# ShelfMod build script (macOS / Linux).
#
# Usage:
#   ./build.sh [TARGETS...] [--offline|-o] [--verbose|-v] [--clean|-c]
#
# TARGETS: any combination of  forge  neoforge  fabric  all  (default: all)
# Flags:
#   -o, --offline    Pass --offline to gradle (no network — requires prior online build)
#   -v, --verbose    Show full gradle output (otherwise a single-line progress bar)
#   -c, --clean      Run `clean` before building
#
# Built JARs land in `dist/` at the repo root.
#
# JDK requirements (override with JAVA17_HOME / JAVA21_HOME if needed):
#   forge-1.20.1     : Java 17
#   neoforge-1.21.4  : Java 21
#   fabric-1.21.4    : Java 21
#
# Works on macOS bash 3.2+ (no associative arrays, no gawk extensions).

set -eo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
DIST="$ROOT/dist"
mkdir -p "$DIST"

TARGETS_ALL="forge neoforge fabric"

# --- Argument parsing -------------------------------------------------------
VERBOSE=0
OFFLINE=0
DO_CLEAN=0
SELECTED=""

for arg in "$@"; do
  case "$arg" in
    -o|--offline) OFFLINE=1 ;;
    -v|--verbose) VERBOSE=1 ;;
    -c|--clean)   DO_CLEAN=1 ;;
    -h|--help)
      sed -n '2,20p' "$0"; exit 0 ;;
    all)          SELECTED="$TARGETS_ALL" ;;
    forge|neoforge|fabric) SELECTED="$SELECTED $arg" ;;
    clean)        SELECTED="clean" ;;
    -*)           echo "unknown flag: $arg"; exit 2 ;;
    *)            echo "unknown target: $arg"; exit 2 ;;
  esac
done
[ -z "$SELECTED" ] && SELECTED="$TARGETS_ALL"

# --- Per-loader metadata ----------------------------------------------------
dir_for() {
  case "$1" in
    forge)    echo "forge-1.20.1" ;;
    neoforge) echo "neoforge-1.21.4" ;;
    fabric)   echo "fabric-1.21.4" ;;
  esac
}

req_jdk_for() {
  case "$1" in
    forge)    echo "17" ;;
    neoforge|fabric) echo "21" ;;
  esac
}

# Estimated total of progress events (gradle tasks + NFRT sub-steps).
# Used purely to drive the % bar — overshoots are clamped at 100%.
total_steps_for() {
  case "$1" in
    forge)    echo 12 ;;
    neoforge) echo 35 ;;
    fabric)   echo 14 ;;
  esac
}

# --- JDK discovery -----------------------------------------------------------
locate_jdk() {
  local v="$1" var="$2" home c
  eval "home=\${$var-}"
  if [ -n "$home" ] && [ -x "$home/bin/java" ]; then
    echo "$home"; return 0
  fi
  if [ -x /usr/libexec/java_home ]; then
    # -F = strict; without it macOS falls back to ANY JDK and lies about the version.
    home="$(/usr/libexec/java_home -F -v "$v" 2>/dev/null || true)"
    [ -n "$home" ] && { echo "$home"; return 0; }
  fi
  for c in \
    "/opt/homebrew/opt/openjdk@${v}/libexec/openjdk.jdk/Contents/Home" \
    "/usr/local/opt/openjdk@${v}/libexec/openjdk.jdk/Contents/Home" \
    "/usr/lib/jvm/java-${v}-openjdk-amd64" \
    "/usr/lib/jvm/java-${v}-openjdk"; do
    if [ -x "$c/bin/java" ]; then
      echo "$c"; return 0
    fi
  done
  return 1
}

jdk_for() {
  case "$1" in
    forge)            locate_jdk 17 JAVA17_HOME ;;
    neoforge|fabric)  locate_jdk 21 JAVA21_HOME ;;
  esac
}

# --- Progress bar (BSD awk compatible) --------------------------------------
# Reads gradle stdout/stderr, prints a single-line bar that updates on each
# `> Task :foo` or NFRT `Started working on bar` event. Errors are passed
# through to the user.
progress_filter() {
  local target="$1" total="$2"

  awk -v target="$target" -v total="$total" '
    BEGIN {
      count = 0;
      tty_width = 30;
      esc_clear = "\033[K";
      in_failure = 0;
    }
    function bar(c, t,    pct, filled, s, i) {
      pct = c * 100 / t;
      if (pct > 100) pct = 100;
      filled = int(pct * tty_width / 100);
      s = "[";
      for (i = 0; i < filled; i++) s = s "#";
      for (i = filled; i < tty_width; i++) s = s "-";
      return s "] " sprintf("%3d", pct) "%";
    }
    function strip_ansi(s) {
      gsub(/\033\[[0-9;]*[A-Za-z]/, "", s);
      return s;
    }
    function paint(label) {
      printf "\r[%s] %s %s%s", target, bar(count, total), label, esc_clear;
      fflush();
    }
    /^> Task :/ {
      skipped = ($0 ~ / UP-TO-DATE| NO-SOURCE| SKIPPED| FROM-CACHE/);
      if (!skipped) count++;
      label = $0;
      sub(/^> Task :/, ":", label);
      sub(/ UP-TO-DATE| NO-SOURCE| SKIPPED| FROM-CACHE/, "", label);
      paint(label);
      next;
    }
    /Started working on/ {
      count++;
      s = strip_ansi($0);
      sub(/.*Started working on +/, "", s);
      paint("[NFRT] " s);
      next;
    }
    /Completed +.*in [0-9.]+s/ {
      s = strip_ansi($0);
      sub(/^[ ✓]*Completed +/, "", s);
      paint("[NFRT] done " s);
      next;
    }
    /BUILD SUCCESSFUL/ {
      count = total;
      printf "\r[%s] %s %s%s\n", target, bar(total, total), $0, esc_clear;
      fflush();
      next;
    }
    /^FAILURE: |BUILD FAILED|^> Task .* FAILED/ {
      printf "\r%s", esc_clear;
      print;
      in_failure = 1;
      next;
    }
    in_failure == 1 { print; next; }
    # Suppress everything else by default; uncomment next line to debug.
    # { print; }
  '
}

# --- Gradle invocation ------------------------------------------------------
run_gradle() {
  local target="$1"; shift
  local dir="$(dir_for "$target")"
  local req="$(req_jdk_for "$target")"
  local jdk
  jdk="$(jdk_for "$target" || true)"

  if [ -z "$jdk" ]; then
    echo "[$target] SKIP — JDK $req not found. Install (e.g. \`brew install openjdk@$req\`) or set JAVA${req}_HOME."
    return 2
  fi

  local args="--no-daemon"
  [ "$OFFLINE" = "1" ] && args="$args --offline"

  echo "[$target] gradle $* (JDK $req at $jdk)"
  if [ "$VERBOSE" = "1" ]; then
    (
      cd "$ROOT/$dir"
      JAVA_HOME="$jdk" PATH="$jdk/bin:$PATH" ./gradlew $args "$@"
    )
  else
    local total="$(total_steps_for "$target")"
    set +e
    (
      cd "$ROOT/$dir"
      JAVA_HOME="$jdk" PATH="$jdk/bin:$PATH" ./gradlew $args "$@" 2>&1
    ) | progress_filter "$target" "$total"
    local rc=${PIPESTATUS[0]}
    set -e
    return $rc
  fi
}

# --- Top-level actions ------------------------------------------------------
clean_all() {
  for t in $TARGETS_ALL; do run_gradle "$t" clean || true; done
  rm -rf "$DIST"
  mkdir -p "$DIST"
}

build_one() {
  local target="$1"
  local dir="$(dir_for "$target")"
  [ "$DO_CLEAN" = "1" ] && run_gradle "$target" clean || true
  run_gradle "$target" build || return $?
  shopt -s nullglob
  for jar in "$ROOT/$dir/build/libs/"*.jar; do
    case "$jar" in
      *-sources.jar|*-dev.jar|*-dev-shadow.jar) continue ;;
    esac
    base="$(basename "$jar")"
    cp -f "$jar" "$DIST/${target}-${base}"
    echo "  -> dist/${target}-${base}"
  done
}

if [ "$SELECTED" = "clean" ]; then
  clean_all
  exit 0
fi

fails=""
for t in $SELECTED; do
  if ! build_one "$t"; then
    fails="$fails $t"
  fi
done
echo
echo "=== build summary ==="
ls -lh "$DIST"/*.jar 2>/dev/null || echo "(no jars produced)"
if [ -n "$fails" ]; then
  echo "failed/skipped:$fails"
  exit 1
fi
