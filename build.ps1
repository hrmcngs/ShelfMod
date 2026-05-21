<#
.SYNOPSIS
  ShelfMod build script for Windows (PowerShell 5.1+).

.DESCRIPTION
  Builds the mod JAR(s) for one or more loaders and copies the result to dist/.
  Shows a single-line progress bar by default; pass -Verbose for raw gradle output.

.PARAMETER Targets
  Any of: forge, neoforge, fabric, all, clean. Default: all.

.PARAMETER Offline
  Pass --offline to gradle (no network access; first build still requires online).

.PARAMETER VerboseOutput
  Show full gradle output instead of the progress bar.

.PARAMETER Clean
  Run `clean` before building each target.

.EXAMPLE
  .\build.ps1                       # build all loaders
  .\build.ps1 forge                 # forge only
  .\build.ps1 forge fabric -Offline # both offline
  .\build.ps1 -Clean -Targets all   # clean + build

  JDK requirements (override via JAVA17_HOME / JAVA21_HOME env vars):
    forge-1.20.1     : Java 17
    neoforge-1.21.4  : Java 21
    fabric-1.21.4    : Java 21
#>
[CmdletBinding()]
param(
  [Parameter(Position=0, ValueFromRemainingArguments=$true)]
  [string[]]$Targets = @('all'),

  [Alias('o')]
  [switch]$Offline,

  [Alias('v')]
  [switch]$VerboseOutput,

  [Alias('c')]
  [switch]$Clean
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Dist = Join-Path $Root 'dist'
if (-not (Test-Path $Dist)) { New-Item -ItemType Directory -Path $Dist | Out-Null }

$AllTargets = @('forge', 'neoforge', 'fabric')

# Normalize/validate target list
$selected = New-Object System.Collections.ArrayList
foreach ($t in $Targets) {
  switch ($t.ToLower()) {
    'all'      { $AllTargets | ForEach-Object { [void]$selected.Add($_) } }
    'forge'    { [void]$selected.Add('forge') }
    'neoforge' { [void]$selected.Add('neoforge') }
    'fabric'   { [void]$selected.Add('fabric') }
    'clean'    { $selected = @('clean'); break }
    default    { throw "Unknown target: $t" }
  }
}

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

function Get-StepsTotal($t) {
  switch ($t) {
    'forge'    { 12 }
    'neoforge' { 35 }
    'fabric'   { 14 }
  }
}

function Find-Jdk([string]$Version, [string]$EnvVar) {
  $home = [Environment]::GetEnvironmentVariable($EnvVar)
  if ($home -and (Test-Path (Join-Path $home 'bin\java.exe'))) { return $home }
  # JAVA_HOME with matching version
  if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $v = & (Join-Path $env:JAVA_HOME 'bin\java.exe') -version 2>&1 | Out-String
    if ($v -match "version `"$Version") { return $env:JAVA_HOME }
  }
  # Common install locations
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

function Get-JdkFor($t) {
  switch ($t) {
    'forge'              { Find-Jdk '17' 'JAVA17_HOME' }
    { $_ -in 'neoforge','fabric' } { Find-Jdk '21' 'JAVA21_HOME' }
  }
}

function Format-Bar([int]$Count, [int]$Total, [int]$Width = 30) {
  $pct = if ($Total -gt 0) { [Math]::Min(100, [int](($Count * 100) / $Total)) } else { 0 }
  $filled = [int](($pct * $Width) / 100)
  $bar = '[' + ('#' * $filled) + ('-' * ($Width - $filled)) + ']'
  return "$bar {0,3}%" -f $pct
}

function Invoke-Gradle($target, [string[]]$gradleArgs) {
  $dir = Get-LoaderDir $target
  $req = Get-RequiredJdk $target
  $jdk = Get-JdkFor $target
  if (-not $jdk) {
    Write-Host "[$target] SKIP — JDK $req not found. Install (e.g. winget install EclipseAdoptium.Temurin.${req}.JDK) or set JAVA${req}_HOME." -ForegroundColor Yellow
    return 2
  }

  $allArgs = @('--no-daemon') + $gradleArgs
  if ($Offline) { $allArgs = @('--no-daemon', '--offline') + $gradleArgs }

  Write-Host "[$target] gradle $($gradleArgs -join ' ') (JDK $req at $jdk)"

  $env:JAVA_HOME = $jdk
  $env:Path = (Join-Path $jdk 'bin') + ';' + $env:Path
  $loaderDir = Join-Path $Root $dir

  if ($VerboseOutput) {
    Push-Location $loaderDir
    try {
      & .\gradlew.bat @allArgs
      return $LASTEXITCODE
    } finally { Pop-Location }
  }

  # Progress mode: capture stdout/stderr line by line, drive a single-line bar.
  $total = Get-StepsTotal $target
  $count = 0
  $inFailure = $false

  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = Join-Path $loaderDir 'gradlew.bat'
  foreach ($a in $allArgs) { [void]$psi.ArgumentList.Add($a) } 2>$null
  if (-not $psi.ArgumentList.Count) {
    # PS5.1 fallback: use Arguments string
    $psi.Arguments = ($allArgs -join ' ')
  }
  $psi.WorkingDirectory = $loaderDir
  $psi.UseShellExecute = $false
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError = $true

  $proc = [System.Diagnostics.Process]::Start($psi)
  # Merge stderr asynchronously into the same stream we read
  $stderrJob = Start-Job -ScriptBlock {
    param($p) while (-not $p.HasExited -or -not $p.StandardError.EndOfStream) {
      $l = $p.StandardError.ReadLine(); if ($l) { Write-Output $l }
    }
  } -ArgumentList $proc

  while (-not $proc.StandardOutput.EndOfStream) {
    $line = $proc.StandardOutput.ReadLine()
    if ($line -match '^>\s*Task\s+:(.*)$') {
      $count++
      $label = ':' + ($Matches[1] -replace '\s+(UP-TO-DATE|NO-SOURCE|SKIPPED|FROM-CACHE).*$', '')
      Write-Host -NoNewline ("`r[{0}] {1} {2}" -f $target, (Format-Bar $count $total), $label).PadRight([Console]::WindowWidth - 1)
      continue
    }
    if ($line -match 'Started working on\s+(.+)$') {
      $count++
      $label = '[NFRT] ' + ($Matches[1] -replace "`e\[[0-9;]*[A-Za-z]", '')
      Write-Host -NoNewline ("`r[{0}] {1} {2}" -f $target, (Format-Bar $count $total), $label).PadRight([Console]::WindowWidth - 1)
      continue
    }
    if ($line -match 'BUILD SUCCESSFUL') {
      Write-Host ("`r[{0}] {1} {2}" -f $target, (Format-Bar $total $total), $line).PadRight([Console]::WindowWidth - 1)
      continue
    }
    if ($line -match '^FAILURE:|BUILD FAILED|>\s*Task\s.*FAILED') {
      Write-Host ''
      Write-Host $line -ForegroundColor Red
      $inFailure = $true; continue
    }
    if ($inFailure) { Write-Host $line }
  }
  $proc.WaitForExit()
  Receive-Job $stderrJob -Wait -AutoRemoveJob | ForEach-Object {
    if ($_ -match '^FAILURE:|error') { Write-Host $_ -ForegroundColor Red }
  }
  return $proc.ExitCode
}

function Build-One($target) {
  $dir = Get-LoaderDir $target
  if ($Clean) { Invoke-Gradle $target @('clean') | Out-Null }
  $rc = Invoke-Gradle $target @('build')
  if ($rc -ne 0) { return $rc }

  $libs = Join-Path (Join-Path $Root $dir) 'build\libs'
  Get-ChildItem -Path $libs -Filter '*.jar' -ErrorAction SilentlyContinue | ForEach-Object {
    if ($_.Name -match '-(sources|dev|dev-shadow)\.jar$') { return }
    $out = Join-Path $Dist ("$target-" + $_.Name)
    Copy-Item -Force $_.FullName $out
    Write-Host "  -> dist\$target-$($_.Name)"
  }
  return 0
}

if ($selected -contains 'clean') {
  foreach ($t in $AllTargets) { Invoke-Gradle $t @('clean') | Out-Null }
  Remove-Item -Recurse -Force $Dist -ErrorAction SilentlyContinue
  New-Item -ItemType Directory -Path $Dist | Out-Null
  exit 0
}

$fails = New-Object System.Collections.ArrayList
foreach ($t in $selected) {
  $rc = Build-One $t
  if ($rc -ne 0) { [void]$fails.Add($t) }
}

Write-Host ''
Write-Host '=== build summary ==='
Get-ChildItem $Dist -Filter '*.jar' -ErrorAction SilentlyContinue | Format-Table Name, Length, LastWriteTime -AutoSize
if ($fails.Count -gt 0) {
  Write-Host "failed/skipped: $($fails -join ', ')" -ForegroundColor Red
  exit 1
}
