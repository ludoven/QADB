"""QADB Bridge control core.

This module deliberately contains no device or shell execution.  A managed
worker must call the policy gate immediately before its final actuator dispatch;
without that injection, write-capable tasks are unsupported.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from functools import wraps
from hmac import compare_digest
from pathlib import Path
import re
import sqlite3
from threading import Lock, RLock
from time import monotonic
from uuid import uuid4


class BridgeRejected(RuntimeError):
    pass


def synchronized(method):
    @wraps(method)
    def call(self, *args, **kwargs):
        with self._lock:
            return method(self, *args, **kwargs)
    return call


AUTO_APPROVED_ACTION_KINDS = frozenset({
    "click_sequence",
    "click",
    "long_press",
    "input_text",
    "swipe",
    "press_key",
    "manage_app_launch",
    "open_link",
    "erase_one_char",
    "focus_and_clear_text",
})


class SqliteBridgeLedger:
    """Append-only local control ledger; it does not claim device-side effects."""

    def __init__(self, path: str | Path):
        self._connection = sqlite3.connect(path, check_same_thread=False)
        self._lock = Lock()
        with self._lock:
            self._connection.execute(
                "CREATE TABLE IF NOT EXISTS bridge_ledger ("
                "sequence INTEGER PRIMARY KEY, run_id TEXT NOT NULL, event TEXT NOT NULL, at REAL NOT NULL)"
            )
            self._connection.commit()

    def append(self, run_id: str, event: str, at: float) -> None:
        with self._lock:
            self._connection.execute(
                "INSERT INTO bridge_ledger(run_id, event, at) VALUES (?, ?, ?)", (run_id, event, at)
            )
            self._connection.commit()

    def events(self, run_id: str) -> list[str]:
        with self._lock:
            return [row[0] for row in self._connection.execute(
                "SELECT event FROM bridge_ledger WHERE run_id = ? ORDER BY sequence", (run_id,)
            )]


@dataclass
class Approval:
    approval_id: str
    action_digest: str
    task_version: int
    device_id: str
    action_kind: str
    action_target: str | None
    expires_at: float
    decided: bool = False
    allowed: bool = False


@dataclass
class RunControl:
    run_id: str
    instance_id: str
    device_id: str
    task_version: int
    stopped: bool = False
    worker_finished: bool = False
    heartbeat_deadline: float = 0.0
    pending: dict[str, Approval] = field(default_factory=dict)
    dispatched: dict[str, str] = field(default_factory=dict)
    guidance: dict[str, bool] = field(default_factory=dict)
    ledger: list[str] = field(default_factory=list)


class QadbBridge:
    """Per-instance authenticated ownership and policy state.

    The HTTP adapter is intentionally outside this module: it must provide the
    token in a header, never in a URL, and call these methods for every route.
    """

    def __init__(self, token: str, instance_id: str | None = None, now=monotonic,
                 ledger: SqliteBridgeLedger | None = None):
        if not token:
            raise ValueError("bridge token is required")
        self._token = token
        self.instance_id = instance_id or str(uuid4())
        self._now = now
        self._ledger = ledger
        self._runs: dict[str, RunControl] = {}
        self._lock = RLock()

    def authenticate(self, token: str | None) -> None:
        if token is None or not compare_digest(self._token, token):
            raise BridgeRejected("unauthorized bridge request")

    @synchronized
    def register_run(self, run_id: str, device_id: str, task_version: int) -> RunControl:
        existing = self._runs.get(run_id)
        if existing is not None:
            if (existing.device_id, existing.task_version) != (device_id, task_version):
                raise BridgeRejected("run id already belongs to this instance")
            return existing
        control = RunControl(run_id, self.instance_id, device_id, task_version)
        self._runs[run_id] = control
        self._record(control, "REGISTERED")
        return control

    @synchronized
    def heartbeat(self, run_id: str, lease_seconds: float = 8.0) -> None:
        control = self._owned(run_id)
        if lease_seconds <= 0:
            raise BridgeRejected("heartbeat lease must be positive")
        control.heartbeat_deadline = self._now() + lease_seconds

    @synchronized
    def request_approval(
        self,
        run_id: str,
        action_digest: str,
        ttl_seconds: float = 30.0,
        action_kind: str | None = None,
        action_target: str | None = None,
    ) -> Approval:
        control = self._active(run_id)
        normalized_kind = (action_kind or "unknown").strip().lower()
        if re.fullmatch(r"[a-z][a-z0-9_]{0,63}", normalized_kind) is None:
            raise BridgeRejected("invalid action kind")
        if action_target is not None and (
            len(action_target) > 200 or any(ord(char) < 32 for char in action_target)
        ):
            raise BridgeRejected("invalid action target")
        auto_allowed = normalized_kind in AUTO_APPROVED_ACTION_KINDS
        approval = Approval(
            str(uuid4()),
            action_digest,
            control.task_version,
            control.device_id,
            normalized_kind,
            action_target,
            self._now() + ttl_seconds,
            decided=auto_allowed,
            allowed=auto_allowed,
        )
        control.pending[approval.approval_id] = approval
        self._record(control, f"PREPARED:{approval.approval_id}")
        if auto_allowed:
            self._record(control, f"APPROVAL_AUTO_ALLOWED:{approval.approval_id}:{normalized_kind}")
        return approval

    @synchronized
    def decide_approval(self, run_id: str, approval_id: str, allowed: bool) -> None:
        control = self._active(run_id)
        approval = control.pending.get(approval_id)
        if approval is None or approval.decided or self._now() > approval.expires_at:
            raise BridgeRejected("approval is missing or already consumed")
        approval.decided = True
        approval.allowed = allowed
        self._record(control, f"APPROVAL_{'ALLOWED' if allowed else 'REJECTED'}:{approval_id}")

    @synchronized
    def approval_snapshot(self, run_id: str, approval_id: str) -> dict[str, object]:
        """Returns the decision state without consuming the one-shot approval."""
        control = self._active(run_id)
        approval = control.pending.get(approval_id)
        if approval is None:
            raise BridgeRejected("approval is missing or already consumed")
        expired = self._now() > approval.expires_at
        return {
            "approvalId": approval.approval_id,
            "actionKind": approval.action_kind,
            "actionTarget": approval.action_target,
            "actionDigest": approval.action_digest,
            "deviceId": approval.device_id,
            "taskVersion": approval.task_version,
            "expiresInMs": max(0, int((approval.expires_at - self._now()) * 1000)),
            "pending": not approval.decided and not expired,
            "allowed": approval.decided and approval.allowed and not expired,
            "rejected": approval.decided and not approval.allowed,
            "expired": expired,
        }

    @synchronized
    def dispatch_allowed(self, run_id: str, approval_id: str, action_digest: str, device_id: str, task_version: int) -> None:
        control = self._active(run_id)
        approval = control.pending.get(approval_id)
        if approval is None or not approval.decided or not approval.allowed:
            raise BridgeRejected("action has no approved permission")
        if self._now() > approval.expires_at:
            raise BridgeRejected("approval expired")
        if (approval.action_digest, approval.device_id, approval.task_version) != (action_digest, device_id, task_version):
            raise BridgeRejected("approval binding changed")
        del control.pending[approval_id]
        control.dispatched[approval_id] = action_digest
        self._record(control, f"DISPATCHED:{approval_id}")

    @synchronized
    def dispatch_prepared(self, run_id: str, approval_id: str, action_digest: str) -> None:
        """Dispatches using the registered device/version binding only.

        The worker supplies no mutable device or task-version fields at this
        boundary, so it cannot change an approval's target after it was issued.
        """
        control = self._owned(run_id)
        self.dispatch_allowed(
            run_id,
            approval_id,
            action_digest,
            control.device_id,
            control.task_version,
        )

    @synchronized
    def record_actuator_result(self, run_id: str, approval_id: str, action_digest: str, ok: bool) -> None:
        """Records only that the gated worker reported its final dispatch result.

        This is deliberately not an assertion of a user-visible device effect;
        callers must attach an independent post-action observation for that.
        """
        control = self._owned(run_id)
        if control.dispatched.get(approval_id) != action_digest:
            raise BridgeRejected("actuator result is not bound to a dispatched approval")
        del control.dispatched[approval_id]
        self._record(
            control,
            f"ACTUATOR_{'OK' if ok else 'FAILED'}:{approval_id}:{action_digest}",
        )

    @synchronized
    def stop(self, run_id: str) -> RunControl:
        control = self._owned(run_id)
        control.stopped = True
        control.pending.clear()
        self._record(control, "STOPPING")
        return control

    @synchronized
    def worker_finished(self, run_id: str) -> None:
        control = self._owned(run_id)
        control.stopped = True
        control.pending.clear()
        control.worker_finished = True
        self._record(control, "WORKER_FINISHED")

    @synchronized
    def task_version_changed(self, run_id: str, version: int) -> None:
        control = self._owned(run_id)
        if version <= control.task_version:
            raise BridgeRejected("task version must advance")
        if control.dispatched:
            raise BridgeRejected("in-flight actuator must settle before task version changes")
        control.task_version = version
        control.pending.clear()
        control.dispatched.clear()
        self._record(control, f"TASK_VERSION:{version}")

    @synchronized
    def receive_guidance(self, run_id: str, text: str) -> str:
        """Records a non-emergency request; only worker injection may mark it applied."""
        if not text.strip():
            raise BridgeRejected("guidance must not be empty")
        control = self._active(run_id)
        guidance_id = str(uuid4())
        control.guidance[guidance_id] = False
        self._record(control, f"GUIDANCE_RECEIVED:{guidance_id}")
        return guidance_id

    @synchronized
    def mark_guidance_applied(self, run_id: str, guidance_id: str) -> None:
        control = self._active(run_id)
        if guidance_id not in control.guidance:
            raise BridgeRejected("guidance is not owned by this run")
        control.guidance[guidance_id] = True
        self._record(control, f"GUIDANCE_APPLIED:{guidance_id}")

    @synchronized
    def control_snapshot(self, run_id: str) -> dict[str, object]:
        control = self._owned(run_id)
        return {
            "runtimeInstanceId": control.instance_id,
            "runId": control.run_id,
            "stopping": control.stopped,
            "workerFinished": control.worker_finished,
            "quiescent": control.stopped and control.worker_finished and not control.dispatched,
            "pendingApprovals": sorted(
                approval_id
                for approval_id, approval in control.pending.items()
                if not approval.decided and self._now() <= approval.expires_at
            ),
            "dispatchedApprovals": sorted(control.dispatched),
            "guidance": control.guidance.copy(),
            "heartbeatExpired": self._now() > control.heartbeat_deadline,
        }

    def _owned(self, run_id: str) -> RunControl:
        try:
            return self._runs[run_id]
        except KeyError as error:
            raise BridgeRejected("run is not owned by this runtime instance") from error

    def _active(self, run_id: str) -> RunControl:
        control = self._owned(run_id)
        if control.stopped:
            raise BridgeRejected("stop barrier is active")
        if self._now() > control.heartbeat_deadline:
            control.stopped = True
            control.pending.clear()
            self._record(control, "HEARTBEAT_EXPIRED")
            raise BridgeRejected("host heartbeat expired")
        return control

    def _record(self, control: RunControl, event: str) -> None:
        control.ledger.append(event)
        if self._ledger is not None:
            self._ledger.append(control.run_id, event, self._now())
