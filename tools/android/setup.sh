#!/usr/bin/env bash
set -euo pipefail
source "$(dirname -- "${BASH_SOURCE[0]}")/env.sh"
[[ ${1:-} == "" || ${1:-} == --host-packages ]] || die "Usage: setup.sh [--host-packages]"
[[ $(uname -s) == Linux ]] || die 'This setup requires Linux.'
[[ $(uname -m) == x86_64 ]] || die 'Google Linux emulator distribution requires x86_64; this host needs a supported emulator host/image combination.'
if [[ ${1:-} == --host-packages ]]; then
    sudo apt-get update
    sudo apt-get install -y openjdk-17-jdk-headless curl unzip python3 libgl1 libegl1 libx11-6 libxcb1 libnss3 libasound2t64 libpulse0 libvulkan1
    source "$(dirname -- "${BASH_SOURCE[0]}")/env.sh"
fi
need curl; need unzip; need python3
exec 9>"$STATE/setup.lock"
flock 9
scratch=$(mktemp -d)
trap 'rm -rf "$scratch"' EXIT
if ! command -v javac >/dev/null || ! java -version 2>&1 | grep -q 'version "17\.' || ! javac -version 2>&1 | grep -q '^javac 17\.'; then
    echo 'Installing checksum-verified Temurin JDK 17 in user-local storage.'
    curl -fSL --retry 3 'https://api.adoptium.net/v3/assets/latest/17/hotspot?architecture=x64&image_type=jdk&os=linux' -o "$scratch/java.json"
    read -r url checksum < <(python3 - "$scratch/java.json" <<'PY'
import json,sys
p=json.load(open(sys.argv[1]))[0]['binary']['package'];print(p['link'],p['checksum'])
PY
)
    curl -fSL --retry 3 "$url" -o "$scratch/jdk.tar.gz"
    printf '%s  %s\n' "$checksum" "$scratch/jdk.tar.gz" | sha256sum -c -
    mkdir -p "$HOME/.local/share/mobiling/jdk-17"
    tar -xzf "$scratch/jdk.tar.gz" --strip-components=1 -C "$HOME/.local/share/mobiling/jdk-17"
    export JAVA_HOME=$HOME/.local/share/mobiling/jdk-17
    export PATH=$JAVA_HOME/bin:$PATH
fi
java -version
if [[ ! -x $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager ]]; then
    curl -fSL --retry 3 https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -o "$scratch/tools.zip"
    printf '%s  %s\n' 7ec965280a073311c339e571cd5de778b9975026cfcbe79f2b1cdcb1e15317ee "$scratch/tools.zip" | sha256sum -c -
    unzip -q "$scratch/tools.zip" -d "$scratch/tools"
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    mv "$scratch/tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
fi
# Avoid yes/SIGPIPE defeating pipefail; preserve sdkmanager's exit status.
set +o pipefail
if yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses >"$STATE/licenses.log" 2>&1; then
    license_status=0
else
    license_status=$?
fi
set -o pipefail
(( license_status == 0 )) || die "SDK license acceptance failed; see $STATE/licenses.log"
sdkmanager --sdk_root="$ANDROID_HOME" 'platform-tools' 'emulator' 'platforms;android-34' 'build-tools;34.0.0' 'system-images;android-34;google_apis;x86_64'
python3 - "$REPO_ROOT/client/android/local.properties" "$ANDROID_HOME" <<'PY'
import pathlib,sys
p=pathlib.Path(sys.argv[1]); lines=p.read_text().splitlines() if p.exists() else []
value=sys.argv[2].replace('\\','\\\\').replace(':','\\:').replace(' ','\\ ')
p.write_text('\n'.join([s for s in lines if s.split('=',1)[0].strip() != 'sdk.dir']+['sdk.dir='+value])+'\n')
PY
if ! emulator -list-avds | grep -qx "$AVD"; then
    printf 'no\n' | avdmanager create avd --name "$AVD" --package 'system-images;android-34;google_apis;x86_64' --device 'pixel_2'
fi
python3 - "$ANDROID_AVD_HOME/$AVD.avd/config.ini" "$ANDROID_HOME" <<'PY'
import pathlib,sys
p=pathlib.Path(sys.argv[1]); d={k.strip():v.strip() for s in p.read_text().splitlines() if '=' in s for k,v in [s.split('=',1)]}
if 'android-34/google_apis/x86_64' not in d.get('image.sysdir.1',''): raise SystemExit('Existing AVD uses a different image; refusing to overwrite userdata. Rename it before setup.')
d.update({'hw.ramSize':'2560','hw.cpu.ncore':'2','hw.lcd.width':'720','hw.lcd.height':'1280','hw.lcd.density':'240','hw.gpu.enabled':'yes','hw.gpu.mode':'swiftshader_indirect','disk.dataPartition.size':'6G','showDeviceFrame':'no','hw.keyboard':'yes','fastboot.forceColdBoot':'yes'})
p.write_text(''.join(f'{k}={v}\n' for k,v in d.items()))
PY
mkdir -p "$HOME/.config/mobiling"
# Shell-quoted values; no paths enter tracked files.
{
    printf 'export ANDROID_HOME=%q\nexport ANDROID_SDK_ROOT=%q\n' "$ANDROID_HOME" "$ANDROID_HOME"
    printf 'export JAVA_HOME=%q\n' "${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")}"
    printf 'export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"\n'
} > "$HOME/.config/mobiling/android-env.sh"
"$REPO_ROOT/tools/android/sandbox.sh" status 9>&-
printf 'Setup complete. Interactive shell: source %s\n' "$HOME/.config/mobiling/android-env.sh"
