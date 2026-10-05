#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
按「模组清单」批量下载/更新指定 Minecraft 版本的模组。

读取 mods/mod-list.txt 中的模组清单（每行一个 Modrinth slug/id），
为每个模组查询目标 MC 版本 + 加载器的最新版本并下载到 mods/<版本> 目录。

【清单即来源】
  新增模组 = 清单加一行；剔除模组 = 清单删一行。
  不需要任何排除参数——不想要的模组直接从清单移除即可。
  清单支持 # 注释，可按用途分组注释。

【Modrinth】无需密钥。先用 slug 直查项目，失败则用 slug 作为关键词搜索兜底。

【CurseForge】需 --curseforge-api-key（网页有 Cloudflare 拦截，只能走官方 API）。
  免费申请：https://console.curseforge.com/ -> API Keys
  当 Modrinth 找不到项目或没有目标版本时自动回退查询。

用法示例：
    # 预览本次会下载/更新哪些
    python tools/update-mods.py --dry-run --allow-prerelease

    # 正式更新 26.3 整合包
    python tools/update-mods.py --allow-prerelease

    # 升级到新版本（自动建 mods/26.4，不影响 26.3）
    python tools/update-mods.py --game-version 26.4 --allow-prerelease

    # 从旧模组目录生成初始清单（仅初始化用一次）
    python tools/update-mods.py --generate-list --source-dir "F:/game/minecraft/1.21.1server/mods"
"""

import argparse
import csv
import json
import shutil
import sys
import time
import urllib.parse
import urllib.request
import zipfile
from pathlib import Path

MODRINTH_BASE = "https://api.modrinth.com/v2"
CURSEFORGE_BASE = "https://api.curseforge.com/v1"
USER_AGENT = "mixinEnhance-mod-updater/1.0"

# CurseForge 加载器类型映射
CF_LOADER_TYPE = {"forge": 1, "fabric": 4, "quilt": 5, "neoforge": 6}


# ---------------------------------------------------------------- HTTP 基础

def _http_get_json(url, headers, retries=2, timeout=30):
    """GET 并解析 JSON；失败退避重试，最终返回 None。"""
    for attempt in range(retries + 1):
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except Exception:
            if attempt == retries:
                return None
            time.sleep(0.6)
    return None


def modrinth_get(path, retries=2):
    return _http_get_json(f"{MODRINTH_BASE}/{path}", {"User-Agent": USER_AGENT}, retries)


def curseforge_get(path, api_key, retries=2):
    if not api_key:
        return None
    return _http_get_json(
        f"{CURSEFORGE_BASE}/{path}",
        {"x-api-key": api_key, "Accept": "application/json"},
        retries,
    )


def download_file(url, dest, retries=3, timeout=180):
    """下载到 dest；成功返回 (True, '')，失败返回 (False, 错误信息)。"""
    last_error = ""
    for attempt in range(1, retries + 1):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
            with urllib.request.urlopen(req, timeout=timeout) as resp, open(dest, "wb") as f:
                shutil.copyfileobj(resp, f)
            return True, ""
        except Exception as exc:
            last_error = str(exc)
            if attempt < retries:
                time.sleep(1)
    return False, last_error


# ---------------------------------------------------------------- Modrinth

def resolve_modrinth_project(slug, keyword, loader):
    """先 slug 直查，失败再用关键词搜索兜底。"""
    project = modrinth_get(f"project/{slug}")
    if project:
        return project

    # 注意：搜索不能加 versions 过滤，否则尚未发布目标版本的模组会搜不到，
    # 被误判为「不存在」。这里只按加载器过滤，版本可用性交给 get_target_version。
    facet = urllib.parse.quote(json.dumps([["categories:" + loader]]))
    keywords = []
    if keyword:
        keywords.append(keyword)
    keywords.append(slug)
    keywords.append(slug.replace("_", " ").replace("-", " "))

    seen = set()
    for kw in keywords:
        if not kw or kw in seen:
            continue
        seen.add(kw)
        q = urllib.parse.quote(kw)
        res = modrinth_get(f"search?query={q}&facets={facet}&limit=5")
        if res and res.get("hits"):
            return modrinth_get(f"project/{res['hits'][0]['slug']}")
    return None


def get_target_version(project_id, game_version, loader, allow_prerelease):
    gv = urllib.parse.quote(json.dumps([game_version]))
    ld = urllib.parse.quote(json.dumps([loader]))
    versions = modrinth_get(f"project/{project_id}/version?game_versions={gv}&loaders={ld}")
    if not versions:
        return None
    release = next((v for v in versions if v.get("version_type") == "release"), None)
    if release:
        return release
    if allow_prerelease and versions:
        return versions[0]
    return None


# ---------------------------------------------------------------- CurseForge

def resolve_curseforge_mod(keyword, game_version, loader, api_key):
    loader_type = CF_LOADER_TYPE[loader]
    filter_enc = urllib.parse.quote(keyword)
    res = curseforge_get(
        f"mods/search?gameId=432&classId=6&gameVersion={game_version}"
        f"&modLoaderType={loader_type}&searchFilter={filter_enc}&pageSize=5",
        api_key,
    )
    if res and res.get("data"):
        return res["data"][0]
    return None


def get_curseforge_file(mod_id, game_version, loader, api_key):
    loader_type = CF_LOADER_TYPE[loader]
    res = curseforge_get(
        f"mods/{mod_id}/files?gameVersion={game_version}&modLoaderType={loader_type}&pageSize=50",
        api_key,
    )
    if res and res.get("data"):
        return sorted(res["data"], key=lambda f: f.get("fileDate", 0), reverse=True)[0]
    return None


# ---------------------------------------------------------------- 生成清单

def extract_mod_id(jar_path):
    """从 jar 内的 fabric.mod.json / quilt.mod.json 提取规范化 mod id。"""
    try:
        with zipfile.ZipFile(jar_path) as zf:
            for name in ("fabric.mod.json", "quilt.mod.json"):
                try:
                    with zf.open(name) as f:
                        data = json.load(f)
                except KeyError:
                    continue
                if name == "fabric.mod.json":
                    return data.get("id")
                return (data.get("quilt_loader") or {}).get("metadata", {}).get("id")
    except Exception:
        pass
    return None


def generate_list(source_dir, list_file):
    source = Path(source_dir)
    if not source.is_dir():
        print(f"[错误] 源目录不存在: {source}")
        sys.exit(1)

    ids = []
    for jar in sorted(source.glob("*.jar")):
        mod_id = extract_mod_id(jar)
        if mod_id:
            ids.append(mod_id)

    list_file.write_text("\n".join(ids) + "\n", encoding="utf-8")
    print(f"[√] 已生成清单: {list_file} （{len(ids)} 个模组）")


# ---------------------------------------------------------------- 主流程

def load_entries(list_file):
    """读取清单，过滤空行与注释，返回 [(slug, keyword), ...]。"""
    entries = []
    # utf-8-sig：清单可能是带 BOM 的 UTF-8（PowerShell Set-Content 会加 BOM），需自动去除
    for line in list_file.read_text(encoding="utf-8-sig").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        parts = line.split("|", 1)
        slug = parts[0].strip()
        keyword = parts[1].strip() if len(parts) > 1 else ""
        entries.append((slug, keyword))
    return entries


def main():
    # Windows 控制台默认 GBK，打印中文/特殊字符时可能 UnicodeEncodeError，强制 UTF-8 输出
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

    project_root = Path(__file__).resolve().parent.parent

    parser = argparse.ArgumentParser(description="按清单批量下载/更新 Minecraft 模组")
    parser.add_argument("--list-file", default=str(project_root / "mods" / "mod-list.txt"))
    parser.add_argument("--out-dir", default=None)
    parser.add_argument("--game-version", default="26.3")
    parser.add_argument("--loader", default="fabric", choices=["fabric", "forge", "neoforge", "quilt"])
    parser.add_argument("--allow-prerelease", action="store_true")
    parser.add_argument("--curseforge-api-key", default="")
    parser.add_argument("--source-dir", default="")
    parser.add_argument("--generate-list", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    list_file = Path(args.list_file)
    out_dir = Path(args.out_dir) if args.out_dir else project_root / "mods" / args.game_version

    if args.generate_list:
        generate_list(args.source_dir or ".", list_file)
        return

    if not list_file.is_file():
        print(f"[错误] 清单文件不存在: {list_file}")
        print(f"       可先运行: python tools/update-mods.py --generate-list --source-dir <旧mods目录>")
        sys.exit(1)

    if not args.dry_run:
        out_dir.mkdir(parents=True, exist_ok=True)

    entries = load_entries(list_file)

    print(f"清单文件  : {list_file}")
    print(f"目标版本  : Minecraft {args.game_version} / {args.loader}")
    print(f"输出目录  : {out_dir}")
    print(f"清单模组  : {len(entries)} 个")
    cf_label = "+ CurseForge" if args.curseforge_api_key else "(未配置 CF key)"
    print(f"平台      : Modrinth {cf_label}")
    if args.dry_run:
        print("※ 预览模式，不会实际下载")
    print()

    results = []

    # slug -> 文件名映射：记录本次每个模组对应的文件；
    # 下次运行时若发现同一模组换了文件名（即出了新版本），删掉旧文件避免多版本共存冲突
    map_file = out_dir / ".mod-files.json"
    old_map = {}
    if map_file.is_file():
        try:
            old_map = json.loads(map_file.read_text(encoding="utf-8-sig"))
        except Exception:
            old_map = {}
    new_map = {}

    total = len(entries)
    for index, (slug, keyword) in enumerate(entries, start=1):
        print(f"[{index}/{total}] {slug}", end=" ")

        # 1) Modrinth
        source = "Modrinth"
        project = resolve_modrinth_project(slug, keyword, args.loader)
        version = get_target_version(project["id"], args.game_version, args.loader, args.allow_prerelease) if project else None
        title = project.get("title") if project else slug
        proj_label = project.get("slug") if project else slug

        # 2) 回退 CurseForge
        if not version and args.curseforge_api_key:
            cf_mod = resolve_curseforge_mod(slug, args.game_version, args.loader, args.curseforge_api_key)
            if cf_mod:
                cf_file = get_curseforge_file(cf_mod["id"], args.game_version, args.loader, args.curseforge_api_key)
                if cf_file:
                    source = "CurseForge"
                    version = cf_file
                    title = cf_mod.get("name")
                    proj_label = cf_mod.get("slug")

        if not version:
            hint = "" if args.curseforge_api_key else " (可加 --curseforge-api-key 尝试)"
            print(f"-> 无 {args.game_version}/{args.loader} 版本{hint}")
            results.append({
                "清单项": slug, "模组名": title, "状态": f"无{args.game_version}版本",
                "版本": "", "文件": "", "项目": proj_label, "来源": "",
            })
            continue

        if source == "Modrinth":
            file = next((f for f in version.get("files", []) if f.get("primary")), None)
            if file is None:
                file = version["files"][0]
            download_url = file["url"]
            dest_name = file["filename"]
            version_label = version.get("version_number")
        else:
            download_url = version.get("downloadUrl")
            dest_name = version.get("fileName")
            version_label = version.get("displayName")

        dest = out_dir / dest_name

        if args.dry_run:
            print(f"-> 将下载[{source}]: {dest_name} ({version_label})")
            status = "预览"
        elif dest.is_file():
            print(f"-> 已是最新，跳过 ({dest_name})")
            status = "已存在"
        else:
            ok, err = download_file(download_url, dest)
            if ok:
                print(f"-> 下载完成[{source}]: {dest_name}")
                status = "成功"
            else:
                if dest.is_file():
                    dest.unlink()  # 清理不完整文件，避免下次被误判为已存在
                print(f"-> 下载失败(已重试3次): {err}")
                status = "下载失败"

        if dest_name:
            new_map[slug] = dest_name

        results.append({
            "清单项": slug, "模组名": title, "状态": status,
            "版本": version_label, "文件": dest_name, "项目": proj_label, "来源": source,
        })

    # 清理被新版本替换掉的旧文件，并写回映射
    if not args.dry_run:
        removed = []
        for slug, new_name in new_map.items():
            old_name = old_map.get(slug)
            if old_name and new_name and old_name != new_name:
                old_path = out_dir / old_name
                if old_path.is_file():
                    old_path.unlink()
                    removed.append(old_name)
        if removed:
            print()
            print(f"已清理旧版本 {len(removed)} 个:")
            for name in removed:
                print(f"  - {name}")
        map_file.write_text(json.dumps(new_map, ensure_ascii=False, indent=2), encoding="utf-8")

    # 汇总
    from collections import Counter
    counts = Counter(r["状态"] for r in results)
    print()
    print("===== 汇总 =====")
    for status, count in counts.items():
        print(f"{status:<16} {count}")

    if not args.dry_run:
        report_path = out_dir / "download-report.csv"
        fieldnames = ["清单项", "模组名", "状态", "版本", "文件", "项目", "来源"]
        with report_path.open("w", newline="", encoding="utf-8-sig") as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(results)
        print()
        print(f"详细报告: {report_path}")
        print(f"模组目录: {out_dir}")


if __name__ == "__main__":
    main()
