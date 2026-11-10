$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$distDir = Join-Path $projectRoot 'frontend\dist'
$srcStatic = Join-Path $projectRoot 'backend\src\main\resources\static'
$targetStatic = Join-Path $projectRoot 'backend\target\classes\static'
$jarPath = Join-Path $projectRoot 'backend\target\QuanLyKTX.jar'

# 1. Clean old assets in static dirs
foreach ($dir in @($srcStatic, $targetStatic)) {
    if (Test-Path -LiteralPath (Join-Path $dir 'assets')) {
        Get-ChildItem -Path (Join-Path $dir 'assets') -Recurse -File | Remove-Item -Force
    }
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    Copy-Item -Path (Join-Path $distDir '*') -Destination $dir -Recurse -Force
}

# 2. Update fat JAR if present
if (Test-Path -LiteralPath $jarPath) {
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem

    $zip = [System.IO.Compression.ZipFile]::Open($jarPath, [System.IO.Compression.ZipArchiveMode]::Update)
    try {
        $toDelete = @()
        foreach ($entry in $zip.Entries) {
            if ($entry.FullName -like 'BOOT-INF/classes/static/*' -or $entry.FullName -like 'static/*') {
                $toDelete += $entry
            }
        }
        foreach ($entry in $toDelete) {
            $entry.Delete()
        }

        foreach ($file in (Get-ChildItem -Path $distDir -Recurse -File)) {
            $rel = $file.FullName.Substring($distDir.Length).TrimStart('\', '/') -replace '\\', '/'
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $file.FullName, 'BOOT-INF/classes/static/' + $rel) | Out-Null
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $file.FullName, 'static/' + $rel) | Out-Null
        }
    } finally {
        $zip.Dispose()
    }
}
Write-Host 'Synchronized dist to backend static and QuanLyKTX.jar!' -ForegroundColor Green

