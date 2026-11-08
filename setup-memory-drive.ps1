<#
.SYNOPSIS
    Increases the Windows pagefile (swap) size to provide more virtual memory
    for DoraMCU development, preventing DL4J OutOfMemoryError crashes.

.DESCRIPTION
    This script configures a larger pagefile on the C: drive to act as a
    memory drive on the hard disk, extending available memory beyond physical RAM.

    Your system has ~16 GB physical RAM. DL4J training requires ~10 GB
    (4 GB JVM heap + 6 GB JavaCPP native memory). The pagefile provides
    additional virtual memory when physical RAM is exhausted.

    Recommended pagefile size: 16 GB minimum, 24 GB maximum.

.PREREQUISITES
    - Run as Administrator
    - At least 24 GB free space on C: drive

.EXAMPLE
    .\setup-memory-drive.ps1
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$Drive = "C:",

    [Parameter(Mandatory=$false)]
    [int]$InitialSizeMB = 16384,  # 16 GB initial size

    [Parameter(Mandatory=$false)]
    [int]$MaximumSizeMB = 24576   # 24 GB maximum size
)

$ErrorActionPreference = "Stop"

# Check for administrator privileges
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Error "This script must be run as Administrator. Right-click PowerShell and select 'Run as Administrator'."
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DoraMCU Memory Drive Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check available disk space
$disk = Get-CimInstance Win32_LogicalDisk -Filter "DeviceID='$Drive'"
$freeSpaceGB = [math]::Round($disk.FreeSpace / 1GB, 2)
$requiredSpaceGB = $MaximumSizeMB / 1024

Write-Host "Drive: $Drive" -ForegroundColor Yellow
Write-Host "Free space: $freeSpaceGB GB" -ForegroundColor Yellow
Write-Host "Required space: $requiredSpaceGB GB" -ForegroundColor Yellow
Write-Host ""

if ($disk.FreeSpace -lt $MaximumSizeMB * 1MB) {
    Write-Error "Insufficient disk space on $Drive. Need at least $requiredSpaceGB GB, but only $freeSpaceGB GB is available."
    exit 1
}

# Check current pagefile configuration
$pageFiles = Get-CimInstance Win32_PageFileSetting
Write-Host "Current pagefile configuration:" -ForegroundColor Yellow
if ($pageFiles.Count -eq 0) {
    Write-Host "  No pagefile configured (managed by system)" -ForegroundColor Gray
} else {
    foreach ($pf in $pageFiles) {
        $name = if ($pf.Name) { $pf.Name } else { "System managed" }
        $initial = if ($pf.InitialSize) { "$($pf.InitialSize) MB" } else { "System managed" }
        $maximum = if ($pf.MaximumSize) { "$($pf.MaximumSize) MB" } else { "System managed" }
        Write-Host "  $name : Initial=$initial, Maximum=$maximum" -ForegroundColor Gray
    }
}
Write-Host ""

# Get current physical memory
$os = Get-CimInstance Win32_OperatingSystem
$totalRAMGB = [math]::Round($os.TotalVisibleMemorySize / 1MB, 2)
Write-Host "Physical RAM: $totalRAMGB GB" -ForegroundColor Yellow
Write-Host ""

# Configure the new pagefile
Write-Host "Configuring pagefile on $Drive..." -ForegroundColor Green
Write-Host "  Initial size: $InitialSizeMB MB ($([math]::Round($InitialSizeMB/1024, 1)) GB)" -ForegroundColor Green
Write-Host "  Maximum size: $MaximumSizeMB MB ($([math]::Round($MaximumSizeMB/1024, 1)) GB)" -ForegroundColor Green
Write-Host ""

# First, set to system managed to clear existing settings, then set custom size
# This is a two-step process required by Windows
$wmi = [WMIClass]"Win32_ComputerSystem"

# Step 1: Disable all pagefiles (set to system managed = 0)
$Result = $wmi.Rename()
if ($Result.ReturnValue -ne 0) {
    Write-Warning "Could not modify computer system settings. Attempting direct WMI pagefile configuration..."
}

# Step 2: Configure pagefile via WMI
$pageFile = [WMIClass]"Win32_PageFileSetting"
$newPageFile = $pageFile.CreateInstance()
$newPageFile.Name = "${Drive}\pagefile.sys"
$newPageFile.InitialSize = $InitialSizeMB
$newPageFile.MaximumSize = $MaximumSizeMB

# Use wmic command for reliable configuration
Write-Host "Applying pagefile settings via WMIC..." -ForegroundColor Green
& wmic computersystem where "Name='$($wmi.Name)'" set AutomaticManagedPagefile=False 2>$null
Start-Sleep -Seconds 2

# Set the pagefile size using the registry approach via wmic
& wmic pagefileset where "Name='$Drive\\pagefile.sys'" set InitialSize=$InitialSizeMB,MaximumSize=$MaximumSizeMB 2>$null

if ($LASTEXITCODE -ne 0) {
    # If wmic pagefileset doesn't find existing file, create it via SetPageFile method
    Write-Host "Creating new pagefile entry..." -ForegroundColor Green
    
    # Use PowerShell to set pagefile via registry
    $pageFileKey = "HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Memory Management"
    Set-ItemProperty -Path $pageFileKey -Name "PagingFiles" -Value "${Drive}\pagefile.sys = ${InitialSizeMB} ${MaximumSizeMB}" -Type String
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Configuration Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "IMPORTANT: A system restart is required for the new pagefile settings to take effect." -ForegroundColor Red
Write-Host ""
Write-Host "After restarting:" -ForegroundColor Yellow
Write-Host "  1. Your virtual memory will be: $totalRAMGB GB RAM + $([math]::Round($MaximumSizeMB/1024, 1)) GB pagefile" -ForegroundColor Yellow
Write-Host "  2. DL4J training will have more virtual memory available" -ForegroundColor Yellow
Write-Host "  3. The pagefile acts as a memory drive on your hard disk" -ForegroundColor Yellow
Write-Host ""
Write-Host "To verify after restart, run:" -ForegroundColor Gray
Write-Host "  Get-CimInstance Win32_PageFileSetting | Select-Object Name, InitialSize, MaximumSize" -ForegroundColor Gray
Write-Host ""
Write-Host "Would you like to restart now? (Y/N)" -ForegroundColor Yellow
$response = Read-Host
if ($response -eq 'Y' -or $response -eq 'y') {
    Write-Host "Restarting in 5 seconds..." -ForegroundColor Red
    Start-Sleep -Seconds 5
    Restart-Computer -Force
}
