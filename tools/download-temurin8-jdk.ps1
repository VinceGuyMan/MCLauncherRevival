param(
    [string]$Destination = "$(Split-Path -Parent $MyInvocation.MyCommand.Path)\jdk8"
)

$ErrorActionPreference = "Stop"

try {
    [Net.ServicePointManager]::SecurityProtocol = [Enum]::ToObject([Net.SecurityProtocolType], 3072)
} catch {
}

$toolsDir = Split-Path -Parent $Destination
$zipPath = Join-Path $toolsDir "temurin8-jdk.zip"
$apiUrl = "https://api.adoptium.net/v3/binary/latest/8/ga/windows/x64/jdk/hotspot/normal/eclipse"

if (!(Test-Path $toolsDir)) {
    New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
}

Write-Host "Downloading Eclipse Temurin 8 JDK from Adoptium..."
Write-Host $apiUrl

$client = New-Object Net.WebClient
$client.Headers.Add("User-Agent", "MCLauncherRevive dependency downloader")
$client.DownloadFile($apiUrl, $zipPath)

if (Test-Path $Destination) {
    Remove-Item -Recurse -Force $Destination
}
New-Item -ItemType Directory -Force -Path $Destination | Out-Null

Write-Host "Extracting JDK..."
$shell = New-Object -ComObject Shell.Application
$zip = $shell.NameSpace($zipPath)
$dest = $shell.NameSpace($Destination)

if ($zip -eq $null -or $dest -eq $null) {
    throw "Windows could not open the downloaded JDK zip file."
}

$dest.CopyHere($zip.Items(), 20)

for ($i = 0; $i -lt 180; $i++) {
    $java = Get-ChildItem -Path $Destination -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match "\\bin\\java\.exe$" } |
        Select-Object -First 1

    $javac = Get-ChildItem -Path $Destination -Recurse -Filter javac.exe -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match "\\bin\\javac\.exe$" } |
        Select-Object -First 1

    if ($java -and $javac) {
        Write-Host "Portable JDK is ready:"
        Write-Host (Split-Path -Parent (Split-Path -Parent $java.FullName))
        exit 0
    }
    Start-Sleep -Seconds 1
}

throw "The JDK zip was downloaded, but extraction did not produce java.exe and javac.exe."
