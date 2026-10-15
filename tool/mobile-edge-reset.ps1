[CmdletBinding()]
param(
    [int]$Port = 8080
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$edgeRoot = Join-Path $repositoryRoot "mobile-edge"
$npm = Get-Command npm.cmd -ErrorAction Stop

$listeners = @(
    Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique
)

foreach ($processId in $listeners) {
    Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
}

Start-Sleep -Seconds 2

$process = Start-Process `
    -FilePath $npm.Source `
    -ArgumentList @("run", "dev") `
    -WorkingDirectory $edgeRoot `
    -WindowStyle Hidden `
    -PassThru

$deadline = (Get-Date).AddSeconds(30)
$healthUrl = "http://127.0.0.1:$Port/health"
do {
    Start-Sleep -Milliseconds 500
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $healthUrl -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            [ordered]@{
                ok = $true
                parentProcessId = $process.Id
                port = $Port
                healthUrl = $healthUrl
            } | ConvertTo-Json
            exit 0
        }
    }
    catch {
    }
} while ((Get-Date) -lt $deadline)
