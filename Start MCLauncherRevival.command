#!/usr/bin/env sh

cd "$(dirname "$0")" || exit 1
sh ./run-macos.sh "$@"
status=$?

if [ "$status" -ne 0 ]; then
    echo
    echo "MCLauncherRevival exited with code $status."
    echo "Press Return to close this window."
    read _ignored
fi

exit "$status"
