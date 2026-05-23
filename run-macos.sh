#!/usr/bin/env sh
set -eu

cd "$(dirname "$0")"
exec sh ./scripts/run-macos.sh "$@"
