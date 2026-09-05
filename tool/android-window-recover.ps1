[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class NativeWindow {
    [DllImport("user32.dll")]
    public static extern bool ShowWindowAsync(IntPtr hWnd, int nCmdShow);

    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool SetWindowPos(IntPtr hWnd, IntPtr hWndInsertAfter, int X, int Y, int cx, int cy, uint uFlags);
}
'@

$processes = @(
    Get-Process |
        Where-Object {
            $_.ProcessName -in @('emulator', 'qemu-system-x86_64') -and
            $_.MainWindowHandle -ne 0
        }
)

if ($processes.Count -eq 0) {
    throw 'No visible Android emulator window process was found.'
}

$window = $processes |
    Sort-Object StartTime -Descending |
    Select-Object -First 1

$handle = $window.MainWindowHandle
$SW_RESTORE = 9
$SWP_SHOWWINDOW = 0x0040
$HWND_TOPMOST = [IntPtr](-1)
$HWND_NOTOPMOST = [IntPtr](-2)

[void] [NativeWindow]::ShowWindowAsync($handle, $SW_RESTORE)
[void] [NativeWindow]::SetWindowPos($handle, $HWND_TOPMOST, 40, 40, 1180, 2000, $SWP_SHOWWINDOW)
[void] [NativeWindow]::SetForegroundWindow($handle)
Start-Sleep -Milliseconds 500
[void] [NativeWindow]::SetWindowPos($handle, $HWND_NOTOPMOST, 40, 40, 1180, 2000, $SWP_SHOWWINDOW)
[void] [NativeWindow]::SetForegroundWindow($handle)

Write-Host "Recovered emulator window:"
Write-Host "  process=$($window.ProcessName)"
Write-Host "  pid=$($window.Id)"
Write-Host "  title=$($window.MainWindowTitle)"
Write-Host "  position=40,40"
Write-Host "  size=1180x2000"
