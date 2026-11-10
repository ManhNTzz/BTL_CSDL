$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Set-Location -LiteralPath $projectRoot
$configPath = Join-Path $projectRoot 'config\application-local.properties'
$jarPath = Join-Path $projectRoot 'backend\target\QuanLyKTX.jar'
$runDir = Join-Path $projectRoot 'run'
New-Item -ItemType Directory -Force -Path $runDir | Out-Null
if (-not (Test-Path -LiteralPath $configPath)) {
    Copy-Item -LiteralPath (Join-Path $projectRoot 'config\application-local.example.properties') -Destination $configPath
    Write-Host 'Lan dau: dien ten CSDL, tai khoan va mat khau MySQL trong tep cau hinh, luu lai roi chay Chay_App.bat lan nua.' -ForegroundColor Yellow
    Start-Process -FilePath notepad.exe -ArgumentList ('"' + $configPath + '"')
    if ($env:KTX_NON_INTERACTIVE -ne '1') { Read-Host 'Nhan Enter de dong' }
    exit 0
}
if (-not (Test-Path -LiteralPath $jarPath)) { throw 'Khong tim thay backend/target/QuanLyKTX.jar. Hay giai nen day du ZIP hoac build lai backend.' }
$serverPort = 8080
foreach ($line in [IO.File]::ReadAllLines($configPath)) {
    if ($line -match '^\s*server\.port\s*=\s*(\d+)\s*$') { $serverPort = [int]$matches[1] }
}
if ($serverPort -lt 1 -or $serverPort -gt 65535) { throw 'server.port phai tu 1 den 65535.' }
$url = 'http://127.0.0.1:' + $serverPort
function Test-Application {
    try {
        $response = Invoke-RestMethod -Uri ($url + '/api/he-thong/trang-thai') -TimeoutSec 2
        return ($response.ungDung -eq 'QuanLyKTX')
    } catch { return $false }
}
function Open-Application {
    if ($env:KTX_NO_WINDOW -eq '1') { return }
    $candidates = @(
        (Join-Path ${env:ProgramFiles(x86)} 'Microsoft\Edge\Application\msedge.exe'),
        (Join-Path $env:ProgramFiles 'Microsoft\Edge\Application\msedge.exe'),
        (Join-Path $env:ProgramFiles 'Google\Chrome\Application\chrome.exe')
    )
    $browser = $candidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
    if ($browser) { Start-Process -FilePath $browser -ArgumentList ('--app=' + $url) }
    else { Start-Process $url }
}
if (Test-Application) { Open-Application; exit 0 }
try {
    $probe = New-Object Net.Sockets.TcpClient
    try { $probe.Connect('127.0.0.1', $serverPort); $busy = $true } catch { $busy = $false } finally { $probe.Dispose() }
    if ($busy) { throw "Cong $serverPort dang duoc chuong trinh khac su dung. Doi server.port trong config/application-local.properties." }
    $java = $null
    if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { $java = Join-Path $env:JAVA_HOME 'bin\java.exe' }
    if (-not $java) { $java = (Get-Command java.exe -ErrorAction Stop).Source }
    $configUri = ([Uri]((Join-Path $projectRoot 'config') + [IO.Path]::DirectorySeparatorChar)).AbsoluteUri
    $taskArgs = @('-Dfile.encoding=UTF-8','-jar',('"'+$jarPath+'"'),'--spring.profiles.active=local',('--spring.config.additional-location="'+$configUri+'"'),'--app.auto-open-window=false',('--logging.file.name="'+(Join-Path $runDir 'backend.log')+'"'))
    $javaProcess = Start-Process -FilePath $java -ArgumentList $taskArgs -WorkingDirectory $projectRoot -WindowStyle Hidden -PassThru
    @{ pid=$javaProcess.Id; started=$javaProcess.StartTime.ToUniversalTime().Ticks.ToString(); jar=$jarPath } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $runDir 'backend.json')
    Write-Host 'Dang khoi dong, vui long cho toi da 60 giay...'
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        $javaProcess.Refresh()
        if ($javaProcess.HasExited) { break }
        if (Test-Application) { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) {
        $javaProcess.Refresh()
        if (-not $javaProcess.HasExited) { Stop-Process -Id $javaProcess.Id }
        throw 'Khong khoi dong duoc. Kiem tra Java 17+, MySQL, cau hinh va tep run/backend.log.'
    }
    Open-Application
    Write-Host 'Da mo ung dung. Khi dung xong, chay Tat_App.bat de dung backend.' -ForegroundColor Green
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($env:KTX_NON_INTERACTIVE -ne '1') { Read-Host 'Nhan Enter de dong' }
    exit 1
}
