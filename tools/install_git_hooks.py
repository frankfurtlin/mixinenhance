#!/usr/bin/env python3
"""Install a git pre-commit hook that runs the project's static checks.

Checks: unused imports (tools/check_unused_imports.py) and whether the injection
manifest is up to date (tools/gen_injection_manifest.py --check).

.git/hooks is not version-controlled, so re-run this script after cloning on a new machine.

Usage:
    python3 tools/install_git_hooks.py
"""
import os
import sys

HOOK = """#!/bin/sh
# Pre-commit checks: unused imports + injection manifest freshness
# (installed by tools/install_git_hooks.py)
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
PY=""
for c in python3 python py; do
    if command -v "$c" >/dev/null 2>&1 && "$c" -c "import sys" >/dev/null 2>&1; then
        PY="$c"; break
    fi
done
if [ -z "$PY" ]; then
    echo "pre-commit: no working Python found, skipping checks."
    exit 0
fi
"$PY" "$ROOT/tools/check_unused_imports.py" || exit 1
"$PY" "$ROOT/tools/gen_injection_manifest.py" --check || exit 1
"""


def main():
    here = os.path.dirname(os.path.abspath(__file__))
    root = os.path.abspath(os.path.join(here, ".."))
    if not os.path.isdir(os.path.join(root, ".git")):
        print("No .git directory found under: %s" % root)
        return 1
    hook_dir = os.path.join(root, ".git", "hooks")
    os.makedirs(hook_dir, exist_ok=True)
    hook_path = os.path.join(hook_dir, "pre-commit")
    with open(hook_path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(HOOK)
    print("Installed pre-commit hook: %s" % hook_path)
    print("Unused-import and injection-manifest checks will now run before every git commit.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
