[CmdletBinding()]
param(
    [ValidateSet('Diagnose', 'Create', 'Retune', 'Start', 'Debloat', 'InstallShortcuts')]
    [string] $Action = 'Diagnose',

    [string] $AvdName = 'Mobiling_Light_API_34'
)

$ErrorActionPreference = 'Stop'

$sdkRoot = if ($env:ANDROID_SDK_ROOT) {
    $env:ANDROID_SDK_ROOT
} elseif ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} else {
    Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}

$emulator = Join-Path $sdkRoot 'emulator\emulator.exe'
$adb = Join-Path $sdkRoot 'platform-tools\adb.exe'
$avdManager = Join-Path $sdkRoot 'cmdline-tools\latest\bin\avdmanager.bat'
$sdkManager = Join-Path $sdkRoot 'cmdline-tools\latest\bin\sdkmanager.bat'
$systemImage = 'system-images;android-34;google_apis;x86_64'
$avdHome = if ($env:ANDROID_AVD_HOME) { $env:ANDROID_AVD_HOME } else { Join-Path $env:USERPROFILE '.android\avd' }
$configPath = Join-Path $avdHome "$AvdName.avd\config.ini"
$windowPlacementPath = Join-Path $avdHome "$AvdName.window.json"

Add-Type -AssemblyName System.Windows.Forms
Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class MobilingWindowApi {
    [StructLayout(LayoutKind.Sequential)]
    public struct RECT { public int Left; public int Top; public int Right; public int Bottom; }
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);
    [DllImport("user32.dll")] public static extern bool SetWindowPos(IntPtr hWnd, IntPtr insertAfter, int x, int y, int width, int height, uint flags);
}
'@

foreach ($required in @($emulator, $adb, $avdManager, $sdkManager)) {
    if (-not (Test-Path $required)) {
        throw "Required Android SDK tool was not found: $required"
    }
}

function Show-Diagnostics {
    Write-Host "Android SDK: $sdkRoot"
    Write-Host "AVD name:    $AvdName"
    Write-Host ''
    & $emulator -accel-check
    Write-Host ''
    Write-Host 'Installed AVDs:'
    & $emulator -list-avds
    Write-Host ''
    Write-Host 'Connected devices:'
    & $adb devices -l
}

function Set-ConfigValue {
    param(
        [Parameter(Mandatory)] [string] $Key,
        [Parameter(Mandatory)] [string] $Value
    )

    $content = Get-Content $configPath
    $pattern = "^$([regex]::Escape($Key))="

    if ($content -match $pattern) {
        $content = $content | ForEach-Object {
            if ($_ -match $pattern) { "$Key=$Value" } else { $_ }
        }
    } else {
        $content += "$Key=$Value"
    }

    Set-Content -Path $configPath -Value $content -Encoding ascii
}

function Find-EmulatorWindowProcess {
    Get-Process | Where-Object {
        $_.MainWindowHandle -ne 0 -and (
            $_.MainWindowTitle -like "*$AvdName*" -or
            $_.MainWindowTitle -like "*Android Emulator*"
        )
    } | Select-Object -First 1
}

function Save-EmulatorWindowPlacement {
    $process = Find-EmulatorWindowProcess
    if ($null -eq $process) { return }

    $rect = New-Object MobilingWindowApi+RECT
    if ([MobilingWindowApi]::GetWindowRect($process.MainWindowHandle, [ref] $rect)) {
        [ordered]@{
            x = $rect.Left
            y = $rect.Top
            width = $rect.Right - $rect.Left
            height = $rect.Bottom - $rect.Top
        } | ConvertTo-Json | Set-Content -Path $windowPlacementPath -Encoding utf8
    }
}

function Restore-EmulatorWindowPlacement {
    $process = $null
    for ($attempt = 0; $attempt -lt 30 -and $null -eq $process; $attempt++) {
        Start-Sleep -Milliseconds 500
        $process = Find-EmulatorWindowProcess
    }
    if ($null -eq $process) { return }

    if (Test-Path $windowPlacementPath) {
        $placement = Get-Content $windowPlacementPath -Raw | ConvertFrom-Json
    } else {
        $screens = [System.Windows.Forms.Screen]::AllScreens
        $target = if ($screens.Count -gt 1) { $screens[1].WorkingArea } else { $screens[0].WorkingArea }
        $placement = [pscustomobject]@{
            x = $target.Left + 20
            y = $target.Top + 20
            width = [Math]::Min(600, $target.Width - 40)
            height = [Math]::Min(1080, $target.Height - 40)
        }
    }

    [void] [MobilingWindowApi]::SetWindowPos(
        $process.MainWindowHandle,
        [IntPtr]::Zero,
        [int] $placement.x,
        [int] $placement.y,
        [int] $placement.width,
        [int] $placement.height,
        0x0040
    )
}

function New-LightAvd {
    $existing = @(& $emulator -list-avds)
    if ($existing -contains $AvdName) {
        Write-Host "AVD '$AvdName' already exists; preserving it."
        return
    }

    $installedPackages = & $sdkManager --list_installed
    if ($installedPackages -notmatch [regex]::Escape($systemImage)) {
        Write-Host "Installing $systemImage ..."
        & $sdkManager $systemImage
        if ($LASTEXITCODE -ne 0) {
            throw "sdkmanager failed with exit code $LASTEXITCODE"
        }
    }

    Write-Host "Creating AVD '$AvdName' without Google Play ..."
    'no' | & $avdManager create avd --name $AvdName --package $systemImage --device 'pixel_4a'
    if ($LASTEXITCODE -ne 0) {
        throw "avdmanager failed with exit code $LASTEXITCODE"
    }

    if (-not (Test-Path $configPath)) {
        throw "AVD was created but config.ini was not found: $configPath"
    }

    Set-ConfigValue 'hw.cpu.ncore' '2'
    Set-ConfigValue 'hw.ramSize' '4096'
    Set-ConfigValue 'vm.heapSize' '256'
    Set-ConfigValue 'hw.gpu.enabled' 'yes'
    Set-ConfigValue 'hw.gpu.mode' 'host'
    Set-ConfigValue 'hw.camera.back' 'none'
    Set-ConfigValue 'hw.camera.front' 'none'
    Set-ConfigValue 'hw.keyboard' 'yes'
    Set-ConfigValue 'hw.keyboard.lid' 'no'
    Set-ConfigValue 'hw.dPad' 'yes'
    Set-ConfigValue 'hw.mainKeys' 'yes'
    Set-ConfigValue 'hw.lcd.width' '1080'
    Set-ConfigValue 'hw.lcd.height' '1920'
    Set-ConfigValue 'hw.lcd.density' '320'
    Set-ConfigValue 'disk.dataPartition.size' '6G'
    Set-ConfigValue 'showDeviceFrame' 'no'
    Set-ConfigValue 'skin.dynamic' 'yes'

    Write-Host "Created and tuned '$AvdName'."
}

function Retune-LightAvd {
    if (-not (Test-Path $configPath)) {
        throw "AVD config.ini was not found: $configPath"
    }

    $runningDevices = @(
        & $adb devices |
            Select-Object -Skip 1 |
            Where-Object { $_ -match "\tdevice$" } |
            ForEach-Object { ($_ -split "\s+")[0] }
    )

    Save-EmulatorWindowPlacement

    foreach ($serial in ($runningDevices | Where-Object { $_ -like 'emulator-*' })) {
        $runningAvdName = ((& $adb -s $serial emu avd name 2>$null | Select-Object -First 1) | Out-String).Trim()
        if ($runningAvdName -eq $AvdName) {
            Write-Host "Stopping running AVD '$AvdName' before retuning ..."
            & $adb -s $serial emu kill | Out-Null
            Start-Sleep -Seconds 3
        }
    }

    Set-ConfigValue 'hw.ramSize' '4096'
    Set-ConfigValue 'hw.keyboard' 'yes'
    Set-ConfigValue 'hw.keyboard.lid' 'no'
    Set-ConfigValue 'hw.dPad' 'yes'
    Set-ConfigValue 'hw.mainKeys' 'yes'
    Set-ConfigValue 'hw.lcd.width' '1080'
    Set-ConfigValue 'hw.lcd.height' '1920'
    Set-ConfigValue 'hw.lcd.density' '320'
    Set-ConfigValue 'showDeviceFrame' 'no'
    Set-ConfigValue 'skin.dynamic' 'yes'

    Write-Host "Retuned '$AvdName' to 1080x1920 portrait at density 320 with 4 GB RAM and physical keyboard input enabled."
}

function Start-LightAvd {
    $existing = @(& $emulator -list-avds)
    if ($existing -notcontains $AvdName) {
        throw "AVD '$AvdName' does not exist. Run with -Action Create first."
    }

    Start-Process -FilePath $emulator -ArgumentList @("@$AvdName", '-gpu', 'host', '-scale', '1.0', '-use-keycode-forwarding', '-no-snapshot-load', '-no-boot-anim', '-netdelay', 'none', '-netspeed', 'full') -WorkingDirectory (Split-Path $emulator)

    Start-Sleep -Seconds 5
    $shell = New-Object -ComObject WScript.Shell
    [void] $shell.AppActivate($AvdName)
}

function Resolve-AvdSerial {
    $readyDevices = @(
        & $adb devices |
            Select-Object -Skip 1 |
            Where-Object { $_ -match "\tdevice$" } |
            ForEach-Object { ($_ -split "\s+")[0] }
    )

    foreach ($serial in ($readyDevices | Where-Object { $_ -like 'emulator-*' })) {
        $runningAvdName = ((& $adb -s $serial emu avd name 2>$null | Select-Object -First 1) | Out-String).Trim()
        if ($runningAvdName -eq $AvdName) {
            return $serial
        }
    }

    throw "AVD '$AvdName' is not running and ready."
}

function Remove-LightAvdBloat {
    $serial = Resolve-AvdSerial
    & $adb -s $serial wait-for-device

    $packages = @(
        'com.android.chrome',
        'com.google.android.apps.maps',
        'com.google.android.apps.messaging',
        'com.google.android.apps.photos',
        'com.google.android.apps.magazines',
        'com.google.android.calendar',
        'com.google.android.calculator',
        'com.google.android.contacts',
        'com.google.android.deskclock',
        'com.google.android.gm',
        'com.google.android.googlequicksearchbox',
        'com.google.android.videos',
        'com.google.android.youtube',
        'com.google.android.apps.tachyon',
        'com.google.android.apps.wellbeing'
    )

    $installed = @(& $adb -s $serial shell pm list packages | ForEach-Object { $_ -replace '^package:', '' })
    $changed = @()
    $missing = @()

    foreach ($packageName in $packages) {
        if ($installed -notcontains $packageName) {
            $missing += $packageName
            continue
        }

        & $adb -s $serial shell pm disable-user --user 0 $packageName | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Failed to disable package '$packageName'."
        }
        $changed += $packageName
    }

    & $adb -s $serial shell settings put global window_animation_scale 0
    & $adb -s $serial shell settings put global transition_animation_scale 0
    & $adb -s $serial shell settings put global animator_duration_scale 0
    & $adb -s $serial shell settings put global auto_update_system 0
    & $adb -s $serial shell settings put secure screensaver_enabled 0
    & $adb -s $serial shell settings put system screen_off_timeout 2147483647
    & $adb -s $serial shell am force-stop com.google.android.googlequicksearchbox 2>$null
    & $adb -s $serial shell am start -W -n 'app.mobiling.client/.MainActivity' | Out-Null

    $mobilingInstalled = (& $adb -s $serial shell pm path app.mobiling.client 2>$null) -match '^package:'
    $mobilingProcess = ((& $adb -s $serial shell pidof app.mobiling.client 2>$null) | Out-String).Trim()

    [ordered]@{
        ok = ($mobilingInstalled -and -not [string]::IsNullOrWhiteSpace($mobilingProcess))
        avdName = $AvdName
        deviceSerial = $serial
        disabledPackages = $changed
        absentPackages = $missing
        mobilingInstalled = $mobilingInstalled
        mobilingProcessId = $mobilingProcess
    } | ConvertTo-Json -Depth 4
}

function Install-DesktopShortcuts {
    $desktop = [Environment]::GetFolderPath('Desktop')
    $shell = New-Object -ComObject WScript.Shell
    $powershell = (Get-Command powershell.exe).Source
    $workspace = Split-Path $PSScriptRoot -Parent

    $emulatorShortcut = $shell.CreateShortcut((Join-Path $desktop '1tasker Emulator.lnk'))
    $emulatorShortcut.TargetPath = $powershell
    $emulatorShortcut.Arguments = "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`" -Action Start"
    $emulatorShortcut.WorkingDirectory = $workspace
    $emulatorShortcut.Description = 'Start the persistent 1tasker Android emulator.'
    $emulatorShortcut.IconLocation = "$emulator,0"
    $emulatorShortcut.Save()

    $appShortcut = $shell.CreateShortcut((Join-Path $desktop '1tasker Build and Launch.lnk'))
    $appShortcut.TargetPath = $powershell
    $appShortcut.Arguments = "-NoProfile -ExecutionPolicy Bypass -Command `"& '$workspace\tool\android-avd-light.ps1' -Action Start; Start-Sleep -Seconds 20; & '$workspace\tool\android-design-refresh.ps1' -ScreenshotName 'manual-inspection'`""
    $appShortcut.WorkingDirectory = $workspace
    $appShortcut.Description = 'Start the emulator, build, install, and launch 1tasker.'
    $appShortcut.IconLocation = "$emulator,0"
    $appShortcut.Save()

    Write-Host "Desktop shortcuts installed:"
    Write-Host "  $($emulatorShortcut.FullName)"
    Write-Host "  $($appShortcut.FullName)"
}

switch ($Action) {
    'Diagnose' { Show-Diagnostics }
    'Create' {
        Show-Diagnostics
        New-LightAvd
        Write-Host ''
        Show-Diagnostics
    }
    'Retune' { Retune-LightAvd }
    'Start' { Start-LightAvd }
    'Debloat' { Remove-LightAvdBloat }
    'InstallShortcuts' { Install-DesktopShortcuts }
}
