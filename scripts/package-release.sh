#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname "$0")/.." && pwd)
cd "$ROOT_DIR"

RELEASE_VERSION=${1:-v0.7.5-alpha}
PACKAGE_NAME="MCLauncherRevival-${RELEASE_VERSION}"
RELEASE_ROOT=${RELEASE_ROOT:-"$ROOT_DIR/../release"}
STAGE_PARENT="$RELEASE_ROOT/_staging"
STAGE_DIR="$STAGE_PARENT/$PACKAGE_NAME"
ZIP_PATH="$RELEASE_ROOT/$PACKAGE_NAME.zip"

command -v zip >/dev/null 2>&1 || {
    echo "zip is required to build the release package." >&2
    exit 1
}
command -v unzip >/dev/null 2>&1 || {
    echo "unzip is required to verify the release package." >&2
    exit 1
}

echo "Preparing $PACKAGE_NAME"
sh ./scripts/build-linux.sh
test -f MCLauncherRevival.jar

rm -rf "$STAGE_DIR" "$ZIP_PATH"
mkdir -p "$STAGE_DIR/scripts" "$STAGE_DIR/tools"

for file in \
    MCLauncherRevival.jar README.md CHANGELOG.md ASSETS.md SECURITY.md LICENSE NOTICE.md \
    "Setup MCLR.cmd" "Start MCLR.cmd" "Start MCLR XP.cmd" \
    "Start MCLauncherRevival.command" build-macos.sh run-macos.sh package-macos.sh; do
    cp "$file" "$STAGE_DIR/"
done

for file in \
    run-win.cmd build-win.cmd test-win.cmd banner.txt boot-card-win.txt boot-card-xp.txt \
    run-linux.sh build-linux.sh run-macos.sh build-macos.sh test-java.sh; do
    cp "scripts/$file" "$STAGE_DIR/scripts/"
done

for directory in docs resources src tests; do
    cp -R "$directory" "$STAGE_DIR/"
done

cp tools/download-temurin8-jdk.ps1 "$STAGE_DIR/tools/"
if [ -f tools/download-temurin8-jdk-macos.sh ]; then
    cp tools/download-temurin8-jdk-macos.sh "$STAGE_DIR/tools/"
fi

cat > "$STAGE_DIR/RELEASE_INFO.txt" <<EOF
MCLauncherRevival $RELEASE_VERSION

This is the runnable release package.
Do not confuse this package with GitHub source-code or tag ZIP archives.

Recommended first run on Windows:
  Setup MCLR.cmd

Start on Windows 7-11:
  Start MCLR.cmd

Start on Windows XP offline/classic mode:
  Start MCLR XP.cmd

Start on macOS:
  Start MCLauncherRevival.command
  or ./run-macos.sh

Start on Linux:
  ./scripts/run-linux.sh

Internal helper scripts live under scripts/.

This project is unofficial alpha software and is not affiliated with Mojang, Microsoft, Xbox, or Minecraft.
EOF

# ZIP stores Unix mode bits. Normalize them so directories remain traversable and
# launch/build helpers remain executable after extraction on macOS and Linux.
find "$STAGE_DIR" -type d -exec chmod 755 {} +
find "$STAGE_DIR" -type f -exec chmod 644 {} +
find "$STAGE_DIR" -type f \( -name '*.sh' -o -name '*.command' \) -exec chmod 755 {} +

mkdir -p "$RELEASE_ROOT"
(cd "$STAGE_PARENT" && zip -q -r "$ZIP_PATH" "$PACKAGE_NAME")
unzip -tq "$ZIP_PATH"

entries=$(unzip -Z1 "$ZIP_PATH")
for required in \
    MCLauncherRevival.jar scripts/run-win.cmd scripts/run-linux.sh \
    "Start MCLauncherRevival.command" resources/net/minecraft/themes/beta.png \
    src/net/minecraft/MinecraftLauncher.java tests/net/minecraft/LauncherSelfTest.java; do
    printf '%s\n' "$entries" | grep -Fq "$PACKAGE_NAME/$required" || {
        echo "Release ZIP is missing $required" >&2
        exit 1
    }
done

if printf '%s\n' "$entries" | grep -Eq '(^|/)(auth\.properties|game-launch\.properties|launcher\.properties|\.env|\.git|jdk8|build/classes|\.DS_Store)(/|$)'; then
    echo "Release ZIP contains a local, generated, or sensitive path." >&2
    exit 1
fi

rm -rf "$STAGE_DIR"
echo "Created and verified: $ZIP_PATH"
