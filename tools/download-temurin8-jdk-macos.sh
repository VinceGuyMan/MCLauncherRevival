#!/usr/bin/env sh
set -eu

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DESTINATION="${1:-tools/jdk8}"
TARBALL="${MCLR_TEMURIN8_TARBALL:-tools/temurin8-jdk-mac-x64.tar.gz}"
API_URL="https://api.adoptium.net/v3/binary/latest/8/ga/mac/x64/jdk/hotspot/normal/eclipse"

find_jdk_root() {
    root="$1"
    if [ -x "$root/Contents/Home/bin/java" ] \
        && [ -x "$root/Contents/Home/bin/javac" ] \
        && [ -x "$root/Contents/Home/bin/jar" ]; then
        printf '%s\n' "$root"
        return 0
    fi
    if [ -x "$root/bin/java" ] && [ -x "$root/bin/javac" ] && [ -x "$root/bin/jar" ]; then
        printf '%s\n' "$root"
        return 0
    fi
    return 1
}

print_java_version() {
    root="$1"
    if [ -x "$root/Contents/Home/bin/java" ]; then
        "$root/Contents/Home/bin/java" -version
    else
        "$root/bin/java" -version
    fi
}

if [ "$(uname -s)" != "Darwin" ]; then
    echo "This helper is for macOS only."
    exit 1
fi

if existing="$(find_jdk_root "$DESTINATION" 2>/dev/null || true)" && [ -n "$existing" ]; then
    echo "Found existing Temurin/OpenJDK 8 at $existing"
    print_java_version "$existing"
    exit 0
fi

if [ "$(uname -m)" = "arm64" ]; then
    if ! arch -x86_64 /usr/bin/true >/dev/null 2>&1; then
        echo "Temurin 8 for macOS is available as x64. Apple Silicon Macs need Rosetta to run it."
        echo "Install Rosetta with: softwareupdate --install-rosetta"
        exit 1
    fi
    echo "Apple Silicon detected; downloading x64 Temurin 8 for use through Rosetta."
fi

mkdir -p tools
if [ ! -f "$TARBALL" ]; then
    echo "Downloading Temurin 8 from Adoptium"
    echo "$API_URL"
    curl -L -o "$TARBALL" "$API_URL"
else
    echo "Using existing $TARBALL"
fi

rm -rf "$DESTINATION"
mkdir -p "$DESTINATION"
tar -xzf "$TARBALL" -C "$DESTINATION" --strip-components=1

jdk_root="$(find_jdk_root "$DESTINATION" 2>/dev/null || true)"
if [ -z "$jdk_root" ]; then
    echo "The JDK archive was extracted, but java, javac, and jar were not found."
    exit 1
fi

echo "Java JDK 8 ready at $jdk_root"
print_java_version "$jdk_root"
