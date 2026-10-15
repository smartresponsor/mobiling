[CmdletBinding()]
param(
    [string] $AvdName = 'Mobiling_Light_API_34'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$serial = $null
for ($attempt = 0; $attempt -lt 60; $attempt++) {
    foreach ($candidate in @(& adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" } | ForEach-Object { ($_ -split "\s+")[0] })) {
        $name = ((& adb -s $candidate emu avd name 2>$null | Select-Object -First 1) | Out-String).Trim()
        if ($name -eq $AvdName) {
            $serial = $candidate
            break
        }
    }
    if ($null -ne $serial) { break }
    Start-Sleep -Seconds 2
}

if ($null -eq $serial) { throw "AVD '$AvdName' did not become ready." }

for ($attempt = 0; $attempt -lt 120; $attempt++) {
    $bootCompleted = ((& adb -s $serial shell getprop sys.boot_completed 2>$null) | Out-String).Trim()
    if ($bootCompleted -eq '1') { break }
    Start-Sleep -Seconds 2
}

if ($bootCompleted -ne '1') { throw "AVD '$AvdName' did not complete Android boot." }

& adb -s $serial shell wm size
& adb -s $serial shell wm density
& adb -s $serial shell am start -W -n 'app.mobiling.client/.MainActivity'
if ($LASTEXITCODE -ne 0) { throw 'Failed to launch Mobiling.' }

Write-Host "device=$serial"
