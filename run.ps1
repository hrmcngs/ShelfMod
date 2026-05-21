<#
.SYNOPSIS
  Launch the in-dev Minecraft client (or server) for a given loader.

.PARAMETER Loader
  forge | neoforge | fabric

.PARAMETER Mode
  client (default) | server

.PARAMETER Offline
  Pass --offline to gradle (no network).
  Requires at least one prior online launch so assets/dependencies are cached.

.PARAMETER VerboseOutput
  Pass --info to gradle for verbose output.

.EXAMPLE
  .\run.ps1 forge
  .\run.ps1 neoforge server
  .\run.ps1 forge -Offline
  .\run.ps1 fabric server -o
#>
[CmdletBinding()]
param(
  [Parameter(Position=0, Mandatory=$true)]
  [ValidateSet('forge','neoforge','fabric')]
  [string]$Loader,

  [Parameter(Position=1)]
  [ValidateSet('client','server')]
  [string]$Mode = 'client',

  [Alias('o')]
  [switch]$Offline,

  [Alias('v')]
  [switch]$VerboseOutput
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path

function Get-LoaderDir($t) {
  switch ($t) {
    'forge'    { 'forge-1.20.1' }
    'neoforge' { 'neoforge-1.21.4' }
    'fabric'   { 'fabric-1.21.4' }
  }
}

function Get-RequiredJdk($t) {
  if ($t -eq 'forge') { '17' } else { '21' }
}

function Find-Jdk([string]$Version, [string]$EnvVar) {
  $home = [Environment]::GetEnvironmentVariable($EnvVar)
  if ($home -and (Test-Path (Join-Path $home 'bin\java.exe'))) { return $home }
  if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $v = & (Join-Path $env:JAVA_HOME 'bin\java.exe') -version 2>&1 | Out-String
    if ($v -match "version `"$Version") { return $env:JAVA_HOME }
  }
  $probes = @(
    "C:\Program Files\Eclipse Adoptium\jdk-$Version*",
    "C:\Program Files\Microsoft\jdk-$Version*",
    "C:\Program Files\Java\jdk-$Version*",
    "C:\Program Files\BellSoft\LibericaJDK-$Version*",
    "C:\Program Files\Zulu\zulu-$Version*"
  )
  foreach ($p in $probes) {
    $hit = Get-Item -Path $p -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($hit -and (Test-Path (Join-Path $hit 'bin\java.exe'))) { return $hit.FullName }
  }
  return $null
}

$dir = Get-LoaderDir $Loader
$req = Get-RequiredJdk $Loader
$envVar = "JAVA${req}_HOME"
$jdk = Find-Jdk $req $envVar

if (-not $jdk) {
  Write-Host "JDK $req not found. Install (e.g. winget install EclipseAdoptium.Temurin.${req}.JDK) or set $envVar." -ForegroundColor Yellow
  exit 1
}

$task = if ($Mode -eq 'server') { 'runServer' } else { 'runClient' }

$gradleArgs = @('--no-daemon')
if ($Offline)       { $gradleArgs += '--offline' }
if ($VerboseOutput) { $gradleArgs += '--info' }

$flagLabels = @()
if ($Offline)       { $flagLabels += 'offline' }
if ($VerboseOutput) { $flagLabels += 'verbose' }
$flagsTxt = if ($flagLabels.Count -gt 0) { " [$($flagLabels -join ' ')]" } else { '' }

Write-Host "[$Loader/$Mode]$flagsTxt launching with JDK $req at $jdk"
$env:JAVA_HOME = $jdk
$env:Path = (Join-Path $jdk 'bin') + ';' + $env:Path

Push-Location (Join-Path $Root $dir)
try {
  & .\gradlew.bat @gradleArgs $task
  exit $LASTEXITCODE
} finally { Pop-Location }
