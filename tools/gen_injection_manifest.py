#!/usr/bin/env python3
"""Injection manifest generator / checker.

Scans mixin sources for injectors that opted out of failing loudly (``require = 0``) and
writes the ``handler | target class | mixin | side`` list consumed at runtime by
``InjectionSelfCheck``. Those injectors silently do nothing when a Minecraft upgrade
changes the target method, so the mod re-verifies at startup that every handler really
landed in its target class.

Exit code: 0 = manifest written (or, with --check, already up to date); 1 = --check found
it stale, or a @Mixin target / handler could not be resolved (never fail silently).

Usage:
    python3 tools/gen_injection_manifest.py            # (re)generate the manifest
    python3 tools/gen_injection_manifest.py --check    # CI gate: fail when stale
"""
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, ".."))
MIXIN_DIR = os.path.join(ROOT, "src", "main", "java", "com", "frankfurtlin", "mixinenhance", "mixin")
MIXINS_JSON = os.path.join(ROOT, "src", "main", "resources", "mixinenhance.mixins.json")
MANIFEST = os.path.join(ROOT, "src", "main", "resources", "mixinenhance", "injections.txt")

IMPORT_RE = re.compile(r"^\s*import\s+(?!static)([\w.]+)\.(\w+);")
MIXIN_RE = re.compile(r"@Mixin\s*\(([^)]*)\)")
TARGET_RE = re.compile(r"([\w$]+)\.class")
# 注入器：只收 require = 0 的那些（其余注入失败会直接崩，不需要自检兜底）
INJECTOR_RE = re.compile(
    r"@(Inject|ModifyConstant|ModifyVariable|ModifyExpressionValue|ModifyArg|ModifyArgs"
    r"|Redirect|WrapOperation|WrapWithCondition)\s*\(")
REQUIRE_ZERO_RE = re.compile(r"require\s*=\s*0")
METHOD_RE = re.compile(
    r"^\s*(?:public|protected|private|default)\s+(?:static\s+|final\s+|synchronized\s+)*"
    r"[\w<>\[\],.?$\s]+\s+(\w[\w$]*)\s*\(")


def load_sides():
    """mixin 简单类名 -> 端（common / client）。"""
    with open(MIXINS_JSON, "r", encoding="utf-8") as fh:
        data = json.load(fh)
    sides = {}
    for name in data.get("mixins", []):
        sides[name.split(".")[-1]] = "common"
    for name in data.get("client", []):
        sides[name.split(".")[-1]] = "client"
    return sides


def resolve_targets(text, imports, path):
    """解析 @Mixin(...) 里的目标类完整名。"""
    match = MIXIN_RE.search(text)
    if not match:
        return []
    targets = []
    for simple in TARGET_RE.findall(match.group(1)):
        fqn = imports.get(simple)
        if not fqn:
            print("!! 无法解析 @Mixin 目标类 %s（%s）：它必须被 import" % (simple, os.path.relpath(path, ROOT)))
            raise SystemExit(1)
        targets.append(fqn)
    return targets


def scan_file(path, sides):
    """返回 (注入点列表, 未解析的 require = 0 注入器数量)。"""
    with open(path, "r", encoding="utf-8") as fh:
        lines = fh.read().splitlines()
    imports = {}
    for line in lines:
        match = IMPORT_RE.match(line)
        if match:
            imports[match.group(2)] = match.group(1) + "." + match.group(2)

    simple_name = os.path.splitext(os.path.basename(path))[0]
    side = sides.get(simple_name)
    if side is None:
        print("!! %s 未在 mixinenhance.mixins.json 中注册" % simple_name)
        raise SystemExit(1)
    targets = resolve_targets("\n".join(lines), imports, path)
    if not targets:
        return [], 0

    entries = []
    unresolved = 0
    index = 0
    while index < len(lines):
        line = lines[index]
        if not (INJECTOR_RE.search(line) and not line.lstrip().startswith("@Mixin")):
            index += 1
            continue

        # 收集完整注解（可能跨行，按括号配平）
        block = [line]
        depth = line.count("(") - line.count(")")
        cursor = index + 1
        while depth > 0 and cursor < len(lines):
            block.append(lines[cursor])
            depth += lines[cursor].count("(") - lines[cursor].count(")")
            cursor += 1
        joined = " ".join(part.strip() for part in block)

        if REQUIRE_ZERO_RE.search(joined):
            # 跳过注解与空行，落到处理器方法声明
            probe = cursor
            while probe < len(lines) and (lines[probe].strip().startswith("@") or not lines[probe].strip()):
                probe += 1
            method = METHOD_RE.match(lines[probe]) if probe < len(lines) else None
            if method:
                handler = method.group(1)
                for target in targets:
                    entries.append((handler, target, simple_name, side))
            else:
                unresolved += 1
                print("!! 未能定位 require = 0 的处理器方法（%s 第 %d 行）" % (os.path.relpath(path, ROOT), index + 1))
        index = cursor
    return entries, unresolved


def build():
    sides = load_sides()
    entries = []
    unresolved = 0
    for dirpath, _dirnames, filenames in os.walk(MIXIN_DIR):
        for name in sorted(filenames):
            if name.endswith(".java"):
                found, missed = scan_file(os.path.join(dirpath, name), sides)
                entries.extend(found)
                unresolved += missed
    if unresolved:
        print("!! 有 %d 个 require = 0 的注入器没能写进清单，请检查上面的行号" % unresolved)
        raise SystemExit(1)
    entries = sorted(set(entries), key=lambda item: (item[2], item[0], item[1]))
    out = ["# 由 tools/gen_injection_manifest.py 自动生成，请勿手改（CI 会校验是否过期）",
           "# 格式：处理器方法名|目标类|mixin|端",
           "# 作用：require = 0 的注入点失效时不会崩、只会静默失效，启动时按本清单核对",
           "# 共 %d 项" % len(entries)]
    out.extend("|".join(entry) for entry in entries)
    return "\n".join(out) + "\n", len(entries)


def main():
    check = "--check" in sys.argv
    content, count = build()
    old = None
    if os.path.isfile(MANIFEST):
        with open(MANIFEST, "r", encoding="utf-8") as fh:
            old = fh.read()
    if old == content:
        print("Injection manifest is up to date (%d entries)." % count)
        return 0
    if check:
        print("Injection manifest is stale. Run: python3 tools/gen_injection_manifest.py")
        old_lines = (old or "").splitlines()
        new_lines = content.splitlines()
        shown = 0
        for index in range(max(len(old_lines), len(new_lines))):
            before = old_lines[index] if index < len(old_lines) else "<missing>"
            after = new_lines[index] if index < len(new_lines) else "<removed>"
            if before != after:
                print("  - %s" % before)
                print("  + %s" % after)
                shown += 1
                if shown >= 5:
                    break
        return 1
    os.makedirs(os.path.dirname(MANIFEST), exist_ok=True)
    with open(MANIFEST, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(content)
    print("Wrote %s (%d entries)." % (os.path.relpath(MANIFEST, ROOT), count))
    return 0


if __name__ == "__main__":
    sys.exit(main())
