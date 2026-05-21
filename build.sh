#!/usr/bin/env bash
# ShelfMod: build JARs for all loaders.
#
# Usage:
#   ./build.sh              # build all loaders that have a compatible JDK
#   ./build.sh forge        # build only forge-1.20.1
#   ./build.sh neoforge     # build only neoforge-1.21.4
#   ./build.sh fabric       # build only fabric-1.21.4
#   ./build.sh clean        # clean all
#
# Built JARs land in `dist/` at the repo root.
#
# JDK requirements (override with JAVA17_HOME / JAVA21_HOME if needed):
#   - forge-1.20.1     : Java 17
#   - neoforge-1.21.4  : Java 21
#   - fabric-1.21.4    : Java 21
#
# Works on macOS bash 3.2+ (no associative arrays).

set -eo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
DIST="$ROOT/dist"
mkdir -p "$DIST"

TARGETS="forge neoforge fabric"

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

locate_jdk() {
  # $1 = version, $2 = env var name
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
  # Keg-only Homebrew JDKs aren't exposed via java_home; probe their canonical paths.
  for c in \
    "/opt/homebrew/opt/openjdk@${v}/libexec/openjdk.jdk/Contents/Home" \
    "/usr/local/opt/openjdk@${v}/libexec/openjdk.jdk/Contents/Home"; do
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

  echo "[$target] gradle $* (JDK $req at $jdk)"
  (
    cd "$ROOT/$dir"
    JAVA_HOME="$jdk" PATH="$jdk/bin:$PATH" ./gradlew --no-daemon "$@"
  )
}

clean_all() {
  for t in $TARGETS; do run_gradle "$t" clean || true; done
  rm -rf "$DIST"
  mkdir -p "$DIST"
}

build_one() {
  local target="$1"
  local dir="$(dir_for "$target")"
  run_gradle "$target" build || return $?
  shopt -s nullglob
  for jar in "$ROOT/$dir/build/libs/"*.jar; do
    case "$jar" in
      *-sources.jar|*-dev.jar|*-dev-shadow.jar) continue ;;
    esac
    # Prefix the jar with the loader for clarity since archive names overlap
    base="$(basename "$jar")"
    cp -f "$jar" "$DIST/${target}-${base}"
    echo "  -> dist/${target}-${base}"
  done
}

case "${1:-all}" in
  clean)    clean_all ;;
  forge)    build_one forge ;;
  neoforge) build_one neoforge ;;
  fabric)   build_one fabric ;;
  all|"")
    fails=""
    for t in $TARGETS; do
      if ! build_one "$t"; then
        fails="$fails $t"
      fi
    done
    echo
    echo "=== build summary ==="
    ls -la "$DIST"/*.jar 2>/dev/null || echo "(no jars produced)"
    if [ -n "$fails" ]; then
      echo "failed/skipped:$fails"
      exit 1
    fi
    ;;
  *)
    echo "usage: $0 [forge|neoforge|fabric|all|clean]"; exit 2 ;;
esac
