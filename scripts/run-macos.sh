#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

find_java() {
    if [ -x "./tools/jdk8/Contents/Home/bin/java" ]; then
        printf '%s\n' "./tools/jdk8/Contents/Home/bin/java"
        return 0
    fi
    if [ -x "./tools/jdk8/bin/java" ]; then
        printf '%s\n' "./tools/jdk8/bin/java"
        return 0
    fi
    for candidate in ./tools/jdk8/*/Contents/Home/bin/java ./tools/jdk8/*/bin/java; do
        if [ -x "$candidate" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        printf '%s\n' "$JAVA_HOME/bin/java"
        return 0
    fi
    if command -v /usr/libexec/java_home >/dev/null 2>&1; then
        java8_home="$(/usr/libexec/java_home -v 1.8 2>/dev/null || true)"
        if [ -n "$java8_home" ] && [ -x "$java8_home/bin/java" ]; then
            printf '%s\n' "$java8_home/bin/java"
            return 0
        fi
        java_home="$(/usr/libexec/java_home 2>/dev/null || true)"
        if [ -n "$java_home" ] && [ -x "$java_home/bin/java" ]; then
            printf '%s\n' "$java_home/bin/java"
            return 0
        fi
    fi
    if command -v java >/dev/null 2>&1; then
        command -v java
        return 0
    fi
    return 1
}

java_major() {
    first_line="$("$1" -version 2>&1 | sed -n '1p')"
    major="$(printf '%s\n' "$first_line" | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p')"
    if [ "$major" = "1" ]; then
        major="$(printf '%s\n' "$first_line" | sed -n 's/.*version "1\.\([0-9][0-9]*\).*/\1/p')"
    fi
    printf '%s\n' "$major"
}

JAVA="$(find_java || true)"

if [ ! -f MCLauncherRevival.jar ]; then
    echo "MCLauncherRevival.jar was not found."
    echo "For normal use, download the attached GitHub Releases ZIP instead of GitHub's source-code ZIP."
    echo "Building from source now; this requires a JDK."
    sh ./scripts/build-macos.sh
    JAVA="$(find_java || true)"
fi

if [ -z "$JAVA" ]; then
    echo "Java runtime not found."
    echo "Install Java 8, set JAVA_HOME, or extract a compatible JDK/JRE at tools/jdk8."
    exit 1
fi

echo "Java runtime found: $JAVA"
JAVA_MAJOR="$(java_major "$JAVA")"
if [ -n "$JAVA_MAJOR" ] && [ "$JAVA_MAJOR" -gt 8 ] 2>/dev/null; then
    echo "Warning: Java $JAVA_MAJOR detected. Java 8 is recommended for old Beta/Alpha Minecraft clients."
fi

MAC_FONT_PATH_OPT="-Dsun.java2d.fontpath=/System/Library/Fonts:/Library/Fonts"
exec "$JAVA" \
    -Dapple.awt.application.name=MCLauncherRevival \
    "$MAC_FONT_PATH_OPT" \
    -Xdock:name=MCLauncherRevival \
    ${MCLAUNCHER_JAVA_OPTS:-} \
    -jar MCLauncherRevival.jar \
    "$@"
