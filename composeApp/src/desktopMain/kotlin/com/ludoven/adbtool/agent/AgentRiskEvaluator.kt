package com.ludoven.adbtool.agent

data class AgentRiskAssessment(
    val level: AgentRiskLevel,
    val reason: String = ""
)

class AgentRiskEvaluator(
    private val approvalPreferences: AgentApprovalPreferences = AgentApprovalRuntime.preferences
) {
    fun policySnapshot(): AgentApprovalPolicy = approvalPreferences.policy.value

    fun evaluate(
        action: AgentAction,
        observation: AgentObservation,
        localCapabilityReason: String? = null,
        policy: AgentApprovalPolicy = policySnapshot(),
        authorizedPackages: Set<String> = emptySet()
    ): AgentRiskAssessment {
        val targetNode = actionTargetNode(action, observation)
        val targetText = targetNode.targetText()
        val canCommitUiAction = action is AgentAction.Tap || action is AgentAction.TapElement
        if ((action is AgentAction.InputText && (
                targetNode?.password == true ||
                    action.meta.operationKind in BLOCKED_OPERATION_KINDS ||
                    (targetNode == null && observation.uiNodes.any { it.password })
                )) ||
            canCommitUiAction && (
                action.meta.operationKind in BLOCKED_OPERATION_KINDS || targetText.hasBlockedRiskTerm()
            )
        ) {
            return AgentRiskAssessment(
                AgentRiskLevel.BLOCKED,
                "Payments, account changes, and password entry are outside the enabled Agent scope"
            )
        }
        if (authorizedPackages.isNotEmpty()) {
            val targetPkg = when (action) {
                is AgentAction.LaunchPackage -> action.packageName
                is AgentAction.ForceStopPackage -> action.packageName
                is AgentAction.ClearAppData -> action.packageName
                is AgentAction.UninstallPackage -> action.packageName
                else -> null
            }
            if (targetPkg != null && targetPkg !in authorizedPackages) {
                return AgentRiskAssessment(
                    AgentRiskLevel.CONFIRMATION_REQUIRED,
                    "Confirm action on package $targetPkg outside authorized scope"
                )
            }
            if (canCommitUiAction || action is AgentAction.InputText || action is AgentAction.Swipe ||
                (action is AgentAction.KeyEvent && action.key == AgentKey.ENTER)
            ) {
                val nodePkg = targetNode?.packageName?.takeIf(String::isNotBlank)
                val foregroundPkg = observation.currentActivity?.substringBefore('/')?.takeIf { '/' in observation.currentActivity && it.isNotBlank() }
                val activePkg = nodePkg ?: foregroundPkg
                if (activePkg != null && activePkg !in authorizedPackages && activePkg !in SYSTEM_EXEMPT_PACKAGES) {
                    return AgentRiskAssessment(
                        AgentRiskLevel.CONFIRMATION_REQUIRED,
                        "Current app $activePkg is outside authorized scope; confirm action"
                    )
                }
            }
        }
        if (!localCapabilityReason.isNullOrBlank()) {
            return AgentRiskAssessment(AgentRiskLevel.CONFIRMATION_REQUIRED, localCapabilityReason)
        }
        if (canCommitUiAction &&
            (action.meta.operationKind == AgentOperationKind.SEND || targetText.isSendControl()) ||
            action is AgentAction.KeyEvent && action.key == AgentKey.ENTER
        ) {
            return AgentRiskAssessment(
                AgentRiskLevel.CONFIRMATION_REQUIRED,
                "Confirm the current send or submit action on the device"
            )
        }
        if (action.requiresConfirmation) {
            return AgentRiskAssessment(
                AgentRiskLevel.CONFIRMATION_REQUIRED,
                "This device-management action can change device or application data"
            )
        }

        if (policy == AgentApprovalPolicy.CAUTIOUS &&
            (action is AgentAction.InputText || action is AgentAction.ForceStopPackage || action is AgentAction.RebootDevice)
        ) {
            return AgentRiskAssessment(
                AgentRiskLevel.CONFIRMATION_REQUIRED,
                "Cautious policy requires confirmation before changing device or application state"
            )
        }

        val modelRisk = canCommitUiAction && action.meta.operationKind == AgentOperationKind.DELETE
        val explicitTargetRisk = canCommitUiAction && targetText.hasDestructiveRiskTerm()
        val cautiousTextRisk = canCommitUiAction && targetText.hasCautiousOnlyRiskTerm()
        val requiresConfirmation = modelRisk || explicitTargetRisk ||
            (policy == AgentApprovalPolicy.CAUTIOUS && cautiousTextRisk)
        return if (requiresConfirmation) {
            val target = action.meta.target.ifBlank { targetText.take(80).ifBlank { action.toolName } }
            AgentRiskAssessment(
                AgentRiskLevel.CONFIRMATION_REQUIRED,
                "Confirm ${action.meta.operationKind.name.lowercase().replace('_', ' ')} action on $target"
            )
        } else {
            AgentRiskAssessment(AgentRiskLevel.SAFE)
        }
    }

    private fun actionTargetNode(action: AgentAction, observation: AgentObservation): UiNodeSnapshot? =
        when (action) {
            is AgentAction.TapElement -> observation.uiNodes.firstOrNull { it.elementId == action.elementId }
            is AgentAction.InputText -> action.elementId?.let { id ->
                observation.uiNodes.firstOrNull { it.elementId == id }
            }
            is AgentAction.Tap -> observation.uiNodes.minByOrNull {
                val dx = it.bounds.centerX - action.x
                val dy = it.bounds.centerY - action.y
                dx * dx + dy * dy
            }?.takeIf {
                kotlin.math.abs(it.bounds.centerX - action.x) <= 80 &&
                    kotlin.math.abs(it.bounds.centerY - action.y) <= 80
            }
            else -> null
        }

}

private val BLOCKED_OPERATION_KINDS = setOf(AgentOperationKind.PURCHASE, AgentOperationKind.ACCOUNT)

private fun UiNodeSnapshot?.targetText(): String = this?.let {
    "${it.text} ${it.contentDescription}".trim()
}.orEmpty()

private fun String.hasBlockedRiskTerm(): Boolean =
    BLOCKED_ENGLISH_RISK.containsMatchIn(lowercase()) || BLOCKED_CHINESE_RISK.any(::contains)

private fun String.isSendControl(): Boolean {
    val normalized = trim().lowercase()
    return normalized in setOf("send", "submit", "发送", "提交") ||
        normalized.startsWith("send ") || normalized.startsWith("发送 ")
}

/** Only irreversible data and application removal controls require approval. */
private fun String.hasDestructiveRiskTerm(): Boolean {
    val english = lowercase()
    return DESTRUCTIVE_ENGLISH_RISK.containsMatchIn(english) || DESTRUCTIVE_CHINESE_RISK.any(::contains)
}

/** Legacy broad matches are intentionally opt-in because they flag harmless controls such as search clearing. */
private fun String.hasCautiousOnlyRiskTerm(): Boolean =
    CAUTIOUS_ENGLISH_RISK.containsMatchIn(lowercase()) || CAUTIOUS_CHINESE_RISK.any(::contains)

private val DESTRUCTIVE_ENGLISH_RISK = Regex("\\b(delete|clear data|erase data|uninstall|factory reset)\\b")
private val DESTRUCTIVE_CHINESE_RISK = listOf("删除", "清除数据", "清空数据", "卸载", "恢复出厂")
private val CAUTIOUS_ENGLISH_RISK = Regex("\\b(remove|clear)\\b")
private val CAUTIOUS_CHINESE_RISK = listOf("移除", "清除")
private val BLOCKED_ENGLISH_RISK = Regex("\\b(pay|purchase|buy|checkout|sign in|log in|register|create account|password|passcode)\\b")
private val BLOCKED_CHINESE_RISK = listOf("付款", "支付", "购买", "下单", "结账", "登录", "注册账号", "创建账号", "密码", "口令")

internal val SYSTEM_EXEMPT_PACKAGES = setOf(
    "com.android.systemui",
    "com.android.permissioncontroller",
    "com.google.android.permissioncontroller",
    "com.google.android.inputmethod.latin",
    "com.android.inputmethod.latin",
    "com.ludoven.adbtool.ime"
)
