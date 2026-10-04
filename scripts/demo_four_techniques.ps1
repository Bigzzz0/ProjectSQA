param([switch]$ShowRunnerOutput)

$ErrorActionPreference = 'Stop'

$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Set-Location -LiteralPath $repoRoot

$containerName = 'defects4j_sqa'
$sessionId = (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 6)
$startedAt = Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz'
$reportDir = Join-Path $repoRoot ".local/demo/$sessionId"
$demoResults = @()
$lastRunIds = @{}
$techniqueLabels = @{ ipo = 'Native IPO'; mio = 'MIO'; deepseek = 'DeepSeek'; gemini = 'Gemini' }
$demoCsv = "/tmp/project_sqa_demo_$([guid]::NewGuid().ToString('N')).csv"
$backupDir = Join-Path ([System.IO.Path]::GetTempPath()) "project-sqa-demo-$([guid]::NewGuid().ToString('N'))"
$relativeFiles = @(
    'progress.json',
    'results/Jsoup/40/ipo.json',
    'results/Jsoup/40/mio.json',
    'results/Jsoup/40/deepseek.json',
    'results/Jsoup/40/gemini.json'
)
$cases = @(
    @{ Project = 'Jsoup'; Bug = 40; Technique = 'ipo' },
    @{ Project = 'Jsoup'; Bug = 40; Technique = 'mio' },
    @{ Project = 'Jsoup'; Bug = 40; Technique = 'deepseek' },
    @{ Project = 'Jsoup'; Bug = 40; Technique = 'gemini' }
)
$expectedTarget = 'org.jsoup.nodes.DocumentType'

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Engine is unavailable. Start Docker Desktop, then run this script again.'
}

$containerRunning = docker inspect --format '{{.State.Running}}' $containerName 2>$null
if ($LASTEXITCODE -ne 0 -or $containerRunning -ne 'true') {
    throw 'The defects4j_sqa container is not running. Start it with docker compose -f docker/docker-compose.yml up -d.'
}

New-Item -ItemType Directory -Path $backupDir | Out-Null
New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
$saved = @()
try {
    foreach ($index in 0..($relativeFiles.Count - 1)) {
        $source = Join-Path $repoRoot $relativeFiles[$index]
        $backup = Join-Path $backupDir "$index.bak"
        if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
            if (Test-Path -LiteralPath $source) {
                throw "Expected a file, but found another path type: $($relativeFiles[$index])"
            }
            $saved += ,([pscustomobject]@{ Source = $source; Backup = $backup; Existed = $false })
            continue
        }
        Copy-Item -LiteralPath $source -Destination $backup
        $saved += ,([pscustomobject]@{ Source = $source; Backup = $backup; Existed = $true })
        if ($index -gt 0) {
            $lastRunIds[$relativeFiles[$index]] = (Get-Content -LiteralPath $source -Raw | ConvertFrom-Json).run_id
        }
    }

    Write-Host 'LIVE DEMO | 4 techniques | real buggy / fixed evaluation' -ForegroundColor Cyan
    Write-Host "SAME BUG: Jsoup-40 | SAME TARGET CLASS: $expectedTarget" -ForegroundColor Cyan
    Write-Host 'Steps: suite -> checkout -> coverage -> buggy test -> fixed test -> result'
    Write-Host 'BUG_DETECTED = buggy has failing tests and fixed passes. N/A = not measured.'
    $caseNumber = 0

    foreach ($case in $cases) {
        Write-Host ''
        $caseNumber++
        Write-Host ('=' * 78) -ForegroundColor DarkCyan
        Write-Host "CASE $caseNumber / 4 | $($techniqueLabels[$case.Technique]) | $($case.Project)-$($case.Bug)" -ForegroundColor Cyan
        $caseStart = Get-Date
        docker exec $containerName python3 -u /workspace/scripts/run_benchmark.py --resume --project $case.Project --bug $case.Bug --techniques $case.Technique --csv $demoCsv --junit-output-dir "/workspace/.local/demo/$sessionId/junit" | Tee-Object -FilePath (Join-Path $reportDir 'run.log') -Append | ForEach-Object {
            $message = [string]$_
            if ($ShowRunnerOutput) { Write-Host $message -ForegroundColor DarkGray }
            switch -Regex ($message) {
                '^\[JUNIT\] (.*)' { Write-Host "        JUnit > $($Matches[1])" -ForegroundColor Yellow }
                'Target modified classes: (.*)' { Write-Host "  [1/6] Target classes: $($Matches[1])" }
                'Checking out .*b\.\.\.' { Write-Host '  [2/6] Checkout BUGGY version ...' }
                'Packaging test files: (.*)' { Write-Host "        Suite: $($Matches[1])" }
                'Measuring code coverage' { Write-Host '  [3/6] Measure target-class coverage ... (running)' }
                'Target Modified Class Coverage -> (.*)' { Write-Host "        Coverage: $($Matches[1])" -ForegroundColor Cyan }
                'Running test on Buggy version' { Write-Host '  [4/6] Run the same suite on BUGGY ... (running)' }
                'Buggy Failures \((\d+)\)' {
                    $count = [int]$Matches[1]
                    $testStatus = if ($count -eq 0) { 'PASS' } else { 'FAIL' }
                    Write-Host "        JUnit BUGGY: $testStatus | $count failing tests" -ForegroundColor Yellow
                }
                'Checking out Fixed version' { Write-Host '  [5/6] Checkout FIXED and run the same suite ... (running)' }
                'Fixed Failures \((\d+)\)' {
                    $count = [int]$Matches[1]
                    $testStatus = if ($count -eq 0) { 'PASS' } else { 'FAIL' }
                    Write-Host "        JUnit FIXED: $testStatus | $count failing tests" -ForegroundColor Cyan
                }
                'COMPILE ERROR|TIMED OUT|Error evaluating|Warning:|NO_SUITE|\[SKIP\]' { Write-Host "        $message" -ForegroundColor Red }
            }
        }
        if ($LASTEXITCODE -ne 0) {
            throw "The live benchmark failed for $($case.Project)-$($case.Bug)-$($case.Technique)."
        }
        $resultRelative = "results/$($case.Project)/$($case.Bug)/$($case.Technique).json"
        $result = Get-Content -LiteralPath (Join-Path $repoRoot $resultRelative) -Raw | ConvertFrom-Json
        if (-not $result.run_id -or $result.run_id -eq $lastRunIds[$resultRelative]) {
            throw "No fresh result was produced for $resultRelative. Old results will not be displayed as live."
        }
        if ($result.target_classes -ne $expectedTarget) {
            throw "Target class mismatch: expected $expectedTarget, received $($result.target_classes)."
        }
        $label = if ($result.status -eq 'DONE') { $result.fault_detected } else { $result.status }
        $color = if ($label -eq 'BUG_DETECTED') { 'Green' } elseif ($label -eq 'NOT_DETECTED') { 'Yellow' } else { 'Red' }
        Write-Host "  [6/6] RESULT: $label | elapsed $([math]::Round(((Get-Date) - $caseStart).TotalSeconds, 1)) s" -ForegroundColor $color
        if ($result.error) { Write-Host "        Detail: $($result.error)" -ForegroundColor Red }
        Write-Host "        Suite SHA-256: $($result.suite_sha256)" -ForegroundColor DarkGray
        Write-Host "        Run ID: $($result.run_id)" -ForegroundColor DarkGray
        $demoResults += [pscustomobject]@{ project = $case.Project; bug_id = $case.Bug; technique = $techniqueLabels[$case.Technique]; result = $result }
        $payload = [pscustomobject]@{ session_id = $sessionId; started_at = $startedAt; results = @($demoResults) }
        [System.IO.File]::WriteAllText((Join-Path $reportDir 'results.json'), ($payload | ConvertTo-Json -Depth 30), [System.Text.UTF8Encoding]::new($false))
    }

    Write-Host ''
    $csvLines = docker exec $containerName cat $demoCsv
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not save the isolated live-demo CSV.'
    }
    [System.IO.File]::WriteAllText((Join-Path $reportDir 'results.csv'), ($csvLines -join "`n") + "`n", [System.Text.UTF8Encoding]::new($false))
    Write-Host '=== LIVE DEMO SUMMARY ===' -ForegroundColor Cyan
    Write-Host 'Buggy / Fixed columns = number of failing tests; coverage = modified target classes.'
    Write-Host ('{0,-24} {1,9} {2,9} {3,8} {4,8}  {5}' -f 'Technique / Case', 'Line', 'Branch', 'Buggy', 'Fixed', 'Result')
    foreach ($record in $demoResults) {
        $r = $record.result
        $line = if ($null -eq $r.line_cov) { 'N/A' } else { '{0:F2}%' -f $r.line_cov }
        $branch = if ($null -eq $r.branch_cov) { 'N/A' } else { '{0:F2}%' -f $r.branch_cov }
        $buggy = if ($r.status -eq 'DONE' -and $null -ne $r.buggy_failures) { @($r.buggy_failures).Count } else { 'N/A' }
        $fixed = if ($r.status -eq 'DONE' -and $null -ne $r.fixed_failures) { @($r.fixed_failures).Count } else { 'N/A' }
        $label = if ($r.status -eq 'DONE') { $r.fault_detected } else { $r.status }
        $color = if ($label -eq 'BUG_DETECTED') { 'Green' } elseif ($label -eq 'NOT_DETECTED') { 'Yellow' } else { 'Red' }
        Write-Host ('{0,-24} {1,9} {2,9} {3,8} {4,8}  {5}' -f "$($record.technique) / $($record.project)-$($record.bug_id)", $line, $branch, $buggy, $fixed, $label) -ForegroundColor $color
    }
    Write-Host ''
    Write-Host 'GREEN: BUG_DETECTED | YELLOW: NOT_DETECTED | RED: evaluation problem'
    Write-Host 'FLAKY_OR_REGRESSION: tests also fail on FIXED; this label alone does not prove flakiness.'
    Write-Host 'Same bug and target class for all four suites. This single case is not an overall ranking.'
}
finally {
    foreach ($item in $saved) {
        if ($item.Existed -and (Test-Path -LiteralPath $item.Backup -PathType Leaf)) {
            Copy-Item -LiteralPath $item.Backup -Destination $item.Source -Force
        } elseif (-not $item.Existed -and (Test-Path -LiteralPath $item.Source -PathType Leaf)) {
            Remove-Item -LiteralPath $item.Source -Force
        }
    }

    docker exec $containerName rm -f $demoCsv 2>$null | Out-Null

    $tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath()).TrimEnd('\') + '\'
    $resolvedBackupDir = [System.IO.Path]::GetFullPath($backupDir)
    if ($resolvedBackupDir.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase) -and $resolvedBackupDir -ne $tempRoot) {
        foreach ($file in Get-ChildItem -LiteralPath $resolvedBackupDir -File -ErrorAction SilentlyContinue) {
            Remove-Item -LiteralPath $file.FullName -Force
        }
        Remove-Item -LiteralPath $resolvedBackupDir -Force
    }

    Write-Host 'Restored original progress and per-bug JSON files. The master CSV was not used as output.'
    if ($demoResults.Count -gt 0) {
        Write-Host "Saved demo evidence: $reportDir" -ForegroundColor Cyan
        Write-Host 'Evidence files: results.json, results.csv (complete run), run.log, junit/ (raw test output and failure traces)'
    }
}
