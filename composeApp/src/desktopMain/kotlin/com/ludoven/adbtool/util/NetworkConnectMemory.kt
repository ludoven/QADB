package com.ludoven.adbtool.util

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 记录最近一次网络连接输入的 IP 与端口，应用重启后自动回填。
 */
object NetworkConnectMemory {

    data class SavedConnection(
        val ip: String = "",
        val port: String = "5555"
    )

    private val file = File(System.getProperty("user.home"), ".qadb_network_connect.json")
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cached: SavedConnection? = null

    fun load(): SavedConnection = synchronized(this) {
        cached ?: runCatching {
            if (!file.exists()) SavedConnection()
            else {
                val o = json.parseToJsonElement(file.readText()).jsonObject
                SavedConnection(
                    ip = o["ip"]?.jsonPrimitive?.content.orEmpty(),
                    port = o["port"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() } ?: "5555"
                )
            }
        }.getOrElse { SavedConnection() }.also { cached = it }
    }

    fun save(ip: String, port: String) {
        val normalizedIp = ip.trim()
        if (normalizedIp.isBlank()) return
        synchronized(this) {
            val saved = SavedConnection(normalizedIp, port.trim())
            cached = saved
            runCatching {
                file.parentFile?.mkdirs()
                file.writeText(
                    buildJsonObject {
                        put("ip", JsonPrimitive(saved.ip))
                        put("port", JsonPrimitive(saved.port))
                    }.toString()
                )
            }
        }
    }
}
