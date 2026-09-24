import unittest
from tempfile import TemporaryDirectory
from threading import Thread

from qadb_bridge import BridgeRejected, QadbBridge, SqliteBridgeLedger


class BridgeTest(unittest.TestCase):
    def setUp(self):
        self.now = 100.0
        self.bridge = QadbBridge("secret", instance_id="runtime", now=lambda: self.now)
        self.bridge.register_run("run", "device", 1)
        self.bridge.heartbeat("run")

    def test_token_and_ownership_are_required(self):
        with self.assertRaises(BridgeRejected):
            self.bridge.authenticate("wrong")
        self.bridge.authenticate("secret")
        with self.assertRaises(BridgeRejected):
            self.bridge.stop("other")

    def test_worker_may_repeat_host_registration_only_with_same_binding(self):
        self.assertIs(self.bridge._runs["run"], self.bridge.register_run("run", "device", 1))
        with self.assertRaises(BridgeRejected):
            self.bridge.register_run("run", "other-device", 1)

    def test_rejection_and_stop_prevent_dispatch(self):
        approval = self.bridge.request_approval("run", "tap:1")
        self.bridge.decide_approval("run", approval.approval_id, False)
        with self.assertRaises(BridgeRejected):
            self.bridge.dispatch_allowed("run", approval.approval_id, "tap:1", "device", 1)
        self.bridge.stop("run")
        with self.assertRaises(BridgeRejected):
            self.bridge.request_approval("run", "tap:2")

    def test_version_and_heartbeat_invalidate_permission(self):
        approval = self.bridge.request_approval("run", "tap:1")
        self.bridge.decide_approval("run", approval.approval_id, True)
        self.bridge.task_version_changed("run", 2)
        with self.assertRaises(BridgeRejected):
            self.bridge.dispatch_allowed("run", approval.approval_id, "tap:1", "device", 1)
        self.now = 109.0
        with self.assertRaises(BridgeRejected):
            self.bridge.request_approval("run", "tap:2")

    def test_guidance_is_received_but_not_claimed_applied(self):
        guidance_id = self.bridge.receive_guidance("run", "改为第二个候选")
        self.assertFalse(self.bridge.control_snapshot("run")["guidance"][guidance_id])
        self.bridge.mark_guidance_applied("run", guidance_id)
        self.assertTrue(self.bridge.control_snapshot("run")["guidance"][guidance_id])

    def test_control_ledger_is_persisted_without_claiming_device_effect(self):
        with TemporaryDirectory() as directory:
            ledger = SqliteBridgeLedger(f"{directory}/bridge.sqlite")
            bridge = QadbBridge("secret", instance_id="runtime", now=lambda: self.now, ledger=ledger)
            bridge.register_run("persisted", "device", 1)
            bridge.heartbeat("persisted")
            approval = bridge.request_approval("persisted", "tap:1")
            bridge.decide_approval("persisted", approval.approval_id, True)
            bridge.dispatch_allowed("persisted", approval.approval_id, "tap:1", "device", 1)
            self.assertEqual(
                ["REGISTERED", f"PREPARED:{approval.approval_id}",
                 f"APPROVAL_ALLOWED:{approval.approval_id}", f"DISPATCHED:{approval.approval_id}"],
                ledger.events("persisted"),
            )

    def test_persistent_ledger_accepts_route_worker_thread(self):
        with TemporaryDirectory() as directory:
            ledger = SqliteBridgeLedger(f"{directory}/bridge.sqlite")
            failures: list[BaseException] = []

            def append_from_route_thread():
                try:
                    ledger.append("run", "REGISTERED", self.now)
                except BaseException as error:
                    failures.append(error)

            thread = Thread(target=append_from_route_thread)
            thread.start()
            thread.join()
            self.assertEqual([], failures)
            self.assertEqual(["REGISTERED"], ledger.events("run"))

    def test_approval_status_and_actuator_record_keep_effect_claims_separate(self):
        approval = self.bridge.request_approval("run", "a" * 64)
        self.assertTrue(self.bridge.approval_snapshot("run", approval.approval_id)["pending"])
        self.bridge.decide_approval("run", approval.approval_id, True)
        self.assertTrue(self.bridge.approval_snapshot("run", approval.approval_id)["allowed"])
        self.bridge.dispatch_allowed("run", approval.approval_id, "a" * 64, "device", 1)
        self.bridge.record_actuator_result("run", approval.approval_id, "a" * 64, ok=True)
        self.assertTrue(self.bridge._runs["run"].ledger[-1].startswith("ACTUATOR_OK:"))
        with self.assertRaises(BridgeRejected):
            self.bridge.record_actuator_result("run", approval.approval_id, "a" * 64, ok=True)

    def test_control_snapshot_lists_only_undecided_unexpired_approvals(self):
        allowed = self.bridge.request_approval("run", "tap:allowed")
        expired = self.bridge.request_approval("run", "tap:expired", ttl_seconds=1.0)
        pending = self.bridge.request_approval("run", "tap:pending")
        self.bridge.decide_approval("run", allowed.approval_id, True)
        self.now = 102.0

        self.assertEqual(
            [pending.approval_id],
            self.bridge.control_snapshot("run")["pendingApprovals"],
        )

    def test_routine_interactions_are_auto_approved_without_host_prompt(self):
        for action_kind in (
            "click_sequence", "click", "long_press", "input_text", "swipe",
            "press_key", "manage_app_launch", "open_link", "erase_one_char",
            "focus_and_clear_text",
        ):
            approval = self.bridge.request_approval(
                "run",
                action_kind.ljust(64, "a")[:64],
                action_kind=action_kind,
            )
            snapshot = self.bridge.approval_snapshot("run", approval.approval_id)
            self.assertTrue(snapshot["allowed"])
            self.assertNotIn(
                approval.approval_id,
                self.bridge.control_snapshot("run")["pendingApprovals"],
            )
            self.bridge.dispatch_allowed(
                "run", approval.approval_id, action_kind.ljust(64, "a")[:64], "device", 1
            )

    def test_app_stop_and_unknown_actions_still_require_confirmation(self):
        for action_kind in ("press_key_enter", "manage_app_stop", "unknown"):
            approval = self.bridge.request_approval(
                "run", "b" * 64, action_kind=action_kind
            )
            self.assertTrue(self.bridge.approval_snapshot("run", approval.approval_id)["pending"])
            self.assertIn(
                approval.approval_id,
                self.bridge.control_snapshot("run")["pendingApprovals"],
            )

    def test_approval_snapshot_exposes_bound_metadata_and_expired_decision_fails(self):
        approval = self.bridge.request_approval(
            "run", "a" * 64, ttl_seconds=2.0,
            action_kind="manage_app_uninstall", action_target="com.example.app",
        )
        snapshot = self.bridge.approval_snapshot("run", approval.approval_id)
        self.assertEqual(snapshot["actionTarget"], "com.example.app")
        self.assertEqual(snapshot["deviceId"], "device")
        self.assertEqual(snapshot["taskVersion"], 1)
        self.assertEqual(snapshot["actionDigest"], "a" * 64)
        self.now = 103.0
        with self.assertRaises(BridgeRejected):
            self.bridge.decide_approval("run", approval.approval_id, True)

    def test_stop_waits_for_worker_and_inflight_actuator_result(self):
        approval = self.bridge.request_approval("run", "c" * 64, action_kind="click")
        self.bridge.dispatch_allowed("run", approval.approval_id, "c" * 64, "device", 1)
        self.bridge.stop("run")
        self.assertFalse(self.bridge.control_snapshot("run")["quiescent"])
        self.bridge.worker_finished("run")
        self.assertFalse(self.bridge.control_snapshot("run")["quiescent"])
        self.bridge.record_actuator_result("run", approval.approval_id, "c" * 64, ok=False)
        self.assertTrue(self.bridge.control_snapshot("run")["quiescent"])

    def test_worker_finish_closes_gate_and_version_cannot_skip_inflight_action(self):
        approval = self.bridge.request_approval("run", "d" * 64, action_kind="click")
        self.bridge.dispatch_allowed("run", approval.approval_id, "d" * 64, "device", 1)
        with self.assertRaises(BridgeRejected):
            self.bridge.task_version_changed("run", 2)
        self.bridge.record_actuator_result("run", approval.approval_id, "d" * 64, ok=True)
        self.bridge.worker_finished("run")
        with self.assertRaises(BridgeRejected):
            self.bridge.request_approval("run", "e" * 64)


if __name__ == "__main__":
    unittest.main()
