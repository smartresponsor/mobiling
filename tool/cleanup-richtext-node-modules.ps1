Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$nodeModules = Join-Path $repositoryRoot 'client/richtext-web/node_modules'

if (Test-Path -LiteralPath $nodeModules) {
    Remove-Item -LiteralPath $nodeModules -Recurse -Force
}
