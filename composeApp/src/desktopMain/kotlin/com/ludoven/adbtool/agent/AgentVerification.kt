package com.ludoven.adbtool.agent

enum class AgentTaskOutcome {
    RUNNING,
    ENGINE_FINISHED,
    VERIFIED_SUCCESS,
    PARTIAL,
    NEEDS_USER,
    FAILED,
    CANCELLED,
    OUTCOME_UNKNOWN;

    val isTerminal: Boolean
        get() = this != RUNNING
}

enum class AgentVerificationLevel {
    NONE,
    ENGINE_REPORTED,
    VISUAL,
    DETERMINISTIC,
    HUMAN_CONFIRMED
}

enum class AgentVerificationVerdict {
    UNVERIFIED,
    VERIFIED,
    FAILED,
    UNKNOWN
}

enum class AgentEvidenceSource {
    NONE,
    SYSTEM_PROBE,
    DETERMINISTIC_PREDICATE,
    ARTEMIS_CHECKER,
    VISUAL_REVIEW,
    HUMAN_CONFIRMED
}

data class AgentVerificationState(
    val verdict: AgentVerificationVerdict = AgentVerificationVerdict.UNVERIFIED,
    val level: AgentVerificationLevel = AgentVerificationLevel.NONE,
    val source: AgentEvidenceSource? = null,
    val evidenceIds: List<String> = emptyList(),
    val summary: String? = null
)

data class AgentPredicateResult(
    val matches: Boolean,
    val reason: String = "",
    /** False means the available evidence cannot prove either the predicate or its negation. */
    val conclusive: Boolean = true
)

class AgentPredicateEvaluator(
    private val deviceGateway: AgentDeviceGateway
) {
    suspend fun evaluate(
        predicate: AgentPredicate,
        deviceId: String,
        observation: AgentObservation,
        resolvedPackages: Map<String, String> = emptyMap()
    ): AgentPredicateResult = when (predicate) {
        AgentPredicate.Unspecified -> AgentPredicateResult(
            false,
            "No deterministic verification condition was provided",
            conclusive = false
        )
        AgentPredicate.Always -> AgentPredicateResult(true)
        is AgentPredicate.All -> {
            val results = predicate.predicates.map { evaluate(it, deviceId, observation, resolvedPackages) }
            results.firstOrNull { it.conclusive && !it.matches }
                ?: results.firstOrNull { !it.conclusive }
                ?: AgentPredicateResult(true)
        }
        is AgentPredicate.Any -> {
            val results = predicate.predicates.map { evaluate(it, deviceId, observation, resolvedPackages) }
            when {
                results.any { it.matches } -> AgentPredicateResult(true)
                results.any { !it.conclusive } -> AgentPredicateResult(
                    false,
                    results.filter { !it.conclusive }.joinToString("; ") { it.reason }.take(500),
                    conclusive = false
                )
                else -> AgentPredicateResult(false, results.joinToString("; ") { it.reason }.take(500))
            }
        }
        is AgentPredicate.Not -> evaluate(predicate.predicate, deviceId, observation, resolvedPackages).let {
            when {
                !it.conclusive -> AgentPredicateResult(false, it.reason, conclusive = false)
                it.matches -> AgentPredicateResult(false, "Negated condition matched")
                else -> AgentPredicateResult(true)
            }
        }
        is AgentPredicate.ForegroundPackage -> {
            val expected = predicate.packageName ?: predicate.sourceStepId?.let(resolvedPackages::get)
            val activityPackage = observation.currentActivity.foregroundComponentPackage()
            val nodePackages = observation.uiNodes.map(UiNodeSnapshot::packageName)
                .filter(String::isNotBlank)
                .toSet()
            when {
                expected.isNullOrBlank() -> AgentPredicateResult(
                    false,
                    "Foreground package predicate is unresolved",
                    conclusive = false
                )
                activityPackage != null -> if (activityPackage == expected) {
                    AgentPredicateResult(true)
                } else {
                    AgentPredicateResult(
                        false,
                        "Expected foreground package $expected, observed $activityPackage"
                    )
                }
                nodePackages.isEmpty() ->
                    AgentPredicateResult(false, "Foreground package evidence is unavailable", conclusive = false)
                nodePackages.size > 1 -> AgentPredicateResult(
                    false,
                    "Foreground package evidence is ambiguous",
                    conclusive = false
                )
                nodePackages.single() == expected -> AgentPredicateResult(true)
                else -> AgentPredicateResult(
                    false,
                    "Expected foreground package $expected, observed ${nodePackages.single()}"
                )
            }
        }
        is AgentPredicate.ActivityMatches -> {
            val regex = runCatching { Regex(predicate.pattern) }.getOrNull()
                ?: return AgentPredicateResult(false, "Activity pattern is invalid", conclusive = false)
            if (observation.currentActivity.isBlank()) {
                AgentPredicateResult(false, "Foreground Activity evidence is unavailable", conclusive = false)
            } else if (regex.containsMatchIn(observation.currentActivity)) AgentPredicateResult(true)
            else AgentPredicateResult(false, "Foreground Activity does not match ${predicate.pattern.take(120)}")
        }
        is AgentPredicate.ElementPresent -> selectorResult(predicate.selector, observation, shouldExist = true)
        is AgentPredicate.ElementAbsent -> selectorResult(predicate.selector, observation, shouldExist = false)
        is AgentPredicate.ElementState -> {
            when (val resolved = SelectorResolver.resolveUnique(predicate.selector, observation.uiNodes)) {
                SelectorResolution.Missing -> if (observation.hasUiEvidence()) {
                    AgentPredicateResult(false, "Expected UI element is absent")
                } else {
                    AgentPredicateResult(false, "UI hierarchy evidence is unavailable", conclusive = false)
                }
                is SelectorResolution.Ambiguous -> AgentPredicateResult(
                    false,
                    "UI selector is ambiguous (${resolved.matchCount} matches)",
                    conclusive = false
                )
                is SelectorResolution.Resolved -> {
                    val node = resolved.node
                    val expected = predicate.state
                    val matches = (expected.enabled == null || expected.enabled == node.enabled) &&
                        (expected.selected == null || expected.selected == node.selected) &&
                        (expected.checked == null || expected.checked == node.checked) &&
                        (expected.editable == null || expected.editable == node.editable)
                    if (matches) AgentPredicateResult(true)
                    else AgentPredicateResult(false, "UI element state does not match the expected state")
                }
            }
        }
        is AgentPredicate.TextPresent -> {
            if (!observation.hasUiEvidence()) {
                AgentPredicateResult(false, "UI hierarchy evidence is unavailable", conclusive = false)
            } else {
                val matches = observation.uiNodes.any { node ->
                    node.text.equals(predicate.text, predicate.ignoreCase) ||
                        node.contentDescription.equals(predicate.text, predicate.ignoreCase)
                }
                if (matches) AgentPredicateResult(true)
                else AgentPredicateResult(false, "Expected text is not present")
            }
        }
        is AgentPredicate.RegisteredSystemProbe -> {
            val actual = deviceGateway.readSystemProbe(deviceId, predicate.probeId)
            when {
                actual == null -> AgentPredicateResult(
                    false,
                    "System probe ${predicate.probeId} is unavailable",
                    conclusive = false
                )
                actual.equals(predicate.expectedValue, ignoreCase = true) -> AgentPredicateResult(true)
                else -> AgentPredicateResult(false, "System probe ${predicate.probeId} returned an unexpected value")
            }
        }
    }

    private fun selectorResult(
        selector: AgentSelector,
        observation: AgentObservation,
        shouldExist: Boolean
    ): AgentPredicateResult = when (val resolved = SelectorResolver.resolveUnique(selector, observation.uiNodes)) {
        SelectorResolution.Missing -> if (!observation.hasUiEvidence()) {
            AgentPredicateResult(false, "UI hierarchy evidence is unavailable", conclusive = false)
        } else if (shouldExist) {
            AgentPredicateResult(false, "Expected UI element is absent")
        } else {
            AgentPredicateResult(true)
        }
        is SelectorResolution.Ambiguous -> AgentPredicateResult(
            false,
            "UI selector is ambiguous (${resolved.matchCount} matches)",
            conclusive = false
        )
        is SelectorResolution.Resolved -> if (shouldExist) {
            AgentPredicateResult(true)
        } else {
            AgentPredicateResult(false, "UI element is still present")
        }
    }
}

private fun AgentObservation.hasUiEvidence(): Boolean = uiHierarchy.isNotBlank() || uiNodes.isNotEmpty()

class AgentGoalVerifier(private val evaluator: AgentPredicateEvaluator) {
    suspend fun verify(
        goal: AgentPredicate,
        deviceId: String,
        observation: AgentObservation,
        resolvedPackages: Map<String, String> = emptyMap()
    ): AgentPredicateResult {
        if (!goal.isDeterministicCompletionGoal()) {
            return AgentPredicateResult(
                false,
                "Task plan has no meaningful deterministic goal",
                conclusive = false
            )
        }
        return evaluator.evaluate(goal, deviceId, observation, resolvedPackages)
    }
}

internal fun AgentPredicate.isDeterministicCompletionGoal(): Boolean = when (this) {
    AgentPredicate.Unspecified,
    AgentPredicate.Always -> false
    is AgentPredicate.All -> predicates.isNotEmpty() && predicates.all { it.isDeterministicCompletionGoal() }
    is AgentPredicate.Any -> predicates.isNotEmpty() && predicates.all { it.isDeterministicCompletionGoal() }
    is AgentPredicate.Not -> predicate.isDeterministicCompletionGoal()
    is AgentPredicate.ForegroundPackage -> !packageName.isNullOrBlank() || !sourceStepId.isNullOrBlank()
    is AgentPredicate.ActivityMatches -> pattern.isMeaningfulActivityPattern()
    is AgentPredicate.ElementPresent -> selector.hasDeterministicConstraint()
    is AgentPredicate.ElementAbsent -> selector.hasDeterministicConstraint()
    is AgentPredicate.ElementState -> selector.hasDeterministicConstraint()
    is AgentPredicate.TextPresent -> text.isNotBlank()
    is AgentPredicate.RegisteredSystemProbe -> probeId.isNotBlank() && expectedValue.isNotBlank()
}

private fun AgentSelector.hasDeterministicConstraint(): Boolean =
    !resourceId.isNullOrBlank() ||
        textAny.any(String::isNotBlank) ||
        contentDescriptionAny.any(String::isNotBlank) ||
        !role.isNullOrBlank() ||
        ancestor?.hasDeterministicConstraint() == true

private fun String.isMeaningfulActivityPattern(): Boolean {
    if (isBlank()) return false
    val regex = runCatching { Regex(this) }.getOrNull() ?: return false
    return TRIVIAL_ACTIVITY_SAMPLES.any { sample -> !regex.containsMatchIn(sample) }
}

private val TRIVIAL_ACTIVITY_SAMPLES = listOf(
    "com.example.alpha/.MainActivity",
    "org.unrelated.beta/.SettingsScreen",
    "plain-value"
)

internal fun AgentObservation.belongsToPackage(packageName: String): Boolean =
    currentActivity.foregroundComponentPackage()?.let { it == packageName }
        ?: uiNodes.map(UiNodeSnapshot::packageName)
            .filter(String::isNotBlank)
            .toSet()
            .singleOrNull()
            ?.let { it == packageName }
        ?: false

internal fun String.foregroundComponentPackage(): String? =
    FOREGROUND_COMPONENT_PATTERN.find(this)?.groupValues?.getOrNull(1)
        ?: trim().takeIf { it.matches(BARE_PACKAGE_PATTERN) }

private val FOREGROUND_COMPONENT_PATTERN = Regex(
    "(?<![A-Za-z0-9_.])([A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+)/[A-Za-z0-9_.$]+(?![A-Za-z0-9_.$])"
)
private val BARE_PACKAGE_PATTERN = Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")
