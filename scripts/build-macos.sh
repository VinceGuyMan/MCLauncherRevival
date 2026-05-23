#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")/.."

find_tool() {
    name="$1"
    if [ -x "./tools/jdk8/Contents/Home/bin/$name" ]; then
        printf '%s\n' "./tools/jdk8/Contents/Home/bin/$name"
        return 0
    fi
    for candidate in ./tools/jdk8/*/Contents/Home/bin/"$name" ./tools/jdk8/*/bin/"$name"; do
        if [ -x "$candidate" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/$name" ]; then
        printf '%s\n' "$JAVA_HOME/bin/$name"
        return 0
    fi
    if command -v /usr/libexec/java_home >/dev/null 2>&1; then
        java8_home="$(/usr/libexec/java_home -v 1.8 2>/dev/null || true)"
        if [ -n "$java8_home" ] && [ -x "$java8_home/bin/$name" ]; then
            printf '%s\n' "$java8_home/bin/$name"
            return 0
        fi
        java_home="$(/usr/libexec/java_home 2>/dev/null || true)"
        if [ -n "$java_home" ] && [ -x "$java_home/bin/$name" ]; then
            printf '%s\n' "$java_home/bin/$name"
            return 0
        fi
    fi
    if command -v "$name" >/dev/null 2>&1; then
        command -v "$name"
        return 0
    fi
    return 1
}

JAVAC="$(find_tool javac || true)"
JAR="$(find_tool jar || true)"
JAVA="$(find_tool java || true)"

if [ -z "$JAVAC" ] || [ -z "$JAR" ]; then
    echo "Java JDK 8 is recommended to build MCLauncherRevival on macOS."
    echo "Install a JDK with javac and jar on PATH, set JAVA_HOME, or extract one at tools/jdk8."
    exit 1
fi

if [ -n "$JAVA" ]; then
    echo "Java runtime found: $JAVA"
fi
echo "Java compiler found: $JAVAC"

COMPILE_TARGET_ARGS=""
mkdir -p build/toolcheck
printf '%s\n' 'class TargetCheck {}' > build/toolcheck/TargetCheck.java
if "$JAVAC" --release 7 -d build/toolcheck build/toolcheck/TargetCheck.java >/dev/null 2>&1; then
    COMPILE_TARGET_ARGS="--release 7"
elif "$JAVAC" -source 1.7 -target 1.7 -d build/toolcheck build/toolcheck/TargetCheck.java >/dev/null 2>&1; then
    COMPILE_TARGET_ARGS="-source 1.7 -target 1.7 -Xlint:-options"
else
    rm -rf build/toolcheck
    "$JAVAC" -version
    echo "This javac cannot compile Java 7-compatible bytecode."
    echo "Install a JDK 8, set JAVA_HOME to it, or extract one at tools/jdk8."
    echo "On macOS you can run: tools/download-temurin8-jdk-macos.sh"
    exit 1
fi
rm -rf build/toolcheck

mkdir -p build/classes
rm -rf build/classes
mkdir -p build/classes

find src -name '*.java' | sort > build/sources.txt
# shellcheck disable=SC2086
"$JAVAC" $COMPILE_TARGET_ARGS -encoding UTF-8 -d build/classes @build/sources.txt

if [ -d resources ]; then
    cp -R resources/. build/classes/
fi

{
    echo "Manifest-Version: 1.0"
    echo "Main-Class: net.minecraft.MinecraftLauncher"
    echo
} > build/manifest.mf

"$JAR" cfm MCLauncherRevival.jar build/manifest.mf -C build/classes .
echo "Built MCLauncherRevival.jar"
