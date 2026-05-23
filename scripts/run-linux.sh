#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

find_java() {
    if [ -x "./tools/jdk8/bin/java" ]; then
        printf '%s\n' "./tools/jdk8/bin/java"
        return 0
    fi
    for candidate in ./tools/jdk8/*/bin/java; do
        if [ -x "$candidate" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        printf '%s\n' "$JAVA_HOME/bin/java"
        return 0
    fi
    if command -v java >/dev/null 2>&1; then
        command -v java
        return 0
    fi
    return 1
}

JAVA="$(find_java || true)"

if [ ! -f MCLauncherRevival.jar ]; then
    echo "MCLauncherRevival.jar was not found; building from source."
    sh ./scripts/build-linux.sh
    JAVA="$(find_java || true)"
fi

if [ -z "$JAVA" ]; then
    echo "Java runtime not found."
    echo "Install Java 8, set JAVA_HOME, or extract a compatible JDK/JRE at tools/jdk8."
    exit 1
fi

java_major() {
    first_line="$("$1" -version 2>&1 | sed -n '1p')"
    major="$(printf '%s\n' "$first_line" | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p')"
    if [ "$major" = "1" ]; then
        major="$(printf '%s\n' "$first_line" | sed -n 's/.*version "1\.\([0-9][0-9]*\).*/\1/p')"
    fi
    printf '%s\n' "$major"
}

echo "Java runtime found: $JAVA"
JAVA_MAJOR="$(java_major "$JAVA")"
if [ -n "$JAVA_MAJOR" ] && [ "$JAVA_MAJOR" -gt 8 ] 2>/dev/null; then
    echo "Warning: Java $JAVA_MAJOR detected. Java 8 is recommended for old Beta/Alpha Minecraft clients."
fi

exec "$JAVA" ${MCLAUNCHER_JAVA_OPTS:-} -jar MCLauncherRevival.jar "$@"
