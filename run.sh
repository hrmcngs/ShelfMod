#!/usr/bin/env bash
# ShelfMod: launch the in-dev Minecraft client (or server) with the mod loaded.
#
# Usage:
#   ./run.sh <loader> [client|server] [flags]
#   ./run.sh forge                          # client (default)
#   ./run.sh neoforge server                # dedicated server
#   ./run.sh forge --offline                # client, no network access
#   ./run.sh fabric server -o               # server, offline
#
# First invocation downloads Minecraft assets + decompiled sources — slow,
# and requires network. Subsequent runs cache everything in <loader>/run/ and
# ~/.gradle/caches/, so --offline works after one successful online launch.
#
# Flags:
#   -o, --offline    pass --offline to gradle (no network)
#   -v, --verbose    show full gradle output (verbose)
#   -h, --help       show this help
#
# JDK requirements match build.sh:
#   forge-1.20.1     : Java 17
#   neoforge-1.21.4  : Java 21
#   fabric-1.21.4    : Java 21

set -eo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"

target=""
mode="client"
OFFLINE=0
VERBOSE=0

for arg in "$@"; do
  case "$arg" in
    -o|--offline) OFFLINE=1 ;;
    -v|--verbose) VERBOSE=1 ;;
    -h|--help)    sed -n '2,22p' "$0"; exit 0 ;;
    client|server) mode="$arg" ;;
    forge|neoforge|fabric)
      if [ -n "$target" ]; then
        echo "loader already set to '$target' (got '$arg')"; exit 2
      fi
      target="$arg"
      ;;
    -*) echo "unknown flag: $arg"; exit 2 ;;
    *)  echo "unknown argument: $arg"; exit 2 ;;
  esac
done

if [ -z "$target" ]; then
  echo "usage: $0 <forge|neoforge|fabric> [client|server] [--offline] [--verbose]"
  exit 2
fi

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

gradle_args="--no-daemon"
[ "$OFFLINE" = "1" ] && gradle_args="$gradle_args --offline"
[ "$VERBOSE" = "1" ] && gradle_args="$gradle_args --info"

flags=""
[ "$OFFLINE" = "1" ] && flags="${flags:+$flags }offline"
[ "$VERBOSE" = "1" ] && flags="${flags:+$flags }verbose"
[ -n "$flags" ] && flags=" [$flags]"

echo "[$target/$mode]$flags launching with JDK $req at $jdk"
cd "$ROOT/$dir"
JAVA_HOME="$jdk" PATH="$jdk/bin:$PATH" exec ./gradlew $gradle_args "$task"
