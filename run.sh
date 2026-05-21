#!/usr/bin/env bash
# ShelfMod: launch the in-dev Minecraft client (or server) with the mod loaded.
#
# Usage:
#   ./run.sh <loader> [client|server]   loader = forge | neoforge | fabric
#   ./run.sh forge                       # client (default)
#   ./run.sh neoforge server             # dedicated server
#
# First invocation downloads Minecraft assets + decompiled sources — slow.
# Subsequent runs are fast (everything is cached in the loader's working dir).
#
# JDK requirements match build.sh:
#   forge-1.20.1     : Java 17
#   neoforge-1.21.4  : Java 21
#   fabric-1.21.4    : Java 21

set -eo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

target="${1-}"
mode="${2:-client}"

case "$target" in
  forge|neoforge|fabric) ;;
  ""|-h|--help)
    sed -n '2,16p' "$0"; exit 0 ;;
  *)
    echo "unknown loader: $target"; exit 2 ;;
esac

case "$mode" in
  client|server) ;;
  *) echo "mode must be client or server (got: $mode)"; exit 2 ;;
esac

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
  local v="$1" var="$2" home c
  eval "home=\${$var-}"
  if [ -n "$home" ] && [ -x "$home/bin/java" ]; then echo "$home"; return 0; fi
  if [ -x /usr/libexec/java_home ]; then
    home="$(/usr/libexec/java_home -F -v "$v" 2>/dev/null || true)"
    [ -n "$home" ] && { echo "$home"; return 0; }
  fi
  for c in \
    "/opt/homebrew/opt/openjdk@${v}/libexec/openjdk.jdk/Contents/Home" \
    "/usr/local/opt/openjdk@${v}/libexec/openjdk.jdk/Contents/Home" \
    "/usr/lib/jvm/java-${v}-openjdk-amd64" \
    "/usr/lib/jvm/java-${v}-openjdk"; do
    [ -x "$c/bin/java" ] && { echo "$c"; return 0; }
  done
  return 1
}

dir="$(dir_for "$target")"
req="$(req_jdk_for "$target")"
case "$target" in
  forge)            jdk="$(locate_jdk 17 JAVA17_HOME)" ;;
  neoforge|fabric)  jdk="$(locate_jdk 21 JAVA21_HOME)" ;;
esac

[ -z "${jdk:-}" ] && {
  echo "JDK $req not found. Install (e.g. brew install openjdk@$req) or set JAVA${req}_HOME."
  exit 1
}

task="runClient"
[ "$mode" = "server" ] && task="runServer"

echo "[$target/$mode] launching with JDK $req at $jdk"
cd "$ROOT/$dir"
JAVA_HOME="$jdk" PATH="$jdk/bin:$PATH" exec ./gradlew --no-daemon "$task"
