#!/usr/bin/env python3
"""Install a git pre-commit hook that runs tools/check_unused_imports.py.

.git/hooks is not version-controlled, so re-run this script after cloning on a new machine.

Usage:
    python3 tools/install_git_hooks.py
"""
import os
import sys

HOOK = """#!/bin/sh
# Pre-commit check: unused imports (installed by tools/install_git_hooks.py)
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
PY=""
for c in python3 python py; do
    if command -v "$c" >/dev/null 2>&1 && "$c" -c "import sys" >/dev/null 2>&1; then
        PY="$c"; break
    fi
done
if [ -z "$PY" ]; then
    echo "check-unused-imports: no working Python found, skipping."
    exit 0
fi
"$PY" "$ROOT/tools/check_unused_imports.py" || exit 1
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
    print("The unused-import check will now run before every git commit.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
