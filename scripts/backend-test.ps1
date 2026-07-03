$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$jdkHome = Resolve-Path (Join-Path $root ".tools\jdk21\jdk-21.0.11+10")
$mavenBin = Resolve-Path (Join-Path $root ".tools\apache-maven-3.9.9\bin")

$env:JAVA_HOME = $jdkHome.Path
$env:PATH = "$($jdkHome.Path)\bin;$($mavenBin.Path);$env:PATH"

Push-Location (Join-Path $root "backend")
try {
    mvn -B "-Dmaven.repo.local=..\.m2\repository" test
}
finally {
    Pop-Location
}