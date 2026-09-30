$ErrorActionPreference = 'Stop'

$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Set-Location -LiteralPath $repoRoot

$containerName = 'defects4j_sqa'
$demoCsv = "/tmp/project_sqa_demo_$([guid]::NewGuid().ToString('N')).csv"
$backupDir = Join-Path ([System.IO.Path]::GetTempPath()) "project-sqa-demo-$([guid]::NewGuid().ToString('N'))"
$relativeFiles = @(
    'progress.json',
    'results/Chart/14/ipo.json',
    'results/Jsoup/45/mio.json',
    'results/Closure/105/deepseek.json',
    'results/Chart/3/gemini.json'
)
$cases = @(
    @{ Project = 'Chart'; Bug = 14; Technique = 'ipo' },
    @{ Project = 'Jsoup'; Bug = 45; Technique = 'mio' },
    @{ Project = 'Closure'; Bug = 105; Technique = 'deepseek' },
    @{ Project = 'Chart'; Bug = 3; Technique = 'gemini' }
)

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Engine is unavailable. Start Docker Desktop, then run this script again.'
}

$containerRunning = docker inspect --format '{{.State.Running}}' $containerName 2>$null
if ($LASTEXITCODE -ne 0 -or $containerRunning -ne 'true') {
    throw 'The defects4j_sqa container is not running. Start it with docker compose -f docker/docker-compose.yml up -d.'
}

New-Item -ItemType Directory -Path $backupDir | Out-Null
$saved = @()
try {
    foreach ($index in 0..($relativeFiles.Count - 1)) {
        $source = Join-Path $repoRoot $relativeFiles[$index]
        if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
            throw "Required result file is missing: $($relativeFiles[$index])"
        }
        $backup = Join-Path $backupDir "$index.bak"
        Copy-Item -LiteralPath $source -Destination $backup
        $saved += ,([pscustomobject]@{ Source = $source; Backup = $backup })
    }

    Write-Host 'This live demonstration runs one saved suite per technique.'
    Write-Host 'The benchmark CSV is isolated in the container temporary directory.'
    Write-Host 'progress.json and the four existing per-bug JSON results will be restored afterward.'

    foreach ($case in $cases) {
        Write-Host ''
        Write-Host "=== $($case.Project)-$($case.Bug) / $($case.Technique) ==="
        docker exec $containerName python3 /workspace/scripts/run_benchmark.py --resume --project $case.Project --bug $case.Bug --techniques $case.Technique --csv $demoCsv
        if ($LASTEXITCODE -ne 0) {
            throw "The live benchmark failed for $($case.Project)-$($case.Bug)-$($case.Technique)."
        }
    }

    Write-Host ''
    Write-Host '=== Live results captured in the isolated CSV ==='
    docker exec $containerName cat $demoCsv
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not display the isolated live-demo CSV.'
    }
}
finally {
    foreach ($item in $saved) {
        if (Test-Path -LiteralPath $item.Backup -PathType Leaf) {
            Copy-Item -LiteralPath $item.Backup -Destination $item.Source -Force
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
}
