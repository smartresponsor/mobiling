[CmdletBinding()]
param(
    [string] $DeviceSerial = 'emulator-5554',
    [string] $AvdName = 'Mobiling_Light_API_34'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$avdHome = if ($env:ANDROID_AVD_HOME) { $env:ANDROID_AVD_HOME } else { Join-Path $env:USERPROFILE '.android\avd' }
$configPath = Join-Path $avdHome "$AvdName.avd\config.ini"

if (-not (Test-Path $configPath)) {
    throw "AVD config was not found: $configPath"
}

function Set-ConfigValue {
    param([string] $Key, [string] $Value)
    $content = @(Get-Content $configPath)
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

Set-ConfigValue 'hw.keyboard' 'yes'
Set-ConfigValue 'hw.keyboard.lid' 'no'
Set-ConfigValue 'keyboard.charmap' 'qwerty2'
Set-ConfigValue 'hw.dPad' 'yes'

& adb -s $DeviceSerial wait-for-device
& adb -s $DeviceSerial shell settings put secure show_ime_with_hard_keyboard 1
& adb -s $DeviceSerial shell settings put system pointer_speed 0

$keyboardLines = @(
    & adb -s $DeviceSerial shell dumpsys input |
        Select-String -Pattern 'Keyboard|keyboard|qwerty|Virtual' |
        Select-Object -First 30 |
        ForEach-Object { $_.Line.Trim() }
)

Write-Host 'Keyboard AVD configuration enabled.'
Write-Host "device=$DeviceSerial"
Write-Host "config=$configPath"
$keyboardLines | ForEach-Object { Write-Host $_ }
