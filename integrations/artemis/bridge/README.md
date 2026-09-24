# QADB Bridge core

`qadb_bridge.py` is the dependency-free policy/control core for the managed
Artemis runtime. It is not an HTTP server and has no device-action API.
The managed runtime adapter must supply header-based authentication and invoke
`dispatch_allowed` immediately before every final actuator dispatch.

`qadb_bridge_app.py` exposes only the narrow `/qadb/v1/*` control routes, with
OpenAPI and docs disabled. Production must call `create_router()` and mount it
on the already-running Artemis loopback service; `qadb_bridge_server.py` is
only a standalone test adapter and must not be used by the managed runtime.
it refuses to start unless its token and an owned SQLite path arrive as
`QADB_BRIDGE_TOKEN` and `QADB_BRIDGE_LEDGER` in the managed process environment.
The token must not appear in a command line, URL, trace, or normal log.

Implemented invariants:

- random per-instance ownership and constant-time token comparison;
- no cross-instance run control;
- heartbeat expiry activates a stop barrier;
- approvals bind action digest, device and task version, are one-shot and expire;
- the read-only approval snapshot exposes action kind, optional target, digest, device, version and remaining validity; the host rejects manual approval when the target or binding is missing;
- routine interaction kinds (tap, swipe, text input, non-Enter key press, link/app launch) are auto-approved; the worker sends only app-management target names or a fixed Enter label, not coordinates, typed text, or URLs;
- Enter/submit key, app stop, and unknown action kinds remain pending for explicit host confirmation;
- a stop or task-version change clears pending permission; an in-flight dispatch stays tracked until its result is reported;
- optional SQLite ledger transitions are durable control facts only, not proof of a device effect.
- `/stop` reports `quiescent=true` only after the pinned worker reports cleanup completion and no tracked actuator remains in flight. The authenticated `/worker-finished` route cannot by itself claim a device effect.

The worker flow is fail-closed: it registers a run, prepares a canonical action
digest, waits for QADB's one-shot decision, dispatches the same digest, then
records the worker-reported actuator result. `ACTUATOR_OK` means that the
worker reported a successful final dispatch; it is **not** independent proof
that Android reached the intended visible state.

It must not be enabled for write-capable Artemis workers until the pinned
upstream `QadbPolicyActuator` patch is applied and the host supplies an active
heartbeat. The bridge intentionally stops all later dispatches on heartbeat
expiry, task-version changes, stop, missing approval, or action-digest mismatch.

The pinned worker cleanup patch reports `/worker-finished` after background ADB
tasks, tracing, device disconnect, and lock release. If cleanup or an actuator
result is missing, QADB keeps the stop unconfirmed and blocks a new task on the
same device. A restart of the managed runtime loses its in-memory run control;
the desktop does not infer quiescence from an absent run.
