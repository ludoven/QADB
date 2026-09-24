#!/usr/bin/env python3
"""Verify that an external Artemis checkout matches QADB's pinned runtime inputs."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import sys
from tempfile import TemporaryDirectory

from test_worker_policy import run as test_worker_policy


ROOT = Path(__file__).resolve().parent


def git(checkout: Path, *args: str) -> str:
    return subprocess.check_output(["git", "-C", str(checkout), *args], text=True).strip()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--upstream-dir", type=Path, required=True)
    args = parser.parse_args()
    manifest = json.loads((ROOT / "runtime-manifest.json").read_text(encoding="utf-8"))
    checkout = args.upstream_dir.resolve()
    expected_commit = manifest["upstream"]["commit"]
    actual_commit = git(checkout, "rev-parse", "HEAD")
    if actual_commit != expected_commit:
        raise SystemExit(f"upstream commit mismatch: expected {expected_commit}, got {actual_commit}")
    patches: list[Path] = []
    for item in manifest.get("patches", []):
        patch = ROOT / item["path"]
        actual_hash = hashlib.sha256(patch.read_bytes()).hexdigest()
        if actual_hash != item["sha256"]:
            raise SystemExit(f"patch hash mismatch: {patch}")
        patches.append(patch)
    # Validate the ordered patch stack against a clean copy of the pinned
    # commit. Independent reverse checks are invalid when later patches modify
    # files introduced by earlier patches.
    with TemporaryDirectory(prefix="qadb-artemis-verify-") as directory:
        clean_checkout = Path(directory) / "upstream"
        subprocess.check_call([
            "git", "clone", "--no-hardlinks", "--quiet", str(checkout), str(clean_checkout)
        ])
        for patch in patches:
            if subprocess.run(
                ["git", "-C", str(clean_checkout), "apply", "--check", str(patch)],
                capture_output=True,
            ).returncode != 0:
                raise SystemExit(f"patch stack cannot apply in order: {patch}")
            subprocess.check_call([
                "git", "-C", str(clean_checkout), "apply", str(patch)
            ])
        if not test_worker_policy(clean_checkout):
            raise SystemExit("patched worker policy tests failed")
    for item in manifest.get("bridge", {}).get("payload", []):
        payload = ROOT / item["path"]
        actual_hash = hashlib.sha256(payload.read_bytes()).hexdigest()
        if actual_hash != item["sha256"]:
            raise SystemExit(f"bridge payload hash mismatch: {payload}")
    print("QADB Artemis runtime inputs verified")
    return 0


if __name__ == "__main__":
    sys.exit(main())
