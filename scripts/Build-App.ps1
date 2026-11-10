$ErrorActionPreference='Stop'
$projectRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Push-Location (Join-Path $projectRoot 'frontend')
try {
    $hasNpm = (Get-Command npm.cmd -ErrorAction SilentlyContinue) -ne $null
    $hasPrebuilt = Test-Path (Join-Path $projectRoot 'backend\src\main\resources\static\index.html')
    if ($hasNpm) {
        & npm.cmd install
        & npm.cmd run build -- --emptyOutDir=false
        $static = Join-Path $projectRoot 'backend\src\main\resources\static'
        New-Item -ItemType Directory -Force -Path $static | Out-Null
        Copy-Item -Path 'dist\*' -Destination $static -Recurse -Force
    } elseif ($hasPrebuilt) {
        Write-Host 'Khong tim thay Node.js/npm, su dung giao dien pre-built co san trong backend/src/main/resources/static' -ForegroundColor Yellow
    } else {
        throw 'Can cai dat Node.js va npm de build giao dien frontend.'
    }
} finally { Pop-Location }
Push-Location (Join-Path $projectRoot 'backend')
try {
    if (-not $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME)) {
        if (Test-Path 'C:\Program Files\Java\jdk-17') { $env:JAVA_HOME = 'C:\Program Files\Java\jdk-17' }
    }
    $mvnCmd = if (Test-Path '.\mvnw.cmd') { '.\mvnw.cmd' } else { 'mvn.cmd' }
    & $mvnCmd package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw 'Maven package failed' }
} finally { Pop-Location }
Write-Host 'Da tao backend/target/QuanLyKTX.jar. Chay Chay_App.bat de mo giao dien.'
