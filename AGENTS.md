# Repository Agent Instructions

<!-- ENVIRONMENT-SNAPSHOT:START -->
## Environment Snapshot Contract

This repository keeps an explicit local environment snapshot to reduce wrong assumptions in PowerShell, Bash, PHP, Python, SQL, Git, Node, Docker, and local verification commands.

Snapshot source of truth:

```text
.codebase-memory/environment-snapshot.json
.codebase-memory/environment-snapshot.md
```

Refresh interval: every 7 days.

Last collected at: 2026-06-23T17:22:19.3351681-05:00

Next refresh due: 2026-06-30T17:22:19.3351681-05:00

Refresh command:

```powershell
powershell -ExecutionPolicy Bypass -File .\.codebase-memory\collect-environment-snapshot.ps1
```

Assistant rule:

Before producing local shell instructions, PowerShell scripts, setup commands, repository maintenance commands, or verification commands, prefer the latest environment snapshot over assumptions.

If the snapshot is older than 7 days, missing, or inconsistent with the requested task, refresh it before producing risky local automation.

Current detected OS:

```text
Microsoft Windows 10.0.19045 
Architecture: X64
Windows: True
Linux: False
macOS: False
PowerShell: 5.1.19041.6456 / Desktop
```

Current detected available tools:

```text
bash: C:\WINDOWS\system32\bash.exe
cmd: C:\WINDOWS\system32\cmd.exe
composer: C:\ProgramData\ComposerSetup\bin\composer.bat
curl: C:\WINDOWS\system32\curl.exe
curl.exe: C:\WINDOWS\system32\curl.exe
docker: C:\Program Files\Docker\Docker\resources\bin\docker.exe
docker-compose: C:\Program Files\Docker\Docker\resources\bin\docker-compose.exe
git: C:\Program Files\Git\cmd\git.exe
mysql: C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
node: C:\Program Files\nodejs\node.exe
npm: C:\Program Files\nodejs\npm.cmd
php: C:\PHP\php-8.4.13-nts-Win32-vs17-x64\php.exe
pip: C:\Python312\Scripts\pip.exe
pip3: C:\Python312\Scripts\pip3.exe
pnpm: C:\Users\Admin\AppData\Roaming\npm\pnpm.cmd
powershell: C:\WINDOWS\System32\WindowsPowerShell\v1.0\powershell.exe
pwsh: C:\Users\Admin\AppData\Local\Microsoft\WindowsApps\pwsh.exe
py: C:\WINDOWS\py.exe
python: C:\Python312\python.exe
python3: C:\Users\Admin\AppData\Local\Microsoft\WindowsApps\python3.exe
symfony: C:\WINDOWS\symfony.exe
tar: C:\WINDOWS\system32\tar.exe
where: C:\WINDOWS\system32\where.exe
where.exe: C:\WINDOWS\system32\where.exe
yarn: C:\Users\Admin\AppData\Roaming\npm\yarn.cmd
```

Current detected missing tools:

```text
7z
mariadb
psql
sh
sqlite3
```

Known local warnings:

- Bash exists, but WSL currently reports a .wslconfig error. Prefer PowerShell for local commands until WSL is fixed.
- Composer exists, but version check may fail through composer.bat under Windows PowerShell 5.1. Treat Composer as available if direct composer commands work.
<!-- ENVIRONMENT-SNAPSHOT:END -->


