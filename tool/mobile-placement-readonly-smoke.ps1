[CmdletBinding()]
param(
    [string]$AccessToken = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not $AccessToken) {
    $AccessToken = [string]$env:MOBILING_ACCESS_TOKEN
}
if (-not $AccessToken) {
    throw "Read-only placement smoke requires an existing bearer token via -AccessToken or MOBILING_ACCESS_TOKEN."
}

function Invoke-JsonRequest {
    param(
        [Parameter(Mandatory = $true)][string]$Uri,
        [hashtable]$Headers = @{}
    )

    $response = Invoke-WebRequest -Method GET -Uri $Uri -Headers $Headers -ContentType "application/json" -UseBasicParsing
    if ([int]$response.StatusCode -lt 200 -or [int]$response.StatusCode -ge 300) {
        throw "Read-only placement smoke request failed with HTTP $($response.StatusCode): $Uri"
    }

    if (-not $response.Content) { return $null }
    return $response.Content | ConvertFrom-Json
}

$headers = @{ Authorization = "Bearer $AccessToken"; Accept = "application/json" }
$session = Invoke-JsonRequest -Uri "http://127.0.0.1:8080/access/session" -Headers $headers
if ([string]$session.status -ne "authenticated") {
    throw "Mobile-edge bearer session is not authenticated."
}

$retail = Invoke-JsonRequest -Uri "http://127.0.0.1:8080/my/retail" -Headers $headers
$items = @($retail.items)
$candidate = $items | Where-Object {
    $identity = if ($_.id) { [string]$_.id } elseif ($_.retailId) { [string]$_.retailId } else { "" }
    $identity -match '^[1-9][0-9]*$'
} | Select-Object -First 1

if ($null -eq $candidate) {
    throw "Authenticated account has no existing numeric Retail listing for read-only placement verification."
}

$retailId = if ($candidate.id) { [string]$candidate.id } else { [string]$candidate.retailId }
$placement = Invoke-JsonRequest -Uri "http://127.0.0.1:8080/retail/$retailId/placement" -Headers $headers
