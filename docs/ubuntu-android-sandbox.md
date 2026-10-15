# Ubuntu Android sandbox

Ubuntu/Linux is an additional first-class supported sandbox alongside the [Windows PowerShell sandbox](windows-android-sandbox.md). Neither platform is deprecated or historical-only.

Use the canonical `client/android` Gradle wrapper and SDK command-line tools; Android Studio and Docker are unnecessary. All commands below work from any directory when invoked by their absolute path. From this repository root:

```bash
tools/android/sandbox.sh status
tools/android/sandbox.sh setup                 # SDK + licenses + AVD + Java 17
# Optional Ubuntu system packages (requires sudo):
tools/android/sandbox.sh setup --host-packages
source "$HOME/.config/mobiling/android-env.sh" # optional for interactive adb/Gradle

tools/android/sandbox.sh start
tools/android/sandbox.sh edge start
tools/android/sandbox.sh run --brand oneTasker --reset --animations
tools/android/sandbox.sh edge health
tools/android/sandbox.sh capture welcome
tools/android/sandbox.sh ui open-signin
tools/android/sandbox.sh tap 360 600 example
tools/android/sandbox.sh smoke --brand oneTasker # resets selected app data, exercises access UI

tools/android/sandbox.sh stop
tools/android/sandbox.sh edge stop
```

Prerequisites: x86_64 Ubuntu, Bash, curl, unzip, Python 3, Node/npm, KVM access and about 8 GB free disk. Runtime libraries are installed by `setup --host-packages`. Setup prefers installed Java 17; otherwise it installs checksum-verified Temurin 17 under `~/.local/share/mobiling`. The default SDK is `~/Android/Sdk`; `ANDROID_HOME` can override it. Setup writes an ignored `client/android/local.properties` and a shell environment file. It accepts SDK licenses non-interactively. Re-running setup preserves AVD userdata and converges configuration.

The `Mobiling_Light_API_34` AVD uses API 34 Google APIs x86_64, two virtual CPUs, 2560 MB RAM (the current Google APIs image/emulator raises smaller requests to this minimum), a 720×1280 display at 240 dpi, a 6 GB data partition (the API 34 image’s minimum), KVM and software graphics. Headless cold boots avoid dependence on a desktop or snapshots. Emulator and Edge launchers detach their process sessions so they survive the launcher exiting. Builds use Java 17, API/build-tools 34, two Gradle workers and a 1 GB heap to fit this host. Unsupported host architectures fail explicitly instead of downloading an incompatible emulator.

`start` reuses the named healthy AVD, waits up to 300 seconds for boot completion, package manager and a usable UIAutomator dump, then dismisses the keyguard. `MOBILING_BOOT_TIMEOUT` changes the deadline. Readiness/UI automation retries an observed System UI ANR using the enabled Wait action at its current bounds, up to three dumps; persistent dialogs fail with diagnostics. `restart`/`recover` stop and cold-start the AVD without clearing userdata. Stale lock cleanup is restricted to that stopped AVD. `stop` is safe to repeat. Emulator logs and owned process state live in ignored `.codebase-memory/android`. `diagnose` reports adb, KVM and boot logs. Other devices are never selected implicitly; use `ANDROID_SERIAL` to explicitly target a healthy device for install/capture. Network checks and the existing app URL assume the standard Android Emulator; physical-device networking requires a separate application configuration.

`run` requires an explicit `--brand oneTasker|smartResponsor`, builds only that flavor, and checks AGP metadata against its expected application ID before installation. It resolves the installed launcher activity. It checks process and foreground state. Use `--skip-build` only for a previously built APK with matching network configuration; The selected flavor determines the Gradle task. Ambiguous or mismatched APK outputs fail rather than guessing. `--reset` clears app data; `--animations` disables the device's three animation scales. Restore them with `adb shell settings put global <scale> 1` if desired.

## Mobile Edge and upstream services

The Ubuntu launcher defaults to `MOBILING_EDGE_PORT=18080`, as a development-only alternate port to avoid common 8080 conflicts; it does not change the Edge server’s global/default port. It passes a **debug-only** Gradle property, `mobilingDevEdgeUrl=http://10.0.2.2:18080`. Direct builds and release configuration retain the existing `http://10.0.2.2:8080` default. Set `MOBILING_EDGE_PORT=8080` consistently for Edge and Android commands on a host where that port is free. The emulator reaches host services through `10.0.2.2`; emulator `127.0.0.1` is not the host. The existing Edge bind behavior is unchanged.

Configure actual development upstreams in the Git-ignored `.codebase-memory/mobile-edge.env` before `edge start` (or export the variables in your shell):

```bash
ACCESSING_API_BASE_URL=http://127.0.0.1:8000
NAVIGATING_API_BASE_URL=http://127.0.0.1:8000
CATALOGING_API_BASE_URL=http://127.0.0.1:8000
CARTING_API_BASE_URL=http://127.0.0.1:8000
VENDORING_API_BASE_URL=http://127.0.0.1:8000
ATTACHING_API_BASE_URL=http://127.0.0.1:8000
LOCALIZING_API_BASE_URL=http://127.0.0.1:8000
```

These are the repository's old upstream topology, not proof that a server runs there. Point them at your actual App/component runtime. The launcher never installs or invents upstream business services or accounts. `edge restart` rebuilds and reloads configuration, and stops only its owned process. No service occupying a port is killed. `edge health` verifies JSON health from both host and emulator (using Android toybox `nc`), and prints HTTP statuses for navigation, catalog and access session upstream routes. HTTP 503 means Edge is reachable but its upstream is unavailable. Host liveness and upstream success are distinct.

The smoke command proves welcome → sign in → recovery → sign in → welcome → create access → welcome UI responsiveness, plus APK/runtime/gateway checks. It does not assert authenticated business behavior without a live Accessing upstream. To require upstream success, use `sandbox.sh verify-upstream`; it returns nonzero for any unavailable route. The existing Edge live checks can also run with `MOBILE_EDGE_SMOKE_ORIGIN=http://127.0.0.1:18080 npm --prefix mobile-edge run smoke:local` once the upstream runtime is configured.

Optional existing-account login: export `MOBILING_LOGIN_EMAIL` and `MOBILING_LOGIN_PASSWORD`, then run `sandbox.sh ui login`. It verifies `Mobile dashboard`; it does not create accounts. The helper supports simple ASCII credentials only. Avoid capturing sign-in forms containing secrets. Current access tests are UI interactions: instrumentation class/flavor packages from earlier revisions are absent from the current canonical client, on both platforms.

## Evidence and environment

The shared Windows/Ubuntu capture contract is retained: `watchdog/runtime/device/latest.png`, `current-ui.xml`, `current-window.txt`, and timestamped PNG/XML pairs under `history/`. The whole generated directory is Git-ignored. `capture NAME` validates both PNG and XML; `tap` performs a tap then captures. `ui tap-text 'Sign in'` uses current UI bounds and clickable ancestors rather than fixed coordinates. Capture failure returns nonzero.

`tools/android/snapshot.sh` writes ignored `.codebase-memory/environments/linux/environment-snapshot.json` and `.md`, identifying Linux, architecture, Java/SDK paths and versions, devices, KVM and Node/npm with seven-day expiry. Windows collects independently with `tools/android/snapshot.ps1` into `.codebase-memory/environments/windows/`. Detect the host OS first, then refresh only that platform’s snapshot when missing, inconsistent or older than seven days. The Windows reference in `AGENTS.md` represents a supported configuration, not active-host truth. No SDK, APK, userdata, credentials, screenshots or machine-local paths should be committed.

## Troubleshooting

- Missing `/dev/kvm`: enable VT-x/AMD-V in firmware. In a VM, expose CPU virtualization and enable nested KVM on the outer host; userspace scripts cannot provide it.
- Permission denied: `sudo usermod -aG kvm "$USER"`, then log out/in. A valid user ACL also suffices. `emulator -accel-check` must succeed.
- Offline adb or boot timeout: inspect `sandbox.sh diagnose` and `.codebase-memory/android/emulator.log`; use `recover` for the owned AVD. Do not delete userdata or kill unrelated emulator processes. If adb server itself is stale, `adb kill-server; adb start-server` affects all devices and should be used deliberately.
- Port occupied: select a free `MOBILING_EDGE_PORT` and rebuild Android; emulator port 5554/5555 conflicts fail explicitly. Do not stop another host service to make room.
- Low memory: close other applications; budget RAM and swap for both the emulator and Gradle. Avoid concurrent heavy builds. `run` keeps Gradle's workers/heap constrained.
- SDK/network download failure: re-run setup; partial archives are temporary and checksum checks precede extraction. The pinned Java-17-compatible command-line tools come from [Android's official SDK distribution](https://developer.android.com/studio#command-line-tools-only); user-local JDK downloads use [Adoptium's API](https://adoptium.net/installation/ci-scripts/).
