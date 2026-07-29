[CmdletBinding()]
param(
    [string] $DeviceSerial = 'emulator-5554',
    [string] $PackageName = 'app.mobiling.client',
    [string] $ActivityName = '.MainActivity'
)

$ErrorActionPreference = 'Stop'

& adb -s $DeviceSerial shell am start -W -n "$PackageName/$ActivityName" | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Could not launch the Android application.' }
Start-Sleep -Seconds 1

$remote = '/sdcard/window.xml'
& adb -s $DeviceSerial shell uiautomator dump $remote | Out-Null
[xml] $hierarchy = (& adb -s $DeviceSerial shell cat $remote | Out-String)
$signIn = @($hierarchy.SelectNodes('//node[@text="Sign in"]')) | Select-Object -First 1
if ($null -eq $signIn) {
    $texts = @($hierarchy.SelectNodes('//node[@text!=""]') | ForEach-Object { $_.text } | Select-Object -Unique)
    [ordered]@{ ok = $true; alreadyPastWelcome = $true; visibleTexts = $texts } | ConvertTo-Json -Depth 4
    exit 0
}

$target = $signIn
while ($null -ne $target.ParentNode -and $target.clickable -ne 'true') {
    $target = $target.ParentNode
    if ($target.Name -ne 'node') { break }
}
if ($target.Name -ne 'node') { $target = $signIn }

if ($target.bounds -notmatch '^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$') { throw 'Invalid sign-in bounds.' }
$x = [int](([int]$matches[1] + [int]$matches[3]) / 2)
$y = [int](([int]$matches[2] + [int]$matches[4]) / 2)
& adb -s $DeviceSerial shell input tap $x $y | Out-Null
Start-Sleep -Seconds 1

& adb -s $DeviceSerial shell uiautomator dump $remote | Out-Null
[xml] $final = (& adb -s $DeviceSerial shell cat $remote | Out-String)
$texts = @($final.SelectNodes('//node[@text!=""]') | ForEach-Object { $_.text } | Select-Object -Unique)
[ordered]@{ ok = $true; visibleTexts = $texts } | ConvertTo-Json -Depth 4
