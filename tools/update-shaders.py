#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量下载 Modrinth「光影」分区下载量前 N 名（默认 50）的光影包。

对应网页：https://modrinth.com/discover/shaders?s=downloads&m=50
（即按下载量降序，取前 50 个光影项目）

为每个光影项目查询「目标 MC 版本」的最新版本并下载到 shaderpacks/<版本> 目录。

【目标版本来源】默认自动取自项目根目录的 gradle.properties：
  - 游戏版本 -> minecraft_version（如 26.3）
  保证下载的光影包与当前模组版本一致，无需手动同步。
  需要临时下载别的版本时，用 --game-version 显式覆盖即可。

【光影 vs 模组】光影包不区分加载器（同一份 zip 同时支持 Iris / OptiFine），
  因此这里只按游戏版本过滤，不做 loader 过滤。

【Modrinth】无需密钥。

用法示例：
    # 预览本次会下载/更新哪些（版本取自 gradle.properties）
    python tools/update-shaders.py --dry-run --allow-prerelease

    # 正式下载 Top50 光影
    python tools/update-shaders.py

    # 只要前 10 个
    python tools/update-shaders.py --limit 10

    # 指定下载别的游戏版本
    python tools/update-shaders.py --game-version 26.4
"""

import argparse
import csv
import json
import shutil
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path

MODRINTH_BASE = "https://api.modrinth.com/v2"
USER_AGENT = "mixinEnhance-shader-updater/1.0"
DISCOVER_URL = "https://modrinth.com/discover/shaders?s=downloads&m=50"


# ---------------------------------------------------------------- HTTP 基础

def _http_get_json(url, retries=2, timeout=30):
    """GET 并解析 JSON；失败退避重试，最终返回 None。"""
    for attempt in range(retries + 1):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except Exception:
            if attempt == retries:
                return None
            time.sleep(0.6)
    return None


def modrinth_get(path, retries=2):
    return _http_get_json(f"{MODRINTH_BASE}/{path}", retries)


def download_file(url, dest, retries=3, timeout=300):
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


# ---------------------------------------------------------------- 配置

def read_gradle_properties(project_root):
    """读取 gradle.properties，返回 {key: value}（忽略注释/空行，去除键值两侧空白）。"""
    props = {}
    prop_file = Path(project_root) / "gradle.properties"
    if not prop_file.is_file():
        return props
    # utf-8-sig：兼容带 BOM 的文件
    for line in prop_file.read_text(encoding="utf-8-sig").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or line.startswith("!"):
            continue
        if "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


# ---------------------------------------------------------------- Modrinth

def search_top_shaders(limit):
    """按下载量降序取前 limit 个光影项目，返回搜索 hits 列表。"""
    facet = urllib.parse.quote(json.dumps([["project_type:shader"]]))
    # index=downloads 即「下载量降序」，与 discover 页 s=downloads 一致
    res = modrinth_get(f"search?query=&facets={facet}&index=downloads&limit={limit}")
    if not res:
        return []
    return res.get("hits", [])


def get_target_version(project_id, game_version, allow_prerelease):
    """取该项目支持目标游戏版本的最新版本；优先正式版，无则按需回退预发布版。"""
    gv = urllib.parse.quote(json.dumps([game_version]))
    versions = modrinth_get(f"project/{project_id}/version?game_versions={gv}")
    if not versions:
        return None
    # 按发布时间降序，确保取到「最新」
    versions = sorted(versions, key=lambda v: v.get("date_published", ""), reverse=True)
    release = next((v for v in versions if v.get("version_type") == "release"), None)
    if release:
        return release
    if allow_prerelease:
        return versions[0]
    return None


# ---------------------------------------------------------------- 主流程

def main():
    # Windows 控制台默认 GBK，打印中文/特殊字符时可能 UnicodeEncodeError，强制 UTF-8 输出
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass

    project_root = Path(__file__).resolve().parent.parent

    # 目标版本默认取自 gradle.properties，保证下载版本与当前模组版本一致
    props = read_gradle_properties(project_root)
    prop_game_version = props.get("minecraft_version")
    default_game_version = prop_game_version or "26.3"

    parser = argparse.ArgumentParser(description="下载 Modrinth 下载量前 N 的光影包")
    parser.add_argument("--out-dir", default=None)
    parser.add_argument("--game-version", default=default_game_version,
                        help="目标 Minecraft 版本，默认取自 gradle.properties 的 minecraft_version")
    parser.add_argument("--limit", type=int, default=50, help="下载下载量前 N 名，默认 50")
    parser.add_argument("--allow-prerelease", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    out_dir = Path(args.out_dir) if args.out_dir else project_root / "shaderpacks" / args.game_version

    if not args.dry_run:
        out_dir.mkdir(parents=True, exist_ok=True)

    print(f"来源榜单  : {DISCOVER_URL}")
    if prop_game_version and args.game_version == default_game_version:
        src_hint = "  （来自 gradle.properties）"
    elif prop_game_version and args.game_version != prop_game_version:
        src_hint = f"  （命令行覆盖，gradle.properties 为 {prop_game_version}）"
    else:
        src_hint = ""
    print(f"目标版本  : Minecraft {args.game_version}{src_hint}")
    print(f"输出目录  : {out_dir}")
    print(f"取前      : {args.limit} 名")
    if args.dry_run:
        print("※ 预览模式，不会实际下载")
    print()

    hits = search_top_shaders(args.limit)
    if not hits:
        print("[错误] 未能获取光影榜单（网络异常或接口变更）")
        sys.exit(1)

    # slug -> 文件名映射：记录本次每个光影对应的文件；
    # 下次运行若同一光影换了文件名（即出了新版本），删掉旧文件避免多版本共存冲突
    map_file = out_dir / ".shader-files.json"
    old_map = {}
    if map_file.is_file():
        try:
            old_map = json.loads(map_file.read_text(encoding="utf-8-sig"))
        except Exception:
            old_map = {}
    new_map = {}

    results = []
    total = len(hits)
    for index, hit in enumerate(hits, start=1):
        slug = hit.get("slug")
        title = hit.get("title") or slug
        downloads = hit.get("downloads", 0)
        print(f"[{index:>2}/{total}] {slug} ({downloads:,})", end=" ")

        version = get_target_version(hit["project_id"], args.game_version, args.allow_prerelease)
        if not version:
            print(f"-> 无 {args.game_version} 版本")
            results.append({
                "排名": index, "清单项": slug, "光影名": title, "状态": f"无{args.game_version}版本",
                "版本": "", "文件": "", "下载量": downloads,
            })
            continue

        file = next((f for f in version.get("files", []) if f.get("primary")), None)
        if file is None and version.get("files"):
            file = version["files"][0]
        if not file:
            print("-> 版本无可用文件")
            results.append({
                "排名": index, "清单项": slug, "光影名": title, "状态": "无可用文件",
                "版本": "", "文件": "", "下载量": downloads,
            })
            continue

        download_url = file["url"]
        dest_name = file["filename"]
        version_label = version.get("version_number")
        dest = out_dir / dest_name

        if args.dry_run:
            print(f"-> 将下载: {dest_name} ({version_label})")
            status = "预览"
        elif dest.is_file():
            print(f"-> 已是最新，跳过 ({dest_name})")
            status = "已存在"
        else:
            ok, err = download_file(download_url, dest)
            if ok:
                print(f"-> 下载完成: {dest_name}")
                status = "成功"
            else:
                if dest.is_file():
                    dest.unlink()  # 清理不完整文件，避免下次被误判为已存在
                print(f"-> 下载失败(已重试3次): {err}")
                status = "下载失败"

        if dest_name:
            new_map[slug] = dest_name

        results.append({
            "排名": index, "清单项": slug, "光影名": title, "状态": status,
            "版本": version_label, "文件": dest_name, "下载量": downloads,
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
        fieldnames = ["排名", "清单项", "光影名", "状态", "版本", "文件", "下载量"]
        with report_path.open("w", newline="", encoding="utf-8-sig") as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(results)
        print()
        print(f"详细报告: {report_path}")
        print(f"光影目录: {out_dir}")


if __name__ == "__main__":
    main()
