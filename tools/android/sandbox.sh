#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "${BASH_SOURCE[0]}")/env.sh"
EDGE_PORT=${MOBILING_EDGE_PORT:-18080}
[[ $EDGE_PORT =~ ^[1-9][0-9]{3,4}$ && $EDGE_PORT -gt 1024 && $EDGE_PORT -lt 65536 ]] || die 'Invalid MOBILING_EDGE_PORT'
owned_pid() {
    local file=$1 match=$2 pid
    [[ -f $file ]] || return 1
    read -r pid < "$file"
    [[ $pid =~ ^[0-9]+$ && -r /proc/$pid/cmdline ]] || return 1
    python3 - "$pid" "$match" <<'PYOWNED' || return 1
import pathlib,sys
try:
    args=pathlib.Path(f'/proc/{sys.argv[1]}/cmdline').read_bytes().decode().split('\0')
except (OSError,UnicodeDecodeError):
    sys.exit(1)
match=sys.argv[2]
valid=any(a == '-avd' and i+1 < len(args) and args[i+1] == match[5:] for i,a in enumerate(args)) if match.startswith('-avd ') else match in args
sys.exit(0 if valid else 1)
PYOWNED
    echo "$pid"
}
diagnose() {
    adb devices -l || true
    emulator -accel-check || true
    ls -l /dev/kvm || true
    tail -60 "$STATE/emulator.log" 2>/dev/null || true
    if [[ -n ${SERIAL:-} ]]; then
        timeout 10 adb -s "$SERIAL" shell getprop || true
        timeout 10 adb -s "$SERIAL" logcat -d -t 100 > "$STATE/boot-logcat.txt" || true
    fi
}
start() {
    need adb; need emulator; need setsid
    adb start-server 7>&- 8>&- 9>&-
    exec 8>"$STATE/emulator.lock"; flock 8
    SERIAL=$(find_avd || true)
    if [[ -n $SERIAL ]] && healthy "$SERIAL"; then
        if (flock 6; timeout 40 "$REPO_ROOT/tools/android/ui.py" "$SERIAL" ready) 6>"$STATE/ui.lock"; then echo "Ready: $SERIAL ($AVD)"; return; fi
    fi
    [[ -r /dev/kvm && -w /dev/kvm ]] || die '/dev/kvm is absent/inaccessible. Enable VT-x/AMD-V and grant kvm group or ACL access (relogin after group changes).'
    emulator -accel-check || die 'KVM acceleration check failed.'
    if [[ -z $SERIAL ]]; then
        if ! owned_pid "$STATE/emulator.pid" "-avd $AVD" >/dev/null; then
            # Recover only locks belonging to this stopped AVD; never delete userdata.
            if pgrep -af "[q]emu-system.*-avd $AVD|[e]mulator.*-avd $AVD" >/dev/null; then
                die 'AVD process exists outside this launcher but is not adb-ready. Inspect diagnose before stopping it.'
            fi
            local avd_dir=$ANDROID_AVD_HOME/$AVD.avd
            [[ -d $avd_dir ]] || die 'AVD missing; run setup.'
            find "$avd_dir" -maxdepth 1 -name '*.lock' -exec rm -rf -- {} +
            [[ -z $(adb devices | awk '$1=="emulator-5554" {print $1}') ]] || die 'Port 5554 belongs to another device.'
            if ss -ltn | grep -Eq ':555[45][[:space:]]'; then die 'Emulator port 5554/5555 is occupied.'; fi
            setsid nohup emulator -avd "$AVD" -port 5554 -no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader_indirect -accel on > "$STATE/emulator.log" 2>&1 < /dev/null 7>&- 8>&- 9>&- &
            echo "$!" > "$STATE/emulator.pid"
        fi
        SERIAL=emulator-5554
    fi
    local deadline=$((SECONDS + ${MOBILING_BOOT_TIMEOUT:-300}))
    while (( SECONDS < deadline )); do
        if healthy "$SERIAL"; then
            adb -s "$SERIAL" shell input keyevent KEYCODE_WAKEUP
            adb -s "$SERIAL" shell wm dismiss-keyguard
            # Package manager plus an actual UI dump confirm boot is usable.
            if (flock 6; timeout 40 "$REPO_ROOT/tools/android/ui.py" "$SERIAL" ready) 6>"$STATE/ui.lock"; then
                echo "Ready: $SERIAL ($AVD), sys.boot_completed=1"; return
            fi
        fi
        sleep 3
    done
    diagnose
    die 'Emulator did not become usable before boot timeout.'
}
stop() {
    need adb
    exec 8>"$STATE/emulator.lock"; flock 8
    local serial pid
    serial=$(find_avd || true)
    pid=$(owned_pid "$STATE/emulator.pid" "-avd $AVD" || true)
    if [[ -n $serial ]]; then timeout 15 adb -s "$serial" emu kill || true; fi
    if [[ -n $pid ]]; then
        for ((i=0; i<30; i++)); do kill -0 "$pid" 2>/dev/null || break; sleep 1; done
        if owned_pid "$STATE/emulator.pid" "-avd $AVD" >/dev/null; then
            kill -TERM "$pid"
            for ((i=0; i<10; i++)); do
                owned_pid "$STATE/emulator.pid" "-avd $AVD" >/dev/null || break
                sleep 1
            done
            owned_pid "$STATE/emulator.pid" "-avd $AVD" >/dev/null && die 'Owned emulator did not stop after SIGTERM.'
        fi
    fi
    if pgrep -af "[q]emu-system.*-avd $AVD|[e]mulator.*-avd $AVD" >/dev/null; then die 'AVD process is still running; state retained for diagnosis.'; fi
    rm -f "$STATE/emulator.pid"
    echo "$AVD stopped."
}
status() {
    printf 'Host: '; . /etc/os-release; echo "$PRETTY_NAME ($(uname -m))"
    printf 'Virtualization: '; systemd-detect-virt || true
    id; free -h; df -h "$HOME"
    if [[ -e /dev/kvm ]]; then ls -l /dev/kvm; [[ ! -r /dev/kvm || ! -w /dev/kvm ]] || echo 'KVM: user read/write access'; else echo 'KVM: absent'; fi
    for tool in java javac git bash curl unzip python3 node npm adb sdkmanager avdmanager emulator; do
        printf '%-12s %s\n' "$tool" "$(command -v "$tool" || echo MISSING)"
    done
    command -v java >/dev/null && java -version 2>&1 || true
    command -v node >/dev/null && node --version || true
    command -v npm >/dev/null && npm --version || true
    echo "SDK: $ANDROID_HOME"
    for dir in platforms build-tools system-images; do find "$ANDROID_HOME/$dir" -maxdepth 5 -name source.properties -print 2>/dev/null || true; done
    if command -v emulator >/dev/null; then emulator -version | head -3; emulator -accel-check || true; emulator -list-avds; fi
    command -v adb >/dev/null && adb devices -l || true
    "$REPO_ROOT/tools/android/snapshot.sh"
}
edge_health() {
    need curl
    curl -fsS --max-time 5 "http://127.0.0.1:$EDGE_PORT/health" | python3 -c 'import json,sys; assert json.load(sys.stdin).get("ok") is True; print("Mobile Edge host health: OK")'
    select_device
    printf 'GET /health HTTP/1.0\r\nHost: 10.0.2.2\r\n\r\n' | timeout 12 adb -s "$SERIAL" shell "toybox nc -w 5 10.0.2.2 $EDGE_PORT" > "$STATE/edge-emulator-health.txt"
    python3 - "$STATE/edge-emulator-health.txt" <<'PY'
import pathlib,json,sys
s=pathlib.Path(sys.argv[1]).read_text(); head,body=s.split('\n\n',1)
assert ' 200 ' in head.splitlines()[0] and json.loads(body).get('ok') is True, s
print('Mobile Edge emulator gateway health: OK')
PY
    # /health is only liveness. Report the actual upstream state separately.
    for route in navigation/mobile/shell catalog access/session; do
        local code
        code=$(curl -sS --max-time 10 -o "$STATE/upstream-${route//\//-}.json" -w '%{http_code}' "http://127.0.0.1:$EDGE_PORT/$route")
        echo "Upstream /$route: HTTP $code (response in $STATE)"
    done
}
edge() {
    local action=${1:-health} pid
    exec 7>"$STATE/edge.lock"; flock 7
    pid=$(owned_pid "$STATE/edge.pid" "$REPO_ROOT/mobile-edge/var/android-dist/app.js" || true)
    case "$action" in
        start)
            if [[ -n $pid ]]; then
                if [[ -f $STATE/edge.port && $(cat "$STATE/edge.port") != "$EDGE_PORT" ]]; then die 'Owned Edge is running on a different port; restart it with the requested MOBILING_EDGE_PORT.'; fi
                curl -fsS --max-time 3 "http://127.0.0.1:$EDGE_PORT/health" | python3 -c 'import json,sys; assert json.load(sys.stdin).get("ok") is True' || die 'Owned Edge is not healthy on the requested port; use edge restart.'
                echo "Mobile Edge already running: $pid"; return
            fi
            if ss -ltn | grep -Eq ":$EDGE_PORT[[:space:]]"; then die "Port $EDGE_PORT is occupied; choose MOBILING_EDGE_PORT. No existing service will be killed."; fi
            need node; need npm; need setsid
            if [[ -f $REPO_ROOT/.codebase-memory/mobile-edge.env ]]; then
                set -a; source "$REPO_ROOT/.codebase-memory/mobile-edge.env"; set +a
            fi
            (cd "$REPO_ROOT/mobile-edge"; [[ -d node_modules ]] || npm ci; node node_modules/typescript/bin/tsc -p tsconfig.json --outDir var/android-dist)
            (cd "$REPO_ROOT/mobile-edge"; exec setsid nohup env PORT="$EDGE_PORT" NODE_ENV=development node "$REPO_ROOT/mobile-edge/var/android-dist/app.js") >"$STATE/edge.log" 2>&1 < /dev/null 7>&- 8>&- 9>&- &
            echo "$!" > "$STATE/edge.pid"
            echo "$EDGE_PORT" > "$STATE/edge.port"
            for ((i=0;i<30;i++)); do
                if curl -fsS --max-time 2 "http://127.0.0.1:$EDGE_PORT/health" | python3 -c 'import json,sys; assert json.load(sys.stdin).get("ok") is True' 2>/dev/null; then echo "Mobile Edge ready on port $EDGE_PORT"; return; fi
                sleep 1
            done
            tail -40 "$STATE/edge.log"; die 'Mobile Edge failed to start.' ;;
        stop)
            if [[ -n $pid ]]; then
                kill -TERM "$pid"
                for ((i=0;i<15;i++)); do
                    owned_pid "$STATE/edge.pid" "$REPO_ROOT/mobile-edge/var/android-dist/app.js" >/dev/null || break
                    sleep 1
                done
                if owned_pid "$STATE/edge.pid" "$REPO_ROOT/mobile-edge/var/android-dist/app.js" >/dev/null; then die 'Owned Edge process did not stop after SIGTERM.'; fi
            fi
            rm -f "$STATE/edge.pid" "$STATE/edge.port"; echo 'Owned Mobile Edge process stopped.' ;;
        restart) flock -u 7; edge stop; edge start ;;
        health) edge_health ;;
        *) die 'Usage: sandbox.sh edge {start|stop|restart|health}' ;;
    esac
}
run_app() {
    select_device
    local reset=false animations=false skip=false brand= task=
    while (($#)); do
        case "$1" in
            --reset) reset=true;; --animations) animations=true;; --skip-build) skip=true;;
            --brand) shift; brand=${1:?Brand required};; *) die "Unknown run option: $1";;
        esac
        shift
    done
    case "$brand" in
        oneTasker) task=:app:assembleOneTaskerDebug;;
        smartResponsor) task=:app:assembleSmartResponsorDebug;;
        *) die 'Explicit --brand oneTasker|smartResponsor is required';;
    esac
    if ! $skip; then
        need java
        (cd "$REPO_ROOT/client/android"; ./gradlew "$task" --no-daemon --max-workers=2 '-Dorg.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8' -Pkotlin.compiler.execution.strategy=in-process "-PmobilingDevEdgeUrl=http://10.0.2.2:$EDGE_PORT")
    fi
    # AGP metadata supplies the actual APK and application ID, including flavors.
    mapfile -t apk_info < <(python3 - "$REPO_ROOT/client/android/app/build/outputs/apk" "$brand" <<'PY'
import pathlib,json,sys
c=[]
brand=sys.argv[2]
for p in pathlib.Path(sys.argv[1]).rglob('output-metadata.json'):
 d=json.loads(p.read_text())
 if d.get('variantName','').lower()==(brand+'Debug').lower():
  for e in d['elements']:
   a=p.parent/e['outputFile']
   if a.exists(): c.append((a,d['applicationId']))
if len(c)!=1: raise SystemExit(f'Expected one debug APK; found {len(c)}. Remove stale outputs or build a single variant.')
print(c[0][0]); print(c[0][1])
PY
)
    [[ ${#apk_info[@]} == 2 ]] || die 'Unable to select debug APK from AGP output metadata.'
    local apk=${apk_info[0]} package=${apk_info[1]} activity expected_package
    case "$brand" in
        oneTasker) expected_package=app.mobiling.client.onetasker;;
        smartResponsor) expected_package=app.mobiling.client.smartresponsor;;
    esac
    [[ $package == "$expected_package" ]] || die "Brand/package mismatch: $brand -> $package (expected $expected_package)"
    adb -s "$SERIAL" install -r "$apk"
    if $reset; then adb -s "$SERIAL" shell pm clear "$package"; fi
    if $animations; then
        for setting in window_animation_scale transition_animation_scale animator_duration_scale; do adb -s "$SERIAL" shell settings put global "$setting" 0; done
    fi
    activity=$(adb -s "$SERIAL" shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER "$package" | tr -d '\r' | tail -1)
    [[ $activity == "$package/"* ]] || die "No launcher activity found for $package"
    adb -s "$SERIAL" shell am force-stop "$package"
    adb -s "$SERIAL" shell am start -W -n "$activity" | tee "$STATE/launch.txt"
    if grep -Eq 'Error:|Exception' "$STATE/launch.txt"; then die 'Activity launch failed.'; fi
    local foreground=false
    for ((i=0;i<35;i++)); do
        if adb -s "$SERIAL" shell dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity' | grep -Fq "$package/"; then foreground=true; break; fi
        sleep 1
    done
    $foreground || die 'App did not reach foreground.'
    adb -s "$SERIAL" shell pidof "$package" >/dev/null || die 'App process exited.'
    printf '%s\n' "$package" > "$STATE/package"
    printf 'APK: %s\nPackage: %s\nLauncher: %s\nSerial: %s\n' "$apk" "$package" "$activity" "$SERIAL"
}
capture() {
    if [[ ${UI_LOCK_HELD:-false} != true ]]; then exec 6>"$STATE/ui.lock"; flock 6; fi
    select_device
    local name=${1:-capture} stamp
    [[ $name =~ ^[a-zA-Z0-9._-]+$ ]] || die 'Capture name must contain only letters, digits, dot, underscore or dash.'
    stamp=$(date -u +%Y%m%d-%H%M%S-%N)
    mkdir -p "$ARTIFACTS/history"
    timeout 30 adb -s "$SERIAL" shell uiautomator dump /sdcard/mobiling-window.xml
    adb -s "$SERIAL" pull /sdcard/mobiling-window.xml "$ARTIFACTS/current-ui.xml" >/dev/null
    adb -s "$SERIAL" exec-out screencap -p > "$ARTIFACTS/latest.png"
    python3 - "$ARTIFACTS" <<'PY'
import pathlib,sys,xml.etree.ElementTree as E
p=pathlib.Path(sys.argv[1]);E.parse(p/'current-ui.xml');assert (p/'latest.png').read_bytes().startswith(b'\x89PNG\r\n\x1a\n')
PY
    cp "$ARTIFACTS/latest.png" "$ARTIFACTS/history/$stamp-$name.png"
    cp "$ARTIFACTS/current-ui.xml" "$ARTIFACTS/history/$stamp-$name.xml"
    adb -s "$SERIAL" shell dumpsys window > "$ARTIFACTS/current-window.txt"
    echo "Screenshot: $ARTIFACTS/history/$stamp-$name.png"
    echo "UI dump: $ARTIFACTS/current-ui.xml"
    flock -u 6
}
ui() {
    exec 6>"$STATE/ui.lock"; flock 6
    select_device
    "$REPO_ROOT/tools/android/ui.py" "$SERIAL" "$@"
    flock -u 6
}
smoke() {
    start
    # Build while the emulator is up with constrained Gradle memory/worker count.
    edge start
    run_app "$@" --reset --animations
    edge_health
    capture welcome
    ui smoke
    capture access-smoke
    echo 'PASS: boot, install, foreground, host/gateway health and access UI interaction. Upstream service availability is reported separately.'
}
case "${1:-help}" in
    setup) shift; "$REPO_ROOT/tools/android/setup.sh" "$@";;
    status) status;; start) start;; stop) stop;;
    restart) stop; start;;
    recover) stop; start;;
    diagnose) need adb; need emulator; SERIAL=$(find_avd || true); diagnose;;
    run) shift; run_app "$@";;
    capture) shift; capture "$@";;
    tap) shift; exec 6>"$STATE/ui.lock"; flock 6; select_device; [[ ${1:-} =~ ^[0-9]+$ && ${2:-} =~ ^[0-9]+$ ]] || die 'Usage: tap X Y [name]'; adb -s "$SERIAL" shell input tap "$1" "$2"; sleep 1; UI_LOCK_HELD=true capture "${3:-tap}";;
    ui) shift; ui "$@";;
    edge) shift; edge "$@";;
    verify-upstream)
        edge_health
        for route in navigation/mobile/shell catalog access/session; do
            code=$(curl -sS --max-time 10 -o /dev/null -w '%{http_code}' "http://127.0.0.1:$EDGE_PORT/$route")
            [[ $code == 2* ]] || die "Upstream /$route returned HTTP $code; configure the development runtime."
        done;;
    smoke) shift; smoke "$@";;
    help|-h|--help) echo 'Usage: sandbox.sh {status|setup [--host-packages]|start|stop|restart|recover|diagnose|run --brand oneTasker|smartResponsor [--reset] [--animations] [--skip-build]|capture [NAME]|tap X Y [NAME]|ui {smoke|open-signin|tap-text TEXT|login}|edge {start|stop|restart|health}|verify-upstream|smoke --brand oneTasker|smartResponsor}';;
    *) die "Unknown command: $1 (use help)";;
esac
