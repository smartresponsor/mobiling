[CmdletBinding()]
param(
    [string] $DeviceSerial = 'emulator-5554',
    [string] $Login = '',
    [string] $Password = '',
    [string] $PackageName = 'app.mobiling.client',
    [string] $ActivityName = '.MainActivity'
)

$ErrorActionPreference = 'Stop'

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments)] [string[]] $Arguments)
    & adb -s $DeviceSerial @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($Arguments -join ' ')"
    }
}

function Get-Hierarchy {
    $remote = '/sdcard/window.xml'
    Invoke-Adb shell uiautomator dump $remote | Out-Null
    $xml = (& adb -s $DeviceSerial shell cat $remote | Out-String)
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($xml)) {
        throw 'Could not read Android UI hierarchy.'
    }
    [xml] $xml
}

function Find-Node {
    param(
        [Parameter(Mandatory)] [xml] $Hierarchy,
        [string] $Text,
        [string] $ResourceId,
        [switch] $Clickable
    )

    $nodes = @($Hierarchy.SelectNodes('//node'))
    foreach ($node in $nodes) {
        if ($Text -and $node.text -ne $Text -and $node.'content-desc' -ne $Text) { continue }
        if ($ResourceId -and $node.'resource-id' -notlike "*$ResourceId*") { continue }
        if ($Clickable) {
            $clickTarget = $node
            while ($null -ne $clickTarget -and $clickTarget.Name -eq 'node' -and $clickTarget.clickable -ne 'true') {
                $clickTarget = $clickTarget.ParentNode
            }
            if ($null -eq $clickTarget -or $clickTarget.Name -ne 'node' -or $clickTarget.clickable -ne 'true') { continue }
        }
        return $node
    }
    return $null
}

function Find-ClickableAncestorByText {
    param(
        [Parameter(Mandatory)] [xml] $Hierarchy,
        [Parameter(Mandatory)] [string] $Text
    )

    foreach ($node in @($Hierarchy.SelectNodes('//node[@text="' + $Text + '"]'))) {
        $target = $node
        while ($null -ne $target -and $target.Name -eq 'node') {
            if ($target.clickable -eq 'true') { return $target }
            $target = $target.ParentNode
        }
    }

    return $null
}

function Get-Center {
    param([Parameter(Mandatory)] $Node)
    if ($Node.bounds -notmatch '^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$') {
        throw "Invalid node bounds: $($Node.bounds)"
    }
    @(
        [int](([int]$matches[1] + [int]$matches[3]) / 2),
        [int](([int]$matches[2] + [int]$matches[4]) / 2)
    )
}

function Tap-Node {
    param([Parameter(Mandatory)] $Node)
    $target = $Node
    while ($null -ne $target.ParentNode -and $target.clickable -ne 'true') {
        $target = $target.ParentNode
        if ($target.Name -ne 'node') { break }
    }
    if ($target.Name -ne 'node') { $target = $Node }
    $center = Get-Center $target
    & adb -s $DeviceSerial shell input tap $center[0] $center[1] | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Android tap command failed.' }
    Start-Sleep -Milliseconds 1000
}

function Clear-Field {
    Invoke-Adb shell input keyevent KEYCODE_MOVE_END | Out-Null
    for ($index = 0; $index -lt 200; $index++) {
        Invoke-Adb shell input keyevent KEYCODE_DEL | Out-Null
    }
}

function Encode-InputText {
    param([Parameter(Mandatory)] [string] $Value)
    $builder = New-Object System.Text.StringBuilder
    foreach ($character in $Value.ToCharArray()) {
        switch ($character) {
            ' ' { [void] $builder.Append('%s') }
            '&' { [void] $builder.Append('\&') }
            '<' { [void] $builder.Append('\<') }
            '>' { [void] $builder.Append('\>') }
            '|' { [void] $builder.Append('\|') }
            ';' { [void] $builder.Append('\;') }
            '(' { [void] $builder.Append('\(') }
            ')' { [void] $builder.Append('\)') }
            '$' { [void] $builder.Append('\$') }
            '!' { [void] $builder.Append('\!') }
            default { [void] $builder.Append($character) }
        }
    }
    $builder.ToString()
}

& adb -s $DeviceSerial shell am force-stop $PackageName | Out-Null
& adb -s $DeviceSerial shell am start -W -n "$PackageName/$ActivityName" | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Could not launch the Android application.' }
Start-Sleep -Seconds 2
$processId = ((& adb -s $DeviceSerial shell pidof $PackageName 2>$null) | Out-String).Trim()
$currentFocus = ((& adb -s $DeviceSerial shell dumpsys window windows 2>$null | Select-String 'mCurrentFocus|mFocusedApp' | Select-Object -First 2) | Out-String).Trim()
if ([string]::IsNullOrWhiteSpace($processId)) {
    $crashLog = ((& adb -s $DeviceSerial logcat -d -t 120 2>$null | Select-String $PackageName) | Out-String).Trim()
    throw "Mobiling process is not running after launch. Focus: $currentFocus`n$crashLog"
}

$hierarchy = Get-Hierarchy
$diagnosticRoot = Join-Path (Split-Path $PSScriptRoot -Parent) 'watchdog\runtime\device'
New-Item -ItemType Directory -Force -Path $diagnosticRoot | Out-Null
$hierarchy.Save((Join-Path $diagnosticRoot 'current-ui.xml'))
$signInEntry = Find-ClickableAncestorByText -Hierarchy $hierarchy -Text 'Sign in'
if ($null -ne $signInEntry) {
    Tap-Node $signInEntry
    $hierarchy = Get-Hierarchy
}

$emailNode = Find-Node -Hierarchy $hierarchy -ResourceId 'access-sign-in-email'
if ($null -eq $emailNode) { $emailNode = Find-Node -Hierarchy $hierarchy -Text 'Email' }
if ($null -eq $emailNode) {
    $editFields = @($hierarchy.SelectNodes('//node[contains(@class,"EditText")]'))
    if ($editFields.Count -gt 0) { $emailNode = $editFields[0] }
}
if ($null -eq $emailNode) {
    $visible = @($hierarchy.SelectNodes('//node[@text!=""]') | ForEach-Object { $_.text } | Select-Object -Unique)
    throw "Email field was not found. Visible text: $($visible -join ' | ')"
}
if (-not [string]::IsNullOrWhiteSpace($Login)) {
    Tap-Node $emailNode
    Clear-Field
    Invoke-Adb shell input text (Encode-InputText $Login) | Out-Null
}

$hierarchy = Get-Hierarchy
$passwordNode = Find-Node -Hierarchy $hierarchy -ResourceId 'access-sign-in-password'
if ($null -eq $passwordNode) { $passwordNode = Find-Node -Hierarchy $hierarchy -Text 'Password' }
if ($null -eq $passwordNode) {
    $editFields = @($hierarchy.SelectNodes('//node[contains(@class,"EditText")]'))
    if ($editFields.Count -gt 1) { $passwordNode = $editFields[1] }
}
if ($null -eq $passwordNode) { throw 'Password field was not found.' }
if (-not [string]::IsNullOrWhiteSpace($Password)) {
    Tap-Node $passwordNode
    Clear-Field
    Invoke-Adb shell input text (Encode-InputText $Password) | Out-Null
}

$hierarchy = Get-Hierarchy
$submit = Find-ClickableAncestorByText -Hierarchy $hierarchy -Text 'Sign in'
if ($null -eq $submit) { throw 'Sign in submit button was not found.' }
Tap-Node $submit
Start-Sleep -Seconds 5

$runtimeRoot = Join-Path (Split-Path $PSScriptRoot -Parent) 'watchdog\runtime\device'
New-Item -ItemType Directory -Force -Path $runtimeRoot | Out-Null
$screenshot = Join-Path $runtimeRoot 'after-login.png'
& adb -s $DeviceSerial exec-out screencap -p > $screenshot

$finalHierarchy = Get-Hierarchy
[ordered]@{
    ok = $true
    deviceSerial = $DeviceSerial
    screenshot = $screenshot
    visibleTexts = @($finalHierarchy.SelectNodes('//node[@text!=""]') | ForEach-Object { $_.text } | Select-Object -Unique)
} | ConvertTo-Json -Depth 5
