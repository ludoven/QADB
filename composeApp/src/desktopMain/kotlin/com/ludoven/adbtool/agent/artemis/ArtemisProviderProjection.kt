package com.ludoven.adbtool.agent.artemis

enum class ProviderType {
    OPENAI_COMPATIBLE,
    GEMINI,
    ANTHROPIC,
    OPEN_ROUTER
}

data class UnifiedModelProfile(
    val provider: ProviderType = ProviderType.OPENAI_COMPATIBLE,
    val baseUrl: String? = null,
    val apiKey: String? = null,
    val model: String = "",
    val vision: Boolean = true,
    val toolCalling: Boolean = true
)

object ArtemisProviderProjection {
    fun project(profile: UnifiedModelProfile): Map<String, String> {
        val env = mutableMapOf<String, String>()
        val key = profile.apiKey?.trim().orEmpty()
        if (key.isBlank()) return env

        val base = profile.baseUrl?.trim().orEmpty()
        when (profile.provider) {
            ProviderType.GEMINI -> {
                env["GEMINI_API_KEY"] = key
                env["GOOGLE_API_KEY"] = key
            }
            ProviderType.ANTHROPIC -> {
                env["ANTHROPIC_API_KEY"] = key
                if (base.isNotBlank()) env["ANTHROPIC_BASE_URL"] = base
            }
            ProviderType.OPEN_ROUTER -> {
                env["OPEN_ROUTER_API_KEY"] = key
                env["OPENAI_API_KEY"] = key
                if (base.isNotBlank()) env["OPENAI_BASE_URL"] = base
            }
            ProviderType.OPENAI_COMPATIBLE -> {
                env["OPENAI_API_KEY"] = key
                if (base.isNotBlank()) env["OPENAI_BASE_URL"] = base
            }
        }
        if (profile.model.isNotBlank()) {
            env["ARTEMIS_MODEL"] = profile.model
        }
        return env
    }

    fun inferProviderType(baseUrl: String?, modelName: String): ProviderType {
        val lowerUrl = baseUrl?.lowercase().orEmpty()
        val lowerModel = modelName.lowercase()
        return when {
            "openrouter.ai" in lowerUrl -> ProviderType.OPEN_ROUTER
            "googleapis.com" in lowerUrl || "gemini" in lowerUrl -> ProviderType.GEMINI
            "anthropic.com" in lowerUrl -> ProviderType.ANTHROPIC
            "gemini" in lowerModel -> ProviderType.GEMINI
            "claude" in lowerModel -> ProviderType.ANTHROPIC
            else -> ProviderType.OPENAI_COMPATIBLE
        }
    }
}
