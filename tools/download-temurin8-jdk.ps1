param(
    [string]$Destination = "$(Split-Path -Parent $MyInvocation.MyCommand.Path)\jdk8"
)

$ErrorActionPreference = "Stop"

function Find-JdkRoot {
    param([string]$Root)

    if (!(Test-Path $Root)) {
        return $null
    }

    if ((Test-Path (Join-Path $Root "bin\java.exe")) -and
        (Test-Path (Join-Path $Root "bin\javac.exe")) -and
        (Test-Path (Join-Path $Root "bin\jar.exe"))) {
        return $Root
    }

    $java = Get-ChildItem -Path $Root -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match "\\bin\\java\.exe$" } |
        Select-Object -First 1

    if ($java) {
        $candidate = Split-Path -Parent (Split-Path -Parent $java.FullName)
        if ((Test-Path (Join-Path $candidate "bin\javac.exe")) -and
            (Test-Path (Join-Path $candidate "bin\jar.exe"))) {
            return $candidate
        }
    }

    return $null
}

function Extract-JdkZip {
    param(
        [string]$ZipPath,
        [string]$Destination
    )

    if (Test-Path $Destination) {
        Remove-Item -Recurse -Force $Destination
    }
    New-Item -ItemType Directory -Force -Path $Destination | Out-Null

    Write-Host "Found local tools/temurin8-jdk.zip, extracting"
    $shell = New-Object -ComObject Shell.Application
    $zip = $shell.NameSpace($ZipPath)
    $dest = $shell.NameSpace($Destination)

    if ($zip -eq $null -or $dest -eq $null) {
        throw "Windows could not open the JDK zip file."
    }

    $dest.CopyHere($zip.Items(), 20)

    for ($i = 0; $i -lt 180; $i++) {
        $jdkRoot = Find-JdkRoot $Destination
        if ($jdkRoot) {
            Write-Host "Java JDK 8 ready"
            Write-Host $jdkRoot
            exit 0
        }
        Start-Sleep -Seconds 1
    }

    throw "The JDK zip was extracted, but java.exe, javac.exe, and jar.exe were not found."
}

try {
    $toolsDir = Split-Path -Parent $Destination
    $zipPath = Join-Path $toolsDir "temurin8-jdk.zip"
    $apiUrl = "https://api.adoptium.net/v3/binary/latest/8/ga/windows/x64/jdk/hotspot/normal/eclipse"

    if (!(Test-Path $toolsDir)) {
        New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
    }

    $existingJdk = Find-JdkRoot $Destination
    if ($existingJdk) {
        Write-Host "Found existing tools/jdk8"
        Write-Host "Java JDK 8 ready"
        Write-Host $existingJdk
        exit 0
    }

    if (Test-Path $zipPath) {
        Extract-JdkZip $zipPath $Destination
    }

    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    } catch {
        [Net.ServicePointManager]::SecurityProtocol = 3072
    }

    Write-Host "Downloading Temurin 8 from Adoptium"
    Write-Host $apiUrl

    $client = New-Object Net.WebClient
    $client.Headers.Add("User-Agent", "MCLauncherRevival dependency downloader")
    $client.DownloadFile($apiUrl, $zipPath)

    Extract-JdkZip $zipPath $Destination
} catch {
    Write-Host ("Failed with reason: " + $_.Exception.Message)
    exit 1
}
