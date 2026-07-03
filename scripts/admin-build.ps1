$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")

Push-Location (Join-Path $root "admin")
try {
    $env:npm_config_cache = ".\.npm-cache"
    npm run build
}
finally {
    Pop-Location
}