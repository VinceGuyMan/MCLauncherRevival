#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")"

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
    if command -v java >/dev/null 2>&1; then
        command -v java
        return 0
    fi
    return 1
}

JAVA="$(find_java || true)"

if [ ! -f MCLauncherRevival.jar ]; then
    echo "MCLauncherRevival.jar was not found; building from source."
    ./build-macos.sh
    JAVA="$(find_java || true)"
fi

if [ -z "$JAVA" ]; then
    echo "Java runtime not found."
    echo "Install Java 8, set JAVA_HOME, or extract a compatible JDK/JRE at tools/jdk8."
    exit 1
fi

echo "Java runtime found: $JAVA"
exec "$JAVA" ${MCLAUNCHER_JAVA_OPTS:-} -jar MCLauncherRevival.jar
