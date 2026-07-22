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

function Get-Sha256 {
    param([string]$Path)

    $stream = [IO.File]::OpenRead($Path)
    $sha256 = [Security.Cryptography.SHA256]::Create()
    try {
        $hash = $sha256.ComputeHash($stream)
        return ([BitConverter]::ToString($hash)).Replace("-", "").ToLowerInvariant()
    } finally {
        $sha256.Dispose()
        $stream.Dispose()
    }
}

function Read-ExpectedChecksum {
    param([string]$Path)

    if (!(Test-Path $Path)) {
        throw "Checksum file is missing: $Path"
    }
    $match = [regex]::Match(([IO.File]::ReadAllText($Path)), "(?i)\b[0-9a-f]{64}\b")
    if (!$match.Success) {
        throw "The Temurin checksum file did not contain a SHA-256 value."
    }
    return $match.Value.ToLowerInvariant()
}

function Assert-ArchiveChecksum {
    param(
        [string]$ArchivePath,
        [string]$ChecksumPath
    )

    $expected = Read-ExpectedChecksum $ChecksumPath
    $actual = Get-Sha256 $ArchivePath
    if ($actual -ne $expected) {
        throw "Temurin 8 SHA-256 verification failed; the archive will not be extracted."
    }
}

function Get-OfficialPackageInfo {
    param([string]$ApiUrl)

    $request = [Net.HttpWebRequest]::Create($ApiUrl)
    $request.AllowAutoRedirect = $false
    $request.UserAgent = "MCLauncherRevival dependency downloader"
    $response = $request.GetResponse()
    try {
        $location = $response.Headers["Location"]
    } finally {
        $response.Dispose()
    }

    if ([string]::IsNullOrEmpty($location)) {
        throw "Adoptium did not return a JDK package location."
    }
    $uri = New-Object Uri($location)
    if ($uri.Scheme -ne "https" -or $uri.Host -ne "github.com" -or
        !$uri.AbsolutePath.StartsWith("/adoptium/temurin8-binaries/releases/download/")) {
        throw "Adoptium returned an unexpected JDK package location."
    }
    return $location
}

try {
    $toolsDir = Split-Path -Parent $Destination
    $zipPath = Join-Path $toolsDir "temurin8-jdk.zip"
    $checksumPath = "$zipPath.sha256.txt"
    $zipPartPath = "$zipPath.part"
    $checksumPartPath = "$checksumPath.part"
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

    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    } catch {
        [Net.ServicePointManager]::SecurityProtocol = 3072
    }

    $client = New-Object Net.WebClient
    $client.Headers.Add("User-Agent", "MCLauncherRevival dependency downloader")
    try {
        $downloadUrl = Get-OfficialPackageInfo $apiUrl
        Write-Host "Fetching the official Temurin 8 SHA-256 checksum"
        $client.DownloadFile("$downloadUrl.sha256.txt", $checksumPartPath)
        Read-ExpectedChecksum $checksumPartPath | Out-Null
        Move-Item -Force $checksumPartPath $checksumPath

        $needDownload = $true
        if (Test-Path $zipPath) {
            try {
                Assert-ArchiveChecksum $zipPath $checksumPath
                Write-Host "Using verified existing tools/temurin8-jdk.zip"
                $needDownload = $false
            } catch {
                Write-Host "Cached JDK archive did not match the current official checksum; replacing it."
            }
        }

        if ($needDownload) {
            Write-Host "Downloading Temurin 8 from Adoptium"
            Write-Host $downloadUrl
            $client.DownloadFile($downloadUrl, $zipPartPath)
            Assert-ArchiveChecksum $zipPartPath $checksumPath
            Move-Item -Force $zipPartPath $zipPath
        }
    } catch {
        if ((Test-Path $zipPath) -and (Test-Path $checksumPath)) {
            Assert-ArchiveChecksum $zipPath $checksumPath
            Write-Host "Adoptium is unavailable; using the cached archive with its stored checksum."
        } else {
            throw
        }
    } finally {
        if (Test-Path $zipPartPath) { Remove-Item -Force $zipPartPath }
        if (Test-Path $checksumPartPath) { Remove-Item -Force $checksumPartPath }
        $client.Dispose()
    }

    Assert-ArchiveChecksum $zipPath $checksumPath
    Write-Host "Temurin 8 SHA-256 verified"
    Extract-JdkZip $zipPath $Destination
} catch {
    Write-Host ("Failed with reason: " + $_.Exception.Message)
    exit 1
}
