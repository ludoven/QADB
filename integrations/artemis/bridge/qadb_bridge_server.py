"""Uvicorn entrypoint; token is injected through a private process environment."""

import os

from qadb_bridge import QadbBridge, SqliteBridgeLedger
from qadb_bridge_app import create_app


_token = os.environ.get("QADB_BRIDGE_TOKEN")
if not _token:
    raise RuntimeError("QADB_BRIDGE_TOKEN must be injected by the managed runtime")

_ledger_path = os.environ.get("QADB_BRIDGE_LEDGER")
if not _ledger_path:
    raise RuntimeError("QADB_BRIDGE_LEDGER must be an owned managed-runtime path")

app = create_app(QadbBridge(_token, ledger=SqliteBridgeLedger(_ledger_path)))
