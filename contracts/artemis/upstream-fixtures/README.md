# Artemis upstream contract fixtures

These fixtures are deterministic, **synthetic** responses derived from the
pinned upstream source contract in `integrations/artemis/upstream.lock.json`.
They are not captured from a running Artemis daemon and contain no device data,
credentials, screenshots, or model output.

They allow the Kotlin protocol client to validate admission, rejection,
not-persisted-yet session lookup, targeted stop, and SSE ownership handling
without starting Python or connecting an Android device. Replace or supplement
them with redacted runtime captures only after AR-01 environment verification.
