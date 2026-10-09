package com.ludoven.adbtool.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgentTaskIntentTest {
    private val router = AgentTaskIntentRouter()

    @Test
    fun `device overview is status only and requests all core fields`() {
        val intent = router.route("我手机状态如何？")

        assertEquals(AgentTaskIntentKind.DEVICE_STATUS, intent.kind)
        assertEquals(AgentTaskAccessLevel.STATUS_ONLY, intent.accessLevel)
        assertFalse(intent.hasExplicitMutation)
        assertTrue(DeviceStatusField.BATTERY in intent.requestedStatusFields)
        assertTrue(DeviceStatusField.MEMORY in intent.requestedStatusFields)
        assertTrue(DeviceStatusField.STORAGE in intent.requestedStatusFields)
        assertTrue(DeviceStatusField.FOREGROUND_APP in intent.requestedStatusFields)
    }

    @Test
    fun `diagnostic status requests analysis without device mutation`() {
        val intent = router.route("帮我分析设备当前电池和内存是否正常")

        assertEquals(AgentTaskIntentKind.DEVICE_STATUS, intent.kind)
        assertTrue(intent.requiresAiAnalysis)
        assertEquals(
            setOf(DeviceStatusField.BATTERY, DeviceStatusField.MEMORY),
            intent.requestedStatusFields
        )
        assertFalse(intent.hasExplicitMutation)
    }

    @Test
    fun `greetings need no device access`() {
        val intent = router.route("你好呀！")

        assertEquals(AgentTaskIntentKind.CONVERSATION, intent.kind)
        assertEquals(AgentTaskAccessLevel.NONE, intent.accessLevel)
    }

    @Test
    fun `explicit operation clauses bound the maximum frozen plan size`() {
        assertEquals(1, router.explicitOperationClauseCount("打开微信"))
        assertEquals(2, router.explicitOperationClauseCount("先打开微信，然后打开天气"))
        assertEquals(3, router.explicitOperationClauseCount("打开微信，再打开天气，随后打开设置"))
        assertEquals(0, router.explicitOperationClauseCount("不要操作设备，只告诉我怎么打开微信"))
    }

    @Test
    fun `screen and application catalog reads stay read only`() {
        val screen = router.route("识别一下当前屏幕显示的内容")
        val foreground = router.route("当前打开的是什么应用")
        val apps = router.route("手机里安装了哪些应用？")

        assertEquals(AgentTaskIntentKind.SCREEN_READ, screen.kind)
        assertEquals(AgentTaskAccessLevel.UI_READ_ONLY, screen.accessLevel)
        assertEquals(AgentTaskIntentKind.SCREEN_READ, foreground.kind)
        assertEquals(setOf(DeviceStatusField.FOREGROUND_APP), foreground.requestedStatusFields)
        assertEquals(AgentTaskIntentKind.APP_CATALOG_READ, apps.kind)
        assertEquals(AgentTaskAccessLevel.CATALOG_READ_ONLY, apps.accessLevel)
        assertEquals(
            AgentTaskAccessLevel.CATALOG_READ_ONLY,
            router.route("列出已安装应用").accessLevel
        )
        assertEquals(
            AgentTaskAccessLevel.UI_READ_ONLY,
            router.route("找到当前应用包名").accessLevel
        )
        assertEquals(
            AgentTaskAccessLevel.MUTATING,
            router.route("找出 Chrome 并打开").accessLevel
        )
    }

    @Test
    fun `application content reads grant navigation without mutation authority`() {
        listOf(
            "我朋友圈第一条内容是什么",
            "打开微信看看朋友圈第一条内容是什么",
            "查看我朋友圈最新动态"
        ).forEach { task ->
            val intent = router.route(task)
            assertEquals(AgentTaskIntentKind.APP_CONTENT_READ, intent.kind, task)
            assertEquals(AgentTaskAccessLevel.NAVIGATION_READ_ONLY, intent.accessLevel, task)
            assertFalse(intent.hasExplicitMutation, task)
            assertEquals(AgentTaskAuthorityOutcome.READ_ONLY, intent.authority.outcome, task)
            assertTrue(intent.authority.allows(AgentActionEffect.LAUNCH_APP), task)
            assertTrue(intent.authority.allows(AgentActionEffect.NAVIGATION), task)
            assertTrue(intent.authority.allows(AgentActionEffect.SCROLL), task)
            assertFalse(intent.authority.allows(AgentActionEffect.DATA_ENTRY), task)
            assertFalse(intent.authority.allows(AgentActionEffect.COMMIT), task)
        }
        assertFalse(router.route("我朋友圈第一条内容是什么").hasExplicitDeviceAction)
        assertTrue(router.route("打开微信看看朋友圈第一条内容是什么").hasExplicitDeviceAction)
    }

    @Test
    fun `concept questions and write requests cannot inherit application read authority`() {
        val concept = router.route("朋友圈是什么")
        assertEquals(AgentTaskIntentKind.CLARIFICATION, concept.kind)
        assertEquals(AgentTaskAccessLevel.NONE, concept.accessLevel)

        listOf(
            "点赞朋友圈第一条",
            "评论朋友圈第一条真好看",
            "删除朋友圈第一条",
            "不要点赞朋友圈第一条，只告诉我怎么操作"
        ).forEach { task ->
            val intent = router.route(task)
            assertTrue(intent.kind != AgentTaskIntentKind.APP_CONTENT_READ, task)
            assertTrue(intent.accessLevel != AgentTaskAccessLevel.NAVIGATION_READ_ONLY, task)
        }
    }

    @Test
    fun `catalog aliases and system setting probes stay local and read only`() {
        listOf(
            "系统设置是否已安装",
            "设备里有没有设置",
            "查找浏览器",
            "找一下 Chrome"
        ).forEach { task ->
            val intent = router.route(task)
            assertEquals(AgentTaskIntentKind.APP_CATALOG_READ, intent.kind, task)
            assertEquals(AgentTaskAccessLevel.CATALOG_READ_ONLY, intent.accessLevel, task)
        }

        mapOf(
            "查看飞行模式状态" to "airplane_mode",
            "WiFi 开着吗" to "wifi",
            "读取屏幕旋转" to "rotation_locked"
        ).forEach { (task, probe) ->
            val intent = router.route(task)
            assertEquals(AgentTaskIntentKind.DEVICE_STATUS, intent.kind, task)
            assertEquals(AgentTaskAccessLevel.STATUS_ONLY, intent.accessLevel, task)
            assertEquals(probe, intent.systemProbeId, task)
        }
    }

    @Test
    fun `all version three read only benchmark expressions cannot gain mutating access`() {
        val readOnlySpecs = AgentBenchmarkRegistry.specs.filter { spec ->
            spec.category == AgentBenchmarkCategory.READ_ONLY ||
                spec.category == AgentBenchmarkCategory.EXTRACTION ||
                spec.id.endsWith("_read")
        }
        assertEquals(120, AgentBenchmarkRegistry.specs.sumOf { it.expressions.size })

        readOnlySpecs.forEach { spec ->
            val expectedKind = when (spec.id) {
                "read_greeting" -> AgentTaskIntentKind.CONVERSATION
                "read_foreground_app", "read_screen_text", "extract_visible_title" ->
                    AgentTaskIntentKind.SCREEN_READ
                "read_installed_settings" -> AgentTaskIntentKind.APP_CATALOG_READ
                else -> AgentTaskIntentKind.DEVICE_STATUS
            }
            spec.expressions.forEach { task ->
                val intent = router.route(task)
                assertEquals(expectedKind, intent.kind, task)
                assertFalse(intent.hasExplicitMutation, task)
                assertTrue(intent.accessLevel != AgentTaskAccessLevel.MUTATING, task)
            }
        }
    }

    @Test
    fun `all version three operation benchmark expressions require mutating access`() {
        val operationSpecs = AgentBenchmarkRegistry.specs.filter { spec ->
            spec.category !in setOf(
                AgentBenchmarkCategory.READ_ONLY,
                AgentBenchmarkCategory.EXTRACTION
            ) && !spec.id.endsWith("_read") && spec.id != "nav_find_browser"
        }

        operationSpecs.flatMap { it.expressions }.forEach { task ->
            val intent = router.route(task)
            assertEquals(AgentTaskIntentKind.DEVICE_OPERATION, intent.kind, task)
            assertEquals(AgentTaskAccessLevel.MUTATING, intent.accessLevel, task)
            assertTrue(intent.hasExplicitMutation, task)
        }

        AgentBenchmarkRegistry.specs.single { it.id == "nav_find_browser" }
            .expressions
            .forEach { task ->
                val intent = router.route(task)
                assertEquals(AgentTaskIntentKind.APP_CATALOG_READ, intent.kind, task)
                assertEquals(AgentTaskAccessLevel.CATALOG_READ_ONLY, intent.accessLevel, task)
                assertFalse(intent.hasExplicitMutation, task)
            }
    }

    @Test
    fun `explicit operations can receive mutating access even when they mention status fields`() {
        listOf(
            "打开系统设置",
            "把 Wi-Fi 打开",
            "安装微信",
            "点击允许按钮",
            "重启手机",
            "输入测试内容",
            "给奶娃发微信 123",
            "帮我给张三发一条消息",
            "用微信给测试联系人发送收到",
            "回复这条微信好的"
        ).forEach { task ->
            val intent = router.route(task)
            assertEquals(AgentTaskIntentKind.DEVICE_OPERATION, intent.kind, task)
            assertEquals(AgentTaskAccessLevel.MUTATING, intent.accessLevel, task)
            assertTrue(intent.hasExplicitMutation, task)
        }
    }

    @Test
    fun `negative and question action contexts never gain mutating access`() {
        val installationQuestion = router.route("不要打开微信，只告诉我是否安装")
        assertEquals(AgentTaskIntentKind.APP_CATALOG_READ, installationQuestion.kind)
        assertEquals(AgentTaskAccessLevel.CATALOG_READ_ONLY, installationQuestion.accessLevel)

        val wifiQuestion = router.route("Wi-Fi 关闭了吗？")
        assertEquals(AgentTaskIntentKind.DEVICE_STATUS, wifiQuestion.kind)
        assertEquals("wifi", wifiQuestion.systemProbeId)

        listOf(
            "如何关闭 Wi-Fi",
            "能不能打开微信",
            "如何给奶娃发微信",
            "能不能给奶娃发微信",
            "不要给奶娃发微信",
            "给奶娃发微信会发生什么",
            "别删除任何应用",
            "点击这个按钮会发生什么？",
            "手机会自动重启吗？",
            "不要执行任何设备操作，这只是供产品设计文档参考的一份概念介绍，请给我一份关于启动应用这一功能的说明"
        ).forEach { task ->
            assertFalse(router.route(task).hasExplicitMutation, task)
            assertTrue(router.route(task).accessLevel != AgentTaskAccessLevel.MUTATING, task)
        }
    }

    @Test
    fun `mixed read then explicit action remains a device operation`() {
        val intent = router.route("查看当前页面，然后点击允许")

        assertEquals(AgentTaskIntentKind.DEVICE_OPERATION, intent.kind)
        assertEquals(AgentTaskAccessLevel.MUTATING, intent.accessLevel)
        assertTrue(intent.hasExplicitMutation)
    }

    @Test
    fun `status alternatives and action explanations cannot gain mutating access`() {
        val wifi = router.route("Wi-Fi 是开启还是关闭？")
        assertEquals(AgentTaskIntentKind.DEVICE_STATUS, wifi.kind)
        assertEquals(AgentTaskAccessLevel.STATUS_ONLY, wifi.accessLevel)
        assertEquals("wifi", wifi.systemProbeId)

        listOf("删除和卸载有什么区别", "打开设置是什么意思").forEach { task ->
            val intent = router.route(task)
            assertFalse(intent.hasExplicitMutation, task)
            assertTrue(intent.accessLevel != AgentTaskAccessLevel.MUTATING, task)
        }
    }

    @Test
    fun `execution gate requires a selected device only for device dependent intents`() {
        listOf("你好", "你看着办").forEach { task ->
            val gate = router.executionGate(task)
            assertFalse(gate.requiresDevice, task)
            assertTrue(gate.requiresProvider, task)
            assertTrue(gate.canStart(hasDevice = false, hasProvider = true), task)
        }

        listOf(
            "我手机状态如何",
            "手机里安装了哪些应用",
            "当前打开的是什么应用",
            "识别一下当前屏幕显示的内容",
            "我朋友圈第一条内容是什么",
            "打开微信"
        ).forEach { task ->
            val gate = router.executionGate(task)
            assertTrue(gate.requiresDevice, task)
            assertTrue(gate.requiresProvider, task)
            assertFalse(gate.canStart(hasDevice = false, hasProvider = true), task)
            assertTrue(gate.canStart(hasDevice = true, hasProvider = true), task)
        }
    }

    @Test
    fun `settings prompt cannot bypass provider readiness`() {
        val gate = router.executionGate("返回桌面并打开系统设置")

        assertTrue(gate.requiresDevice)
        assertTrue(gate.requiresProvider)
        assertFalse(gate.canStart(hasDevice = false, hasProvider = false))
        assertFalse(gate.canStart(hasDevice = true, hasProvider = false))
        assertFalse(gate.canStart(hasDevice = false, hasProvider = true))
        assertTrue(gate.canStart(hasDevice = true, hasProvider = true))
    }

    @Test
    fun `unknown or empty tasks safely request clarification`() {
        listOf("", "帮我处理一下", "你看着办").forEach { task ->
            val intent = router.route(task)
            assertEquals(AgentTaskIntentKind.CLARIFICATION, intent.kind, task)
            assertEquals(AgentTaskAccessLevel.NONE, intent.accessLevel, task)
            assertFalse(intent.hasExplicitMutation, task)
            assertNotNull(intent.clarificationQuestion, task)
        }
    }
}
