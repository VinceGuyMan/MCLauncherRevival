#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

find_tool() {
    name="$1"
    for candidate in \
        "./tools/jdk8/bin/$name" \
        "./tools/jdk8/Contents/Home/bin/$name" \
        ./tools/jdk8/*/bin/"$name" \
        ./tools/jdk8/*/Contents/Home/bin/"$name"; do
        if [ -x "$candidate" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/$name" ]; then
        printf '%s\n' "$JAVA_HOME/bin/$name"
        return 0
    fi
    if command -v "$name" >/dev/null 2>&1; then
        command -v "$name"
        return 0
    fi
    return 1
}

if [ "$(uname -s)" = "Darwin" ]; then
    sh ./scripts/build-macos.sh
else
    sh ./scripts/build-linux.sh
fi

JAVAC="$(find_tool javac || true)"
JAVA="$(find_tool java || true)"
if [ -z "$JAVAC" ] || [ -z "$JAVA" ]; then
    echo "A JDK with javac and java is required to run launcher self-tests."
    exit 1
fi

COMPILE_TARGET_ARGS=""
mkdir -p build/test-toolcheck
printf '%s\n' 'class TestTargetCheck {}' > build/test-toolcheck/TestTargetCheck.java
if "$JAVAC" --release 7 -d build/test-toolcheck build/test-toolcheck/TestTargetCheck.java >/dev/null 2>&1; then
    COMPILE_TARGET_ARGS="--release 7"
elif "$JAVAC" -source 1.7 -target 1.7 -d build/test-toolcheck build/test-toolcheck/TestTargetCheck.java >/dev/null 2>&1; then
    COMPILE_TARGET_ARGS="-source 1.7 -target 1.7 -Xlint:-options"
else
    rm -rf build/test-toolcheck
    echo "This javac cannot compile Java 7-compatible tests."
    exit 1
fi
rm -rf build/test-toolcheck build/test-classes
mkdir -p build/test-classes
find tests -name '*.java' | sort > build/test-sources.txt
# shellcheck disable=SC2086
"$JAVAC" $COMPILE_TARGET_ARGS -encoding UTF-8 -cp build/classes -d build/test-classes @build/test-sources.txt
"$JAVA" -Djava.awt.headless=true -cp "build/classes:build/test-classes" net.minecraft.LauncherSelfTest
