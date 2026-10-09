package com.ludoven.adbtool.agent

enum class AgentSemanticIntent {
    SEND_MESSAGE,
    POST_CONTENT,
    SUBMIT_FORM,
    DELETE_CONTENT,
    PAYMENT,
    REGISTER_ACCOUNT,
    LOGIN,
    PASSWORD_ENTRY,
    ACCOUNT_CHANGE,
    GENERIC_NAVIGATION,
    GENERIC_READ;

    companion object {
        fun fromWire(value: String?): AgentSemanticIntent = when (value?.trim()?.uppercase()) {
            "SEND_MESSAGE", "SEND", "MESSAGE" -> SEND_MESSAGE
            "POST_CONTENT", "POST", "PUBLISH" -> POST_CONTENT
            "SUBMIT_FORM", "SUBMIT", "COMMIT" -> SUBMIT_FORM
            "DELETE_CONTENT", "DELETE", "REMOVE", "CLEAR" -> DELETE_CONTENT
            "PAYMENT", "PAY", "PURCHASE" -> PAYMENT
            "REGISTER_ACCOUNT", "REGISTER", "SIGNUP" -> REGISTER_ACCOUNT
            "LOGIN", "SIGNIN" -> LOGIN
            "PASSWORD_ENTRY", "PASSWORD" -> PASSWORD_ENTRY
            "ACCOUNT_CHANGE", "ACCOUNT" -> ACCOUNT_CHANGE
            "READ", "OBSERVE" -> GENERIC_READ
            else -> GENERIC_NAVIGATION
        }
    }
}

object SemanticRiskClassifier {
    private val BLOCKED_INTENTS = setOf(
        AgentSemanticIntent.PAYMENT,
        AgentSemanticIntent.PASSWORD_ENTRY,
        AgentSemanticIntent.ACCOUNT_CHANGE
    )

    private val CONFIRMATION_INTENTS = setOf(
        AgentSemanticIntent.SEND_MESSAGE,
        AgentSemanticIntent.POST_CONTENT,
        AgentSemanticIntent.SUBMIT_FORM,
        AgentSemanticIntent.DELETE_CONTENT,
        AgentSemanticIntent.REGISTER_ACCOUNT,
        AgentSemanticIntent.LOGIN
    )

    fun evaluate(
        actionKind: String,
        target: String?,
        intentStr: String? = null
    ): AgentRiskAssessment {
        val intent = intentStr?.let(AgentSemanticIntent::fromWire)
            ?: inferIntentFromTarget(target)

        if (intent in BLOCKED_INTENTS) {
            return AgentRiskAssessment(
                level = AgentRiskLevel.BLOCKED,
                reason = "支付、密码输入和账号变更处于受限安全范围外，禁止执行。"
            )
        }

        if (intent in CONFIRMATION_INTENTS) {
            val description = when (intent) {
                AgentSemanticIntent.SEND_MESSAGE -> "发送消息"
                AgentSemanticIntent.POST_CONTENT -> "发布内容/发帖"
                AgentSemanticIntent.SUBMIT_FORM -> "提交表单"
                AgentSemanticIntent.DELETE_CONTENT -> "删除内容或清除数据"
                AgentSemanticIntent.REGISTER_ACCOUNT -> "注册账号"
                AgentSemanticIntent.LOGIN -> "登录账号"
                else -> "敏感提交"
            }
            return AgentRiskAssessment(
                level = AgentRiskLevel.CONFIRMATION_REQUIRED,
                reason = "检测到操作意图为[$description]（目标: ${target ?: actionKind}），需要人工确认后方可执行。"
            )
        }

        return AgentRiskAssessment(AgentRiskLevel.SAFE)
    }

    private fun inferIntentFromTarget(target: String?): AgentSemanticIntent? {
        val text = target?.lowercase()?.trim().orEmpty()
        if (text.isBlank()) return null
        return when {
            text.contains("支付") || text.contains("付款") || text.contains("购买") || text.contains("pay") || text.contains("checkout") -> AgentSemanticIntent.PAYMENT
            text.contains("密码") || text.contains("password") || text.contains("passcode") -> AgentSemanticIntent.PASSWORD_ENTRY
            text.contains("发送") || text.contains("send") -> AgentSemanticIntent.SEND_MESSAGE
            text.contains("发布") || text.contains("发帖") || text.contains("post") || text.contains("publish") -> AgentSemanticIntent.POST_CONTENT
            text.contains("提交") || text.contains("submit") || text.contains("commit") -> AgentSemanticIntent.SUBMIT_FORM
            text.contains("删除") || text.contains("卸载") || text.contains("delete") || text.contains("uninstall") || text.contains("清除") || text.contains("清空") -> AgentSemanticIntent.DELETE_CONTENT
            text.contains("登录") || text.contains("login") || text.contains("sign in") -> AgentSemanticIntent.LOGIN
            text.contains("注册") || text.contains("register") || text.contains("sign up") -> AgentSemanticIntent.REGISTER_ACCOUNT
            else -> null
        }
    }
}
