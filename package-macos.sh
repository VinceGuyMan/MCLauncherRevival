#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")"

APP_NAME="MCLauncherRevival"
APP_DIR="dist/${APP_NAME}.app"
CONTENTS_DIR="${APP_DIR}/Contents"
MACOS_DIR="${CONTENTS_DIR}/MacOS"
RESOURCES_DIR="${CONTENTS_DIR}/Resources"

if [ ! -f MCLauncherRevival.jar ]; then
    echo "MCLauncherRevival.jar was not found; building it first."
    sh ./build-macos.sh
fi

rm -rf "$APP_DIR"
mkdir -p "$MACOS_DIR" "$RESOURCES_DIR"

cp MCLauncherRevival.jar "$RESOURCES_DIR/MCLauncherRevival.jar"
if [ -f resources/net/minecraft/favicon.png ]; then
    cp resources/net/minecraft/favicon.png "$RESOURCES_DIR/favicon.png"
    if command -v sips >/dev/null 2>&1 && command -v iconutil >/dev/null 2>&1; then
        ICONSET_DIR="dist/${APP_NAME}.iconset"
        rm -rf "$ICONSET_DIR"
        mkdir -p "$ICONSET_DIR"
        sips -z 16 16 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_16x16.png" >/dev/null 2>&1 || true
        sips -z 32 32 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_16x16@2x.png" >/dev/null 2>&1 || true
        sips -z 32 32 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_32x32.png" >/dev/null 2>&1 || true
        sips -z 64 64 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_32x32@2x.png" >/dev/null 2>&1 || true
        sips -z 128 128 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_128x128.png" >/dev/null 2>&1 || true
        sips -z 256 256 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_128x128@2x.png" >/dev/null 2>&1 || true
        sips -z 256 256 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_256x256.png" >/dev/null 2>&1 || true
        sips -z 512 512 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_256x256@2x.png" >/dev/null 2>&1 || true
        sips -z 512 512 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_512x512.png" >/dev/null 2>&1 || true
        sips -z 1024 1024 resources/net/minecraft/favicon.png --out "$ICONSET_DIR/icon_512x512@2x.png" >/dev/null 2>&1 || true
        iconutil -c icns "$ICONSET_DIR" -o "$RESOURCES_DIR/MCLauncherRevival.icns" >/dev/null 2>&1 || true
        rm -rf "$ICONSET_DIR"
    fi
fi

cat > "$CONTENTS_DIR/Info.plist" <<'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN"
 "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDisplayName</key>
    <string>MCLauncherRevival</string>
    <key>CFBundleExecutable</key>
    <string>MCLauncherRevival</string>
    <key>CFBundleIdentifier</key>
    <string>com.mclauncherrevival.launcher</string>
    <key>CFBundleIconFile</key>
    <string>MCLauncherRevival</string>
    <key>CFBundleName</key>
    <string>MCLauncherRevival</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>0.5.8.1-alpha</string>
    <key>CFBundleVersion</key>
    <string>0.5.8.1</string>
    <key>LSApplicationCategoryType</key>
    <string>public.app-category.games</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.8</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
PLIST

cat > "$MACOS_DIR/MCLauncherRevival" <<'LAUNCHER'
#!/usr/bin/env sh
set -eu

APP_BIN_DIR="$(dirname "$0")"
APP_CONTENTS_DIR="$(cd "$APP_BIN_DIR/.." && pwd)"
JAR_PATH="$APP_CONTENTS_DIR/Resources/MCLauncherRevival.jar"

show_error() {
    message="$1"
    if command -v osascript >/dev/null 2>&1; then
        osascript -e 'display dialog "'"$(printf '%s' "$message" | sed 's/"/\\"/g')"'" buttons {"OK"} default button "OK" with icon caution' >/dev/null 2>&1 || true
    fi
    echo "$message" >&2
}

find_java() {
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
if [ -z "$JAVA" ]; then
    show_error "Java was not found. Install Java 8 or set JAVA_HOME, then open MCLauncherRevival again."
    exit 1
fi

major="$(java_major "$JAVA")"
if [ -n "$major" ] && [ "$major" -gt 8 ] 2>/dev/null; then
    echo "Warning: Java $major detected. Java 8 is recommended for old Beta/Alpha Minecraft clients." >&2
fi

exec "$JAVA" \
    -Dapple.awt.application.name=MCLauncherRevival \
    -Dsun.java2d.fontpath=/System/Library/Fonts:/Library/Fonts \
    -Xdock:name=MCLauncherRevival \
    ${MCLAUNCHER_JAVA_OPTS:-} \
    -jar "$JAR_PATH" \
    "$@"
LAUNCHER

chmod +x "$MACOS_DIR/MCLauncherRevival"

echo "Built unsigned app bundle: $APP_DIR"
echo "This app is not signed or notarized. Gatekeeper may require right-click > Open or System Settings approval."
