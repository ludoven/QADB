package com.ludoven.adbtool.agent

import com.ludoven.adbtool.agent.artemis.ArtemisProfile
import com.ludoven.adbtool.agent.artemis.ManagedArtemisRuntime
import java.util.prefs.Preferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AgentEngineKind { SCREENSHOT, ARTEMIS }

data class ArtemisEngineConfiguration(
    val enabled: Boolean = false,
    val baseUrl: String = DEFAULT_ARTEMIS_BASE_URL,
    val profile: ArtemisProfile = ArtemisProfile.FLASH
) {
    companion object {
        const val DEFAULT_ARTEMIS_BASE_URL = "http://127.0.0.1:18761"
    }
}

/** Local-only experimental engine setting.  No provider credentials are stored here. */
class ArtemisEnginePreferences(
    private val preferences: Preferences = Preferences.userNodeForPackage(ArtemisEnginePreferences::class.java)
) {
    private val _configuration = MutableStateFlow(read())
    val configuration: StateFlow<ArtemisEngineConfiguration> = _configuration.asStateFlow()

    fun setConfiguration(configuration: ArtemisEngineConfiguration) {
        preferences.putBoolean(KEY_ENABLED, configuration.enabled)
        preferences.put(KEY_BASE_URL, configuration.baseUrl)
        preferences.put(KEY_PROFILE, configuration.profile.name)
        preferences.flush()
        _configuration.value = configuration
    }

    private fun read(): ArtemisEngineConfiguration = ArtemisEngineConfiguration(
        enabled = preferences.getBoolean(KEY_ENABLED, false),
        baseUrl = preferences.get(KEY_BASE_URL, ArtemisEngineConfiguration.DEFAULT_ARTEMIS_BASE_URL),
        profile = runCatching {
            ArtemisProfile.valueOf(preferences.get(KEY_PROFILE, ArtemisProfile.FLASH.name))
        }.getOrDefault(ArtemisProfile.FLASH)
    )

    private companion object {
        const val KEY_ENABLED = "agent.artemis.enabled"
        const val KEY_BASE_URL = "agent.artemis.base_url"
        const val KEY_PROFILE = "agent.artemis.profile"
    }
}

object ArtemisEngineRuntime {
    val preferences: ArtemisEnginePreferences by lazy { ArtemisEnginePreferences() }
    val managedRuntime: ManagedArtemisRuntime by lazy { ManagedArtemisRuntime() }
}
