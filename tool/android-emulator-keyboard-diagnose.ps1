[CmdletBinding()]
param(
    [string] $AvdName = 'Mobiling_Light_API_34'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$sdkRoot = if ($env:ANDROID_SDK_ROOT) {
    $env:ANDROID_SDK_ROOT
} elseif ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} else {
    Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}

$emulator = Join-Path $sdkRoot 'emulator\emulator.exe'
$avdHome = if ($env:ANDROID_AVD_HOME) { $env:ANDROID_AVD_HOME } else { Join-Path $env:USERPROFILE '.android\avd' }
$avdDir = Join-Path $avdHome "$AvdName.avd"

Write-Host 'Emulator version:'
& $emulator -version

Write-Host ''
Write-Host 'Keyboard-related emulator options:'
& $emulator -help 2>&1 |
    Select-String -Pattern 'keyboard|keycode|char' -CaseSensitive:$false |
    ForEach-Object { $_.Line }

Write-Host ''
Write-Host 'AVD keyboard configuration:'
Get-Content (Join-Path $avdDir 'config.ini') |
    Select-String -Pattern 'keyboard|dpad|charmap' -CaseSensitive:$false |
    ForEach-Object { $_.Line }

Write-Host ''
Write-Host 'Emulator UI state files:'
