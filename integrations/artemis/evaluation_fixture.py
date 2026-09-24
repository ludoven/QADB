#!/usr/bin/env python3
"""Operate only QADB's synthetic AR-06 Android evaluation fixture.

This utility intentionally never targets Settings, user apps, accounts, or
provider credentials. It restarts the fixture to reset its in-app synthetic
state and reads its independently recorded state through Android's debug-only
``run-as`` boundary.
"""

from __future__ import annotations

import argparse
import json
import subprocess
import sys


PACKAGE = "com.ludoven.qadb.artemisevaluation"
ACTIVITY = f"{PACKAGE}/.EvaluationFixtureActivity"


def adb(serial: str, *args: str) -> subprocess.CompletedProcess[bytes]:
    return subprocess.run(
        ["adb", "-s", serial, *args],
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )


def reset(serial: str) -> dict[str, object]:
    adb(serial, "shell", "am", "force-stop", PACKAGE)
    adb(serial, "shell", "am", "start", "-W", "-n", ACTIVITY)
    state = truth(serial)
    expected = {
        "text": "",
        "selectedCandidate": "none",
        "delayedState": "idle",
        "eventCount": 0,
    }
    if state != expected:
        raise RuntimeError(f"fixture reset did not produce its initial synthetic state: {state}")
    return state


def truth(serial: str) -> dict[str, object]:
    result = adb(serial, "exec-out", "run-as", PACKAGE, "cat", "files/state.json")
    try:
        return json.loads(result.stdout.decode("utf-8"))
    except json.JSONDecodeError as error:
        raise RuntimeError("evaluation fixture returned invalid state JSON") from error


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", required=True)
    parser.add_argument("command", choices=("reset", "truth"))
    args = parser.parse_args()
    try:
        value = reset(args.serial) if args.command == "reset" else truth(args.serial)
    except subprocess.CalledProcessError as error:
        detail = error.stderr.decode("utf-8", errors="replace").strip()
        raise SystemExit(f"fixture ADB command failed: {detail or error}") from error
    print(json.dumps(value, ensure_ascii=False, sort_keys=True))
    return 0


if __name__ == "__main__":
    sys.exit(main())
