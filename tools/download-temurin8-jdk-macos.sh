#!/usr/bin/env sh
set -eu

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

DESTINATION="${1:-tools/jdk8}"
TARBALL="${MCLR_TEMURIN8_TARBALL:-tools/temurin8-jdk-mac-x64.tar.gz}"
API_URL="https://api.adoptium.net/v3/binary/latest/8/ga/mac/x64/jdk/hotspot/normal/eclipse"
CHECKSUM_FILE="${TARBALL}.sha256.txt"
PART_FILE="${TARBALL}.part"
CHECKSUM_PART_FILE="${CHECKSUM_FILE}.part"

cleanup_downloads() {
    rm -f "$PART_FILE" "$CHECKSUM_PART_FILE"
}

trap cleanup_downloads EXIT HUP INT TERM

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

read_expected_checksum() {
    checksum_file="$1"
    checksum="$(sed -n 's/^\([0-9A-Fa-f][0-9A-Fa-f]*\).*$/\1/p' "$checksum_file" | sed -n '1p')"
    case "$checksum" in
        *[!0-9A-Fa-f]*|'') return 1 ;;
    esac
    if [ "${#checksum}" -ne 64 ]; then
        return 1
    fi
    printf '%s\n' "$checksum" | tr 'A-F' 'a-f'
}

verify_archive() {
    archive="$1"
    checksum_file="$2"
    expected="$(read_expected_checksum "$checksum_file")" || return 1
    actual="$(shasum -a 256 "$archive" | awk '{print $1}')"
    [ "$actual" = "$expected" ]
}

resolve_official_download() {
    download_url="$(curl --fail --silent --show-error --head "$API_URL" \
        | sed -n 's/^[Ll]ocation: *//p' | tr -d '\r' | sed -n '1p')"
    case "$download_url" in
        https://github.com/adoptium/temurin8-binaries/releases/download/*) ;;
        *)
            echo "Adoptium returned an unexpected JDK download location." >&2
            return 1
            ;;
    esac
    printf '%s\n' "$download_url"
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
if download_url="$(resolve_official_download 2>/dev/null)"; then
    echo "Fetching the official Temurin 8 SHA-256 checksum"
    curl --fail --location --proto '=https' --tlsv1.2 \
        -o "$CHECKSUM_PART_FILE" "${download_url}.sha256.txt"
    read_expected_checksum "$CHECKSUM_PART_FILE" >/dev/null || {
        echo "Adoptium returned an invalid SHA-256 checksum file."
        exit 1
    }
    mv -f "$CHECKSUM_PART_FILE" "$CHECKSUM_FILE"

    if [ -f "$TARBALL" ] && verify_archive "$TARBALL" "$CHECKSUM_FILE"; then
        echo "Using verified existing $TARBALL"
    else
        if [ -f "$TARBALL" ]; then
            echo "Cached JDK archive did not match the current official checksum; replacing it."
        fi
        echo "Downloading Temurin 8 from Adoptium"
        echo "$download_url"
        curl --fail --location --proto '=https' --tlsv1.2 -o "$PART_FILE" "$download_url"
        verify_archive "$PART_FILE" "$CHECKSUM_FILE" || {
            echo "Temurin 8 SHA-256 verification failed; the archive will not be extracted."
            exit 1
        }
        mv -f "$PART_FILE" "$TARBALL"
    fi
elif [ -f "$TARBALL" ] && [ -f "$CHECKSUM_FILE" ] \
    && verify_archive "$TARBALL" "$CHECKSUM_FILE"; then
    echo "Adoptium is unavailable; using $TARBALL with its previously verified checksum."
else
    echo "Could not retrieve Adoptium's official checksum, and no verified cached archive is available."
    exit 1
fi

verify_archive "$TARBALL" "$CHECKSUM_FILE" || {
    echo "Temurin 8 SHA-256 verification failed; the archive will not be extracted."
    exit 1
}
echo "Temurin 8 SHA-256 verified"

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
