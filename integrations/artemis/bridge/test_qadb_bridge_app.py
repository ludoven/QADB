import unittest

from fastapi.testclient import TestClient

from qadb_bridge import QadbBridge
from qadb_bridge_app import create_app, create_router


class BridgeAppTest(unittest.TestCase):
    def test_only_narrow_control_routes_are_registered(self):
        bridge = QadbBridge("secret", instance_id="runtime")
        app = create_app(bridge)
        self.assertEqual(app.docs_url, None)
        paths = {route.path for route in create_router(bridge).routes}
        self.assertIn("/qadb/v1/handshake", paths)
        self.assertIn("/qadb/v1/runs/{run_id}/stop", paths)
        self.assertIn("/qadb/v1/runs/{run_id}/worker-finished", paths)
        self.assertIn("/qadb/v1/runs/{run_id}/actions", paths)
        self.assertIn("/qadb/v1/runs/{run_id}/approvals/{approval_id}/dispatch", paths)
        self.assertNotIn("/api/run", paths)
        self.assertNotIn("/docs", paths)

    def test_registration_validates_the_host_contract_without_model_binding(self):
        bridge = QadbBridge("secret", instance_id="runtime")
        client = TestClient(create_app(bridge))
        headers = {"X-QADB-Token": "secret"}

        accepted = client.post(
            "/qadb/v1/runs/run-1",
            headers=headers,
            json={"device_id": "emulator-5554", "task_version": 0},
        )
        self.assertEqual(accepted.status_code, 200)
        self.assertEqual(accepted.json()["runId"], "run-1")

        rejected = client.post(
            "/qadb/v1/runs/run-2",
            headers=headers,
            json={"device_id": "", "task_version": 0},
        )
        self.assertEqual(rejected.status_code, 400)

    def test_routine_action_is_auto_approved_without_exposing_arguments(self):
        bridge = QadbBridge("secret", instance_id="runtime")
        client = TestClient(create_app(bridge))
        headers = {"X-QADB-Token": "secret"}
        client.post(
            "/qadb/v1/runs/run-safe",
            headers=headers,
            json={"device_id": "emulator-5554", "task_version": 0},
        )
        client.post("/qadb/v1/runs/run-safe/heartbeat", headers=headers)

        prepared = client.post(
            "/qadb/v1/runs/run-safe/actions",
            headers=headers,
            json={"action_digest": "a" * 64, "action_kind": "input_text"},
        )

        self.assertEqual(prepared.status_code, 200)
        self.assertFalse(prepared.json()["pending"])
        self.assertTrue(prepared.json()["autoApproved"])
        approval_id = prepared.json()["approvalId"]
        status = client.get(
            f"/qadb/v1/runs/run-safe/approvals/{approval_id}", headers=headers
        )
        self.assertTrue(status.json()["allowed"])
        self.assertEqual(status.json()["actionKind"], "input_text")

    def test_manual_approval_returns_bound_target_without_action_arguments(self):
        bridge = QadbBridge("secret", instance_id="runtime")
        client = TestClient(create_app(bridge))
        headers = {"X-QADB-Token": "secret"}
        client.post("/qadb/v1/runs/run-manual", headers=headers,
                    json={"device_id": "emulator-5554", "task_version": 0})
        client.post("/qadb/v1/runs/run-manual/heartbeat", headers=headers)
        prepared = client.post("/qadb/v1/runs/run-manual/actions", headers=headers,
                               json={"action_digest": "b" * 64,
                                     "action_kind": "manage_app_uninstall",
                                     "action_target": "com.example.app"})
        self.assertEqual(prepared.status_code, 200)
        approval_id = prepared.json()["approvalId"]
        status = client.get(f"/qadb/v1/runs/run-manual/approvals/{approval_id}", headers=headers)
        self.assertEqual(status.json()["actionTarget"], "com.example.app")
        self.assertEqual(status.json()["actionDigest"], "b" * 64)
        self.assertEqual(status.json()["deviceId"], "emulator-5554")
        self.assertTrue(status.json()["pending"])

    def test_stop_requires_worker_finish_before_quiescence_ack(self):
        bridge = QadbBridge("secret", instance_id="runtime")
        client = TestClient(create_app(bridge))
        headers = {"X-QADB-Token": "secret"}
        client.post("/qadb/v1/runs/run-stop", headers=headers,
                    json={"device_id": "emulator-5554", "task_version": 0})
        stopped = client.post("/qadb/v1/runs/run-stop/stop", headers=headers)
        self.assertFalse(stopped.json()["quiescent"])
        self.assertEqual(client.post("/qadb/v1/runs/run-stop/worker-finished").status_code, 401)
        finished = client.post("/qadb/v1/runs/run-stop/worker-finished", headers=headers)
        self.assertTrue(finished.json()["quiescent"])
        self.assertTrue(client.get("/qadb/v1/runs/run-stop/control", headers=headers).json()["quiescent"])


if __name__ == "__main__":
    unittest.main()
