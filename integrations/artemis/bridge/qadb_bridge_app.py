"""Narrow FastAPI adapter for the QADB Bridge control core.

Mount this app on a private loopback endpoint only.  It intentionally does not
proxy Artemis admin/UI routes or expose action execution routes.
"""

from __future__ import annotations

from fastapi import APIRouter, Body, FastAPI, Header, HTTPException
from pydantic import BaseModel, Field

from qadb_bridge import BridgeRejected, QadbBridge


class ApprovalDecision(BaseModel):
    allowed: bool


class GuidanceRequest(BaseModel):
    text: str = Field(min_length=1, max_length=4000)


class ActionPreparation(BaseModel):
    action_digest: str = Field(min_length=64, max_length=64, pattern=r"^[0-9a-f]{64}$")
    action_kind: str = Field(default="unknown", min_length=1, max_length=64, pattern=r"^[a-z][a-z0-9_]*$")
    action_target: str | None = Field(default=None, max_length=200)


class ActuatorResult(BaseModel):
    action_digest: str = Field(min_length=64, max_length=64, pattern=r"^[0-9a-f]{64}$")
    ok: bool


def create_router(bridge: QadbBridge) -> APIRouter:
    """Create routes for mounting on the already-managed Artemis service."""
    router = APIRouter()

    def guard(token: str | None) -> None:
        try:
            bridge.authenticate(token)
        except BridgeRejected as error:
            raise HTTPException(status_code=401, detail=str(error)) from error

    def owned(call):
        try:
            return call()
        except BridgeRejected as error:
            raise HTTPException(status_code=409, detail=str(error)) from error

    @router.get("/qadb/v1/handshake")
    def handshake(x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        return {
            "protocolVersion": "qadb-bridge-v1",
            "runtimeInstanceId": bridge.instance_id,
            "capabilities": {
                "targetedStop": True,
                "quiescenceAck": True,
                "actionGate": True,
                "approvals": True,
                "guidanceAck": False,
                "events": False,
                "usage": False,
            },
        }

    @router.post("/qadb/v1/runs/{run_id}")
    def register(
        run_id: str,
        payload: dict[str, object] = Body(...),
        x_qadb_token: str | None = Header(default=None),
    ):
        """Validate the narrow host registration payload before Bridge ownership.

        This deliberately avoids framework model binding on the first control
        message.  The desktop client owns this payload and needs a deterministic
        error boundary before any provider or Android operation can begin.
        """
        guard(x_qadb_token)
        device_id = payload.get("device_id")
        task_version = payload.get("task_version")
        if not isinstance(device_id, str) or not device_id.strip():
            raise HTTPException(status_code=400, detail="invalid QADB Bridge device registration")
        if isinstance(task_version, bool) or not isinstance(task_version, int) or task_version < 0:
            raise HTTPException(status_code=400, detail="invalid QADB Bridge task version")
        control = owned(lambda: bridge.register_run(run_id, device_id, task_version))
        return {"runId": control.run_id, "runtimeInstanceId": control.instance_id}

    @router.get("/qadb/v1/runs/{run_id}/control")
    def control(run_id: str, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        return owned(lambda: bridge.control_snapshot(run_id))

    @router.post("/qadb/v1/runs/{run_id}/stop")
    def stop(run_id: str, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        control = owned(lambda: bridge.stop(run_id))
        return {
            "runId": control.run_id,
            "stopping": True,
            "quiescent": control.worker_finished and not control.dispatched,
        }

    @router.post("/qadb/v1/runs/{run_id}/worker-finished")
    def worker_finished(run_id: str, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        owned(lambda: bridge.worker_finished(run_id))
        return {"runId": run_id, "quiescent": owned(lambda: bridge.control_snapshot(run_id))["quiescent"]}

    @router.post("/qadb/v1/runs/{run_id}/approvals/{approval_id}")
    def approval(run_id: str, approval_id: str, payload: ApprovalDecision, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        owned(lambda: bridge.decide_approval(run_id, approval_id, payload.allowed))
        return {"approvalId": approval_id, "accepted": True}

    @router.post("/qadb/v1/runs/{run_id}/actions")
    def prepare_action(run_id: str, payload: ActionPreparation, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        approval = owned(lambda: bridge.request_approval(
            run_id,
            payload.action_digest,
            action_kind=payload.action_kind,
            action_target=payload.action_target,
        ))
        return {
            "approvalId": approval.approval_id,
            "pending": not approval.decided,
            "autoApproved": approval.decided and approval.allowed,
        }

    @router.get("/qadb/v1/runs/{run_id}/approvals/{approval_id}")
    def approval_status(run_id: str, approval_id: str, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        return owned(lambda: bridge.approval_snapshot(run_id, approval_id))

    @router.post("/qadb/v1/runs/{run_id}/approvals/{approval_id}/dispatch")
    def dispatch_action(run_id: str, approval_id: str, payload: ActionPreparation, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        owned(lambda: bridge.dispatch_prepared(run_id, approval_id, payload.action_digest))
        return {"approvalId": approval_id, "dispatched": True}

    @router.post("/qadb/v1/runs/{run_id}/approvals/{approval_id}/result")
    def actuator_result(run_id: str, approval_id: str, payload: ActuatorResult, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        owned(lambda: bridge.record_actuator_result(run_id, approval_id, payload.action_digest, payload.ok))
        return {"approvalId": approval_id, "recorded": True}

    @router.post("/qadb/v1/runs/{run_id}/guidance")
    def guidance(run_id: str, payload: GuidanceRequest, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        guidance_id = owned(lambda: bridge.receive_guidance(run_id, payload.text))
        return {"guidanceId": guidance_id, "received": True, "applied": False}

    @router.post("/qadb/v1/runs/{run_id}/heartbeat")
    def heartbeat(run_id: str, x_qadb_token: str | None = Header(default=None)):
        guard(x_qadb_token)
        owned(lambda: bridge.heartbeat(run_id))
        return {"accepted": True}

    return router


def create_app(bridge: QadbBridge) -> FastAPI:
    """Standalone test adapter; production mounts ``create_router`` instead."""
    app = FastAPI(docs_url=None, redoc_url=None, openapi_url=None)
    app.include_router(create_router(bridge))
    return app
