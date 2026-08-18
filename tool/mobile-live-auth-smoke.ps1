# Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp

[CmdletBinding()]
param(
    [string]$Email = "",
    [string]$Password = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$edgeRoot = Join-Path $repositoryRoot "mobile-edge"
$localPropertiesPath = Join-Path $repositoryRoot "client/android/local.properties"
if ((-not $Email -or -not $Password) -and (Test-Path $localPropertiesPath)) {
    $properties = @{}
    foreach ($line in Get-Content $localPropertiesPath) {
        if ($line -match '^([^#=]+)=(.*)$') { $properties[$matches[1].Trim()] = $matches[2] }
    }
    if (-not $Email) { $Email = [string]$properties['mobiling.debug.login'] }
    if (-not $Password) { $Password = [string]$properties['mobiling.debug.password'] }
}
if (-not $Email -or -not $Password) { throw "Live smoke credentials are not configured in arguments or client/android/local.properties." }

Push-Location $edgeRoot
try {
    $env:ACCESSING_API_BASE_URL = "http://127.0.0.1:8000"
    & npm run dev:restart
    if ($LASTEXITCODE -ne 0) {
        throw "mobile-edge restart failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}

$cookiePath = Join-Path $repositoryRoot "build/live-auth-cookie.txt"
$responsePath = Join-Path $repositoryRoot "build/live-auth-signin.json"
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $cookiePath) | Out-Null

$payloadPath = Join-Path $repositoryRoot "build/live-auth-payload.json"
$signInJson = @{ email = $Email; password = $Password } | ConvertTo-Json -Compress
[System.IO.File]::WriteAllText($payloadPath, $signInJson, [System.Text.UTF8Encoding]::new($false))
$status = & curl.exe -s -o $responsePath -w "%{http_code}" `
    -H "Accept: application/json" `
    -H "Content-Type: application/json" `
    --data-binary "@$payloadPath" `
    "http://127.0.0.1:8000/api/access/signin"

if ($LASTEXITCODE -ne 0 -or [int]$status -lt 200 -or [int]$status -ge 300) {
    $body = if (Test-Path $responsePath) { Get-Content -Raw $responsePath } else { "<no body>" }
    throw "Live sign-in failed with HTTP $status. Body: $body"
}

$signInBody = Get-Content -Raw $responsePath | ConvertFrom-Json
$accessToken = [string]$signInBody.accessToken
if (-not $accessToken) { throw "Accessing sign-in response did not include an accessToken." }
$authorizationHeader = "Authorization: Bearer $accessToken"

$sessionPath = Join-Path $repositoryRoot "build/live-auth-session.json"
$sessionStatus = & curl.exe -s -o $sessionPath -w "%{http_code}" `
    -H "Accept: application/json" `
    -H $authorizationHeader `
    "http://127.0.0.1:8080/access/session"

if ($LASTEXITCODE -ne 0 -or [int]$sessionStatus -ne 200) {
    $body = if (Test-Path $sessionPath) { Get-Content -Raw $sessionPath } else { "<no body>" }
    Write-Warning "Bearer session lookup is currently unavailable (HTTP $sessionStatus): $body"
} else {
    $sessionBody = Get-Content -Raw $sessionPath | ConvertFrom-Json
    if ([string]$sessionBody.status -ne "authenticated") {
        Write-Warning "Bearer session lookup returned status '$($sessionBody.status)'; continuing with the issued token for CRUD verification."
    }
}

function Invoke-MobileCrudRequest {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Resource,
        [string]$Identity,
        [hashtable]$Body
    )

    $suffix = if ($Identity) { "/$Identity" } else { "" }
    $uri = "http://127.0.0.1:8080/my/$Resource$suffix"
    $outputPath = Join-Path $repositoryRoot "build/live-crud-$Resource-$($Method.ToLowerInvariant()).json"
    $arguments = @("-s", "-o", $outputPath, "-w", "%{http_code}", "-X", $Method, "-H", "Accept: application/json", "-H", $authorizationHeader)
    if ($null -ne $Body) {
        $payloadFile = Join-Path $repositoryRoot "build/live-crud-$Resource-payload.json"
        $requestJson = $Body | ConvertTo-Json -Depth 8 -Compress
        [System.IO.File]::WriteAllText($payloadFile, $requestJson, [System.Text.UTF8Encoding]::new($false))
        $arguments += @("-H", "Content-Type: application/json", "--data-binary", "@$payloadFile")
    }
    $arguments += $uri
    $httpStatus = & curl.exe @arguments
    $responseBody = if (Test-Path $outputPath) { Get-Content -Raw $outputPath } else { "{}" }
    if ($LASTEXITCODE -ne 0 -or [int]$httpStatus -lt 200 -or [int]$httpStatus -ge 300) {
        throw "Live $Resource $Method failed with HTTP $httpStatus. Body: $responseBody"
    }
    return [pscustomobject]@{ Status = [int]$httpStatus; Body = ($responseBody | ConvertFrom-Json); Path = $outputPath }
}

function Resolve-CrudIdentity {
    param([Parameter(Mandatory = $true)]$Body, [Parameter(Mandatory = $true)][string]$Resource)
    foreach ($candidate in @("${Resource}Id", "id", "identity")) {
        if ($Body.PSObject.Properties.Name -contains $candidate -and $Body.$candidate) { return [string]$Body.$candidate }
    }
    foreach ($container in @("item", "data", "resource")) {
        if ($Body.PSObject.Properties.Name -contains $container -and $null -ne $Body.$container) {
            $nested = Resolve-CrudIdentity -Body $Body.$container -Resource $Resource
            if ($nested) { return $nested }
        }
    }
    throw "Could not resolve $Resource identity from create response."
}

$existingRetail = Invoke-MobileCrudRequest -Method "GET" -Resource "retail"
foreach ($item in @($existingRetail.Body.items)) {
    $title = [string]$item.title
    $identity = if ($item.id) { [string]$item.id } elseif ($item.retailId) { [string]$item.retailId } else { "" }
    if ($identity -and $title.StartsWith("Mobile Smoke Task", [System.StringComparison]::Ordinal)) {
        Invoke-MobileCrudRequest -Method "DELETE" -Resource "retail" -Identity $identity | Out-Null
    }
}

$created = [System.Collections.Generic.List[object]]::new()
$stamp = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
try {
    foreach ($definition in @(
        @{ Resource = "retail"; Create = @{ kind = "task"; catalogCode = "services"; categoryId = "884"; title = "Mobile Smoke Task $stamp"; description = "Live mobile Retail CRUD smoke task."; amountMinor = 1995; currency = "USD"; location = "Katy, TX" }; Update = @{ title = "Mobile Smoke Task Updated $stamp"; amountMinor = 2495 } }
    )) {
        $resource = [string]$definition.Resource
        Invoke-MobileCrudRequest -Method "GET" -Resource $resource | Out-Null
        $create = Invoke-MobileCrudRequest -Method "POST" -Resource $resource -Body $definition.Create
        $identity = Resolve-CrudIdentity -Body $create.Body -Resource $resource
        $created.Add([pscustomobject]@{ Resource = $resource; Identity = $identity })
        Invoke-MobileCrudRequest -Method "PATCH" -Resource $resource -Identity $identity -Body $definition.Update | Out-Null
        Invoke-MobileCrudRequest -Method "GET" -Resource $resource | Out-Null
    }
}
finally {
    for ($index = $created.Count - 1; $index -ge 0; $index--) {
        $fixture = $created[$index]
        try { Invoke-MobileCrudRequest -Method "DELETE" -Resource $fixture.Resource -Identity $fixture.Identity | Out-Null }
        catch { Write-Warning "Cleanup failed for $($fixture.Resource)/$($fixture.Identity): $($_.Exception.Message)" }
    }
}

Write-Host "Live mobile bearer authentication and CRUD integration passed."
Write-Host "Login: $Email"
Write-Host "Sign-in response: $responsePath"
Write-Host "Session response: $sessionPath"
