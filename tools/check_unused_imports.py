#!/usr/bin/env python3
"""Unused-import checker (heuristic, cross-platform).

For each Java file, take the simple name of every import and check whether it appears
as a word in the file body (excluding import lines). If it never appears, the import is
reported as possibly unused. Wildcard imports (import x.*;) are skipped.

Exit code: 0 = OK; 1 = possibly-unused imports found (usable as a git hook / CI gate).

Usage:
    python3 tools/check_unused_imports.py [source_dir]
"""
import os
import re
import sys

IMPORT_RE = re.compile(r"^\s*import\s+(?:static\s+)?([\w.]+)\.(\w+);")


def check(root):
    issues = []
    for dirpath, _dirnames, filenames in os.walk(root):
        for name in filenames:
            if not name.endswith(".java"):
                continue
            path = os.path.join(dirpath, name)
            with open(path, "r", encoding="utf-8") as fh:
                text = fh.read()
            lines = text.splitlines()
            body = "\n".join(l for l in lines if not re.match(r"^\s*import\s", l))
            for line in lines:
                m = IMPORT_RE.match(line)
                if not m:
                    continue
                simple = m.group(2)
                if not re.search(r"\b" + re.escape(simple) + r"\b", body):
                    issues.append("%s: %s" % (os.path.relpath(path, root), line.strip()))
    return issues


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    root = sys.argv[1] if len(sys.argv) > 1 else os.path.join(here, "..", "src", "main", "java")
    root = os.path.abspath(root)
    if not os.path.isdir(root):
        print("Source directory not found: %s" % root)
        return 0
    issues = check(root)
    if issues:
        print("Possibly unused imports found:")
        for i in issues:
            print("  " + i)
        print("Total: %d. Please clean them up before committing." % len(issues))
        return 1
    print("Import check passed: no unused imports.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
