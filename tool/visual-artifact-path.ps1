Set-StrictMode -Version Latest

function New-VisualArtifactRun {
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [Parameter(Mandatory = $true)][ValidateSet('localhost-inspect','playwright','panther','mobile-ui')][string]$Producer,
        [Parameter(Mandatory = $true)][ValidateSet('web','android','ios')][string]$Platform,
        [ValidateSet('new-user','existing-user','unspecified')][string]$Cohort = 'unspecified',
        [string]$Scenario = '',
        [ValidateSet('GREEN','ATTENTION','NOT_VERIFIED')][string]$Status = 'NOT_VERIFIED',
        [int]$ConsoleErrors = 0,
        [int]$FailedRequests = 0,
        [string]$RunId = ''
    )

    $capturedAt = Get-Date
    $date = $capturedAt.ToString('yyyy-MM-dd')
    $component = Split-Path -Leaf $RepositoryRoot
    $workspaceRoot = Split-Path -Parent $RepositoryRoot
    $artifactRoot = Join-Path $workspaceRoot 'var'

    if ([string]::IsNullOrWhiteSpace($RunId)) {
        $RunId = 'run-' + $capturedAt.ToString('HH-mm-ss')
    }

    $safeRunId = ($RunId -replace '[^a-zA-Z0-9._-]', '-').Trim('-')
    if ([string]::IsNullOrWhiteSpace($safeRunId)) {
        $safeRunId = 'run-' + $capturedAt.ToString('HH-mm-ss')
    }

    $runRoot = Join-Path $artifactRoot (Join-Path $component (Join-Path $date $safeRunId))
    $screenshots = Join-Path $runRoot (Join-Path 'screenshots' (Join-Path $Platform $Cohort))
    $logs = Join-Path $runRoot 'logs'
    $todayRoot = Join-Path $artifactRoot (Join-Path $component 'today')

    New-Item -ItemType Directory -Force -Path $screenshots | Out-Null
    New-Item -ItemType Directory -Force -Path $logs | Out-Null
    New-Item -ItemType Directory -Force -Path $todayRoot | Out-Null

    [ordered]@{
        schema = 'visual-artifact-run-v1'
        component = $component
        date = $date
        run_id = $safeRunId
        producer = $Producer
        platform = $Platform
        cohort = $Cohort
        scenario = $(if ([string]::IsNullOrWhiteSpace($Scenario)) { $null } else { $Scenario })
        task_id = $null
        captured_at = $capturedAt.ToString('o')
        status = $Status
        console_errors = [math]::Max(0, $ConsoleErrors)
        failed_requests = [math]::Max(0, $FailedRequests)
        screenshots_dir = "screenshots/$Platform/$Cohort"
        logs_dir = 'logs'
    } | ConvertTo-Json -Depth 4 | Set-Content -Encoding utf8 (Join-Path $runRoot 'manifest.json')

    [ordered]@{
        date = $date
        run_id = $safeRunId
        target = "../$date/$safeRunId"
    } | ConvertTo-Json -Depth 3 | Set-Content -Encoding utf8 (Join-Path $todayRoot 'manifest.json')

    return [pscustomobject]@{
        Root = $runRoot
        Screenshots = $screenshots
        Logs = $logs
        Today = $todayRoot
        RunId = $safeRunId
        Date = $date
        Component = $component
    }
}

function Set-VisualArtifactRunStatus {
    param(
        [Parameter(Mandatory = $true)]$ArtifactRun,
        [Parameter(Mandatory = $true)][ValidateSet('GREEN','ATTENTION','NOT_VERIFIED')][string]$Status,
        [int]$ConsoleErrors = 0,
        [int]$FailedRequests = 0
    )

    $manifestPath = Join-Path $ArtifactRun.Root 'manifest.json'
    if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
        throw "Visual artifact manifest was not found: $manifestPath"
    }
    $manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json -Depth 8
    $manifest.status = $Status
    $manifest.console_errors = [math]::Max(0, $ConsoleErrors)
    $manifest.failed_requests = [math]::Max(0, $FailedRequests)
    $manifest | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $manifestPath -Encoding utf8
}
