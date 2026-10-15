param()
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$web = Join-Path $root 'client/richtext-web'
Push-Location $web
try {
    npm install --no-audit --no-fund
    npm run build
} finally {
    Pop-Location
}
$android = Join-Path $root 'client/android/app/src/main/assets/richtext'
$ios = Join-Path $root 'client/ios/Resources/RichText'
New-Item -ItemType Directory -Force -Path $android, $ios | Out-Null
Remove-Item -Recurse -Force (Join-Path $android '*') -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force (Join-Path $ios '*') -ErrorAction SilentlyContinue
Copy-Item -Recurse -Force (Join-Path $web 'dist/*') $android
Copy-Item -Recurse -Force (Join-Path $web 'dist/*') $ios
