"""Exercise the patched worker policy without a Provider or Android device."""

from __future__ import annotations

import importlib.util
from pathlib import Path
import sys
import types
import unittest


def load_policy(checkout: Path):
    # The policy only needs ActionResult for annotations in these tests.
    action_types = types.ModuleType("artemis.mcp.action_types")
    action_types.ActionResult = type("ActionResult", (), {})
    previous = sys.modules.get(action_types.__name__)
    sys.modules[action_types.__name__] = action_types
    try:
        path = checkout / "artemis/mcp/actuators/qadb_policy.py"
        spec = importlib.util.spec_from_file_location("qadb_worker_policy_test", path)
        if spec is None or spec.loader is None:
            raise RuntimeError(f"cannot load worker policy at {path}")
        module = importlib.util.module_from_spec(spec)
        sys.modules[spec.name] = module
        spec.loader.exec_module(module)
        return module
    finally:
        if previous is None:
            sys.modules.pop(action_types.__name__, None)
        else:
            sys.modules[action_types.__name__] = previous


class WorkerPolicyTest(unittest.IsolatedAsyncioTestCase):
    policy = None

    async def asyncSetUp(self):
        self.authorizer = self.policy._QadbHttpAuthorizer(
            "http://127.0.0.1:8765", "test-token", "test-run", "test-device", 0
        )
        self.requests = []

        async def registered():
            return None

        async def request(method, path, payload=None):
            self.requests.append((method, path, payload))
            if method == "POST" and path.endswith("/actions"):
                return {"approvalId": "approval"}
            if method == "GET":
                return {"pending": False, "allowed": False}
            raise AssertionError("rejected action must never dispatch")

        self.authorizer._ensure_registered = registered
        self.authorizer._request = request

    async def test_enter_requires_bridge_decision_and_never_dispatches_when_rejected(self):
        for key in ("KEYCODE_ENTER", 66, "KEYCODE_NUMPAD_ENTER", 160):
            with self.subTest(key=key):
                self.requests.clear()
                with self.assertRaises(self.policy.BridgePolicyError):
                    await self.authorizer.authorize("press_key", {"key": key})
                self.assertEqual(self.requests[0][2]["action_kind"], "press_key_enter")
                self.assertEqual(self.requests[0][2]["action_target"], "Enter / submit key")
                self.assertFalse(any(path.endswith("/dispatch") for _, path, _ in self.requests))

    async def test_regular_key_keeps_routine_classification(self):
        with self.assertRaises(self.policy.BridgePolicyError):
            await self.authorizer.authorize("press_key", {"key": "KEYCODE_BACK"})
        self.assertEqual(self.requests[0][2]["action_kind"], "press_key")

    async def test_manage_app_target_is_sent_for_manual_review(self):
        with self.assertRaises(self.policy.BridgePolicyError):
            await self.authorizer.authorize(
                "manage_app", {"action": "uninstall", "app_name": "com.example.app"}
            )
        self.assertEqual(self.requests[0][2]["action_kind"], "manage_app_uninstall")
        self.assertEqual(self.requests[0][2]["action_target"], "com.example.app")

    async def test_rejected_action_does_not_reach_final_actuator(self):
        class Delegate:
            calls = 0

            async def press_key(self, key):
                self.calls += 1
                return types.SimpleNamespace(ok=True)

        delegate = Delegate()
        guarded = self.policy.QadbPolicyActuator(delegate, self.authorizer)
        with self.assertRaises(self.policy.BridgePolicyError):
            await guarded.press_key("ENTER")
        self.assertEqual(delegate.calls, 0)

    async def test_input_text_is_represented_by_digest_without_raw_text(self):
        with self.assertRaises(self.policy.BridgePolicyError):
            await self.authorizer.authorize("input_text", {"text": "private sample"})
        self.assertEqual(self.requests[0][2]["action_kind"], "input_text")
        self.assertNotIn("private sample", str(self.requests))


def run(checkout: Path) -> bool:
    WorkerPolicyTest.policy = load_policy(checkout)
    result = unittest.TextTestRunner().run(
        unittest.defaultTestLoader.loadTestsFromTestCase(WorkerPolicyTest)
    )
    return result.wasSuccessful()
