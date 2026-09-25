<#
.SYNOPSIS
    按「模组清单」批量下载/更新指定 Minecraft 版本的模组。

.DESCRIPTION
    读取 mods/mod-list.txt 中的模组清单（每行一个 Modrinth slug/id），
    为每个模组查询目标 MC 版本 + 加载器的最新版本并下载到 mods/<版本> 目录。

    【清单即来源】
      新增模组 = 清单加一行；剔除模组 = 清单删一行。
      不需要任何排除参数——不想要的模组直接从清单移除即可。
      清单支持 # 注释，可按用途分组注释。

    【Modrinth】无需密钥。先用 slug 直查项目，失败则用 slug 作为关键词搜索兜底。

    【CurseForge】需 -CurseForgeApiKey（网页有 Cloudflare 拦截，只能走官方 API）。
      免费申请：https://console.curseforge.com/ -> API Keys
      当 Modrinth 找不到项目或没有目标版本时自动回退查询。

.PARAMETER ListFile
    模组清单文件，默认 mods/mod-list.txt（相对项目根目录）。

.PARAMETER OutDir
    下载输出目录，默认为 mods/<目标版本>（按版本分二级目录，便于并存维护多个版本）。

.PARAMETER GameVersion
    目标 Minecraft 版本，默认 26.3。

.PARAMETER Loader
    加载器：fabric / forge / neoforge / quilt，默认 fabric。

.PARAMETER AllowPrerelease
    允许 alpha / beta。目标版本较新时很多模组只发布了预发布版。

.PARAMETER CurseForgeApiKey
    CurseForge API 密钥。不提供则只用 Modrinth。

.PARAMETER GenerateList
    从 -SourceDir 扫描旧模组 jar 生成初始清单（仅初始化时用一次）。

.PARAMETER DryRun
    只预览，不实际下载。

.EXAMPLE
    # 预览本次会下载/更新哪些
    .\update-mods.ps1 -DryRun -AllowPrerelease

    # 正式更新 26.3 整合包
    .\update-mods.ps1 -AllowPrerelease

    # 升级到新版本（自动建 mods/26.4，不影响 26.3）
    .\update-mods.ps1 -GameVersion 26.4 -AllowPrerelease
#>
[CmdletBinding()]
param(
    [string]$ListFile,
    [string]$OutDir,
    [string]$GameVersion = '26.3',
    [ValidateSet('fabric', 'forge', 'neoforge', 'quilt')]
    [string]$Loader = 'fabric',
    [switch]$AllowPrerelease,
    [string]$CurseForgeApiKey,
    [string]$SourceDir = 'F:\game\minecraft\1.21.1server\mods',
    [switch]$GenerateList,
    [switch]$DryRun
)

$ErrorActionPreference = 'Continue'
$ModrinthBase = 'https://api.modrinth.com/v2'
$CurseForgeBase = 'https://api.curseforge.com/v1'
$Headers = @{ 'User-Agent' = 'mixinEnhance-mod-updater/1.0' }

$CfLoaderType = @{ 'forge' = 1; 'fabric' = 4; 'quilt' = 5; 'neoforge' = 6 }

$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $ListFile) { $ListFile = Join-Path $projectRoot 'mods/mod-list.txt' }
if (-not $OutDir) { $OutDir = Join-Path (Join-Path $projectRoot 'mods') $GameVersion }

Add-Type -AssemblyName System.IO.Compression.FileSystem

function Invoke-Modrinth {
    param([string]$Path)
    try { return Invoke-RestMethod -Uri "$ModrinthBase/$Path" -Headers $Headers -TimeoutSec 30 }
    catch { return $null }
}

function Invoke-CurseForge {
    param([string]$Path)
    if (-not $CurseForgeApiKey) { return $null }
    try {
        return Invoke-RestMethod -Uri "$CurseForgeBase/$Path" `
            -Headers @{ 'x-api-key' = $CurseForgeApiKey; 'Accept' = 'application/json' } -TimeoutSec 30
    }
    catch { return $null }
}

# ---------- Modrinth ----------

function Resolve-ModrinthProject {
    param([string]$Slug, [string]$Keyword)
    $p = Invoke-Modrinth "project/$Slug"
    if ($p) { return $p }

    $facet = [uri]::EscapeDataString("[[""categories:$Loader""]]")
    # 依次尝试：清单给的搜索关键词 -> 原 slug -> 分隔符换空格的 slug
    # （部分 id 是连写的，如 explosiveenhancement 直接搜不到，需靠关键词 "Explosive Enhancement"）
    $keywords = @()
    if ($Keyword) { $keywords += $Keyword }
    $keywords += $Slug
    $keywords += ($Slug -replace '[_-]', ' ')
    foreach ($kw in ($keywords | Where-Object { $_ } | Select-Object -Unique)) {
        $q = [uri]::EscapeDataString($kw)
        $res = Invoke-Modrinth "search?query=$q&facets=$facet&limit=5"
        if ($res -and $res.hits.Count -gt 0) { return Invoke-Modrinth "project/$($res.hits[0].slug)" }
    }
    return $null
}

function Get-TargetVersion {
    param([string]$ProjectId)
    $gv = [uri]::EscapeDataString('["' + $GameVersion + '"]')
    $ld = [uri]::EscapeDataString('["' + $Loader + '"]')
    $versions = Invoke-Modrinth "project/$ProjectId/version?game_versions=$gv&loaders=$ld"
    if (-not $versions -or $versions.Count -eq 0) { return $null }
    $release = $versions | Where-Object { $_.version_type -eq 'release' } | Select-Object -First 1
    if ($release) { return $release }
    if ($AllowPrerelease) { return $versions[0] }
    return $null
}

# ---------- CurseForge ----------

function Resolve-CurseForgeMod {
    param([string]$Keyword)
    $filter = [uri]::EscapeDataString($Keyword)
    $loader = $CfLoaderType[$Loader]
    $res = Invoke-CurseForge "mods/search?gameId=432&classId=6&gameVersion=$GameVersion&modLoaderType=$loader&searchFilter=$filter&pageSize=5"
    if ($res -and $res.data -and $res.data.Count -gt 0) { return $res.data[0] }
    return $null
}

function Get-CurseForgeFile {
    param([int]$ModId)
    $loader = $CfLoaderType[$Loader]
    $res = Invoke-CurseForge "mods/$ModId/files?gameVersion=$GameVersion&modLoaderType=$loader&pageSize=50"
    if ($res -and $res.data -and $res.data.Count -gt 0) {
        return $res.data | Sort-Object -Property fileDate -Descending | Select-Object -First 1
    }
    return $null
}

# ---------- 从旧目录生成清单（仅初始化用） ----------

if ($GenerateList) {
    if (-not (Test-Path $SourceDir)) { Write-Error "源目录不存在: $SourceDir"; exit 1 }
    $ids = @()
    foreach ($jar in (Get-ChildItem -Path $SourceDir -Filter *.jar -File | Sort-Object Name)) {
        $zip = $null
        try {
            $zip = [System.IO.Compression.ZipFile]::OpenRead($jar.FullName)
            $entry = $zip.Entries | Where-Object { $_.FullName -eq 'fabric.mod.json' } | Select-Object -First 1
            if (-not $entry) { $entry = $zip.Entries | Where-Object { $_.FullName -eq 'quilt.mod.json' } | Select-Object -First 1 }
            if ($entry) {
                $reader = New-Object System.IO.StreamReader($entry.Open())
                $json = $reader.ReadToEnd() | ConvertFrom-Json
                $reader.Close()
                if ($json.id) { $ids += $json.id }
                elseif ($json.quilt_loader) { $ids += $json.quilt_loader.metadata.id }
            }
        }
        catch {}
        finally { if ($zip) { $zip.Dispose() } }
    }
    $ids | Sort-Object | Set-Content -Path $ListFile -Encoding UTF8
    Write-Host "已生成清单: $ListFile （$($ids.Count) 个模组）" -ForegroundColor Green
    exit 0
}

# ---------------- 主流程（清单驱动） ----------------

if (-not (Test-Path $ListFile)) {
    Write-Error "清单文件不存在: $ListFile`n可先运行 .\update-mods.ps1 -GenerateList 生成"
    exit 1
}
if (-not $DryRun -and -not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir -Force | Out-Null
}

# 用 UTF8 读取：清单含中文注释，按系统编码读取会导致行解析错乱、条目被误并入注释行
$entries = Get-Content $ListFile -Encoding UTF8 |
    ForEach-Object { $_.Trim() } |
    Where-Object { $_ -and -not $_.StartsWith('#') }

Write-Host "清单文件  : $ListFile" -ForegroundColor Cyan
Write-Host "目标版本  : Minecraft $GameVersion / $Loader" -ForegroundColor Cyan
Write-Host "输出目录  : $OutDir" -ForegroundColor Cyan
Write-Host "清单模组  : $($entries.Count) 个" -ForegroundColor Cyan
Write-Host "平台      : Modrinth $(if ($CurseForgeApiKey) { '+ CurseForge' } else { '(未配置 CF key)' })" -ForegroundColor Cyan
if ($DryRun) { Write-Host "※ 预览模式，不会实际下载" -ForegroundColor Yellow }
Write-Host ""

$results = @()
$index = 0

# 「slug -> 文件名」映射：记录本次每个模组对应的文件；
# 下次运行时若发现同一模组换了文件名（即出了新版本），就删掉旧文件，避免多版本共存冲突
$mapFile = Join-Path $OutDir '.mod-files.json'
$newMap = @{}
$oldMap = @{}
if (Test-Path $mapFile) {
    try {
        $json = Get-Content $mapFile -Encoding UTF8 -Raw | ConvertFrom-Json
        if ($json) { $json.PSObject.Properties | ForEach-Object { $oldMap[$_.Name] = $_.Value } }
    }
    catch { }
}

foreach ($entry in $entries) {
    $index++
    # 条目支持 "slug|搜索关键词"：前者直查项目，后者仅作搜索兜底
    $parts = $entry -split '\|', 2
    $slug = $parts[0].Trim()
    $keyword = if ($parts.Count -gt 1) { $parts[1].Trim() } else { '' }

    Write-Host "[$index/$($entries.Count)] $slug" -NoNewline

    # 1) Modrinth
    $source = 'Modrinth'
    $project = Resolve-ModrinthProject -Slug $slug -Keyword $keyword
    $version = if ($project) { Get-TargetVersion $project.id } else { $null }
    $title = if ($project) { $project.title } else { $slug }
    $projLabel = if ($project) { $project.slug } else { $slug }

    # 2) 回退 CurseForge
    if (-not $version -and $CurseForgeApiKey) {
        $cfMod = Resolve-CurseForgeMod -Keyword $slug
        if ($cfMod) {
            $cfFile = Get-CurseForgeFile -ModId $cfMod.id
            if ($cfFile) {
                $source = 'CurseForge'
                $version = $cfFile
                $title = $cfMod.name
                $projLabel = $cfMod.slug
            }
        }
    }

    if (-not $version) {
        $hint = if ($CurseForgeApiKey) { '' } else { '(可加 -CurseForgeApiKey 尝试)' }
        Write-Host "  -> 无 $GameVersion/$Loader 版本$hint" -ForegroundColor Yellow
        $results += [pscustomobject]@{
            清单项 = $slug; 模组名 = $title; 状态 = "无${GameVersion}版本"
            版本 = ''; 文件 = ''; 项目 = $projLabel; 来源 = ''
        }
        continue
    }

    if ($source -eq 'Modrinth') {
        $file = $version.files | Where-Object { $_.primary } | Select-Object -First 1
        if (-not $file) { $file = $version.files[0] }
        $downloadUrl = $file.url
        $destName = $file.filename
        $versionLabel = $version.version_number
    }
    else {
        $downloadUrl = $version.downloadUrl
        $destName = $version.fileName
        $versionLabel = $version.displayName
    }

    $dest = Join-Path $OutDir $destName

    if ($DryRun) {
        Write-Host "  -> 将下载[$source]: $destName ($versionLabel)" -ForegroundColor Green
        $status = '预览'
    }
    elseif (Test-Path $dest) {
        Write-Host "  -> 已是最新，跳过 ($destName)" -ForegroundColor DarkGray
        $status = '已存在'
    }
    else {
        try {
            Invoke-WebRequest -Uri $downloadUrl -OutFile $dest -Headers $Headers -TimeoutSec 180
            Write-Host "  -> 下载完成[$source]: $destName" -ForegroundColor Green
            $status = '成功'
        }
        catch {
            Write-Host "  -> 下载失败: $($_.Exception.Message)" -ForegroundColor Red
            $status = '下载失败'
        }
    }

    if ($destName) { $newMap[$slug] = $destName }

    $results += [pscustomobject]@{
        清单项 = $slug; 模组名 = $title; 状态 = $status
        版本 = $versionLabel; 文件 = $destName; 项目 = $projLabel; 来源 = $source
    }
}

# 清理被新版本替换掉的旧文件，并写回映射供下次使用
if (-not $DryRun) {
    $removed = @()
    foreach ($key in $newMap.Keys) {
        $oldName = $oldMap[$key]
        $newName = $newMap[$key]
        if ($oldName -and $newName -and ($oldName -ne $newName)) {
            $oldPath = Join-Path $OutDir $oldName
            if (Test-Path $oldPath) {
                Remove-Item $oldPath -Force
                $removed += $oldName
            }
        }
    }
    if ($removed.Count -gt 0) {
        Write-Host ""
        Write-Host "已清理旧版本 $($removed.Count) 个:" -ForegroundColor DarkYellow
        $removed | ForEach-Object { Write-Host "  - $_" -ForegroundColor DarkYellow }
    }
    $newMap | ConvertTo-Json | Set-Content -Path $mapFile -Encoding UTF8
}

Write-Host ""
Write-Host "===== 汇总 =====" -ForegroundColor Cyan
$results | Group-Object 状态 | ForEach-Object { Write-Host ("{0,-16} {1}" -f $_.Name, $_.Count) }

if (-not $DryRun) {
    $reportPath = Join-Path $OutDir 'download-report.csv'
    $results | Export-Csv -Path $reportPath -NoTypeInformation -Encoding UTF8
    Write-Host ""
    Write-Host "详细报告: $reportPath" -ForegroundColor Cyan
    Write-Host "模组目录: $OutDir" -ForegroundColor Cyan
}
