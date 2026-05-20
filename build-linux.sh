#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")"

find_tool() {
    name="$1"
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/$name" ]; then
        printf '%s\n' "$JAVA_HOME/bin/$name"
        return 0
    fi
    if [ -x "./tools/jdk8/bin/$name" ]; then
        printf '%s\n' "./tools/jdk8/bin/$name"
        return 0
    fi
    for candidate in ./tools/jdk8/*/bin/"$name"; do
        if [ -x "$candidate" ]; then
            printf '%s\n' "$candidate"
            return 0
        fi
    done
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
    echo "Java JDK 8 is recommended to build MCLauncherRevival on Linux."
    echo "Install a JDK with javac and jar on PATH, set JAVA_HOME, or extract one at tools/jdk8."
    exit 1
fi

if [ -n "$JAVA" ]; then
    echo "Java runtime found: $JAVA"
fi
echo "Java compiler found: $JAVAC"

mkdir -p build/classes
rm -rf build/classes
mkdir -p build/classes

find src -name '*.java' | sort > build/sources.txt
"$JAVAC" -source 1.7 -target 1.7 -encoding UTF-8 -d build/classes @build/sources.txt

mkdir -p build/classes/net/minecraft
if [ -d resources/net/minecraft ]; then
    for file in resources/net/minecraft/*.png resources/net/minecraft/*.jpg; do
        if [ -f "$file" ]; then
            cp "$file" build/classes/net/minecraft/
        fi
    done
fi

{
    echo "Manifest-Version: 1.0"
    echo "Main-Class: net.minecraft.MinecraftLauncher"
    echo
} > build/manifest.mf

"$JAR" cfm MCLauncherRevival.jar build/manifest.mf -C build/classes .
echo "Built MCLauncherRevival.jar"
