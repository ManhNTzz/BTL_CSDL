$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Set-Location -LiteralPath $projectRoot
$configPath = Join-Path $projectRoot 'config\application-local.properties'
$jarPath = Join-Path $projectRoot 'backend\target\QuanLyKTX.jar'
$runDir = Join-Path $projectRoot 'run'
New-Item -ItemType Directory -Force -Path $runDir | Out-Null
$configDir = Join-Path $projectRoot 'config'
if (-not (Test-Path -LiteralPath $configDir)) {
    New-Item -ItemType Directory -Force -Path $configDir | Out-Null
}
if (-not (Test-Path -LiteralPath $configPath)) {
    $examplePath = Join-Path $configDir 'application-local.example.properties'
    if (Test-Path -LiteralPath $examplePath) {
        Copy-Item -LiteralPath $examplePath -Destination $configPath
    } else {
        @"
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/QuanLyKTX?useUnicode=true&characterEncoding=UTF-8&connectionTimeZone=Asia/Ho_Chi_Minh
spring.datasource.username=root
spring.datasource.password=6264
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
server.port=8080
"@ | Set-Content -LiteralPath $configPath -Encoding UTF8
    }
}
if (-not (Test-Path -LiteralPath $jarPath)) {
    Write-Host 'Chua co file thuc thi backend/target/QuanLyKTX.jar. Dang tu dong build ung dung lan dau...' -ForegroundColor Yellow
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File (Join-Path $projectRoot 'scripts\Build-App.ps1')
    if (-not (Test-Path -LiteralPath $jarPath)) {
        throw 'Khong tim thay backend/target/QuanLyKTX.jar sau khi build. Vui long kiem tra Java 17+ hoac chay thu Build_App.bat.'
    }
}
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
