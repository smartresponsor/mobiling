#!/usr/bin/env bash
set -euo pipefail
REPO_ROOT=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)
[[ $(uname -s) == Linux ]] || { echo "This Bash workflow requires Linux; use the Windows PowerShell workflow." >&2; exit 1; }
export ANDROID_HOME=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}
export ANDROID_SDK_ROOT=$ANDROID_HOME
if [[ -z ${JAVA_HOME:-} ]]; then
    for candidate in /usr/lib/jvm/java-17-openjdk-$(dpkg --print-architecture) "$HOME/.local/share/mobiling/jdk-17"; do
        if [[ -x $candidate/bin/javac ]]; then export JAVA_HOME=$candidate; break; fi
    done
fi
export PATH="${JAVA_HOME:+$JAVA_HOME/bin:}$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
STATE=$REPO_ROOT/.codebase-memory/android
ARTIFACTS=$REPO_ROOT/watchdog/runtime/device
AVD=Mobiling_Light_API_34
export ANDROID_AVD_HOME=${ANDROID_AVD_HOME:-${ANDROID_USER_HOME:-$HOME/.android}/avd}
mkdir -p "$STATE"
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }
need() { command -v "$1" >/dev/null || die "Missing $1; run tools/android/sandbox.sh setup"; }
healthy() {
    [[ $(timeout 8 adb -s "$1" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r') == 1 ]] &&
        timeout 10 adb -s "$1" shell pm path android 2>/dev/null | grep -q '^package:'
}
find_avd() {
    local s
    while read -r s status rest; do
        [[ $s == emulator-* && $status == device ]] || continue
        if [[ $(timeout 5 adb -s "$s" emu avd name 2>/dev/null | head -1 | tr -d '\r') == "$AVD" ]]; then echo "$s"; return; fi
    done < <(adb devices)
    return 1
}
select_device() {
    need adb
    SERIAL=${ANDROID_SERIAL:-$(find_avd || true)}
    [[ -n $SERIAL ]] || die "No Mobiling emulator attached. Run sandbox.sh start, or set ANDROID_SERIAL."
    healthy "$SERIAL" || die "Device $SERIAL is not booted/usable. Run sandbox.sh diagnose."
}
