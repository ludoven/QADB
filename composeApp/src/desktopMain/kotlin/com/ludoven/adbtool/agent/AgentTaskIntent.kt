package com.ludoven.adbtool.agent

enum class AgentTaskIntentKind {
    CONVERSATION,
    DEVICE_STATUS,
    SCREEN_READ,
    APP_CONTENT_READ,
    APP_CATALOG_READ,
    DEVICE_OPERATION,
    CLARIFICATION
}

enum class AgentTaskAccessLevel {
    NONE,
    STATUS_ONLY,
    UI_READ_ONLY,
    NAVIGATION_READ_ONLY,
    CATALOG_READ_ONLY,
    MUTATING
}

enum class AgentTaskAuthorityOutcome { READ_ONLY, DEVICE_CHANGE }

enum class AgentActionEffect {
    OBSERVE,
    LAUNCH_APP,
    NAVIGATION,
    SCROLL,
    WAIT,
    DATA_ENTRY,
    SEND,
    COMMIT,
    SYSTEM_CHANGE,
    DESTRUCTIVE
}

data class AgentTaskAuthority(
    val outcome: AgentTaskAuthorityOutcome,
    val allowedEffects: Set<AgentActionEffect>
) {
    fun allows(effect: AgentActionEffect): Boolean = effect in allowedEffects

    companion object {
        val NONE = AgentTaskAuthority(AgentTaskAuthorityOutcome.READ_ONLY, emptySet())
        val OBSERVE_ONLY = AgentTaskAuthority(
            AgentTaskAuthorityOutcome.READ_ONLY,
            setOf(AgentActionEffect.OBSERVE)
        )
        val NAVIGATION_READ_ONLY = AgentTaskAuthority(
            AgentTaskAuthorityOutcome.READ_ONLY,
            setOf(
                AgentActionEffect.OBSERVE,
                AgentActionEffect.LAUNCH_APP,
                AgentActionEffect.NAVIGATION,
                AgentActionEffect.SCROLL,
                AgentActionEffect.WAIT
            )
        )
        val DEVICE_OPERATION = AgentTaskAuthority(
            AgentTaskAuthorityOutcome.DEVICE_CHANGE,
            AgentActionEffect.entries.toSet()
        )

        fun forAccessLevel(level: AgentTaskAccessLevel): AgentTaskAuthority = when (level) {
            AgentTaskAccessLevel.NONE -> NONE
            AgentTaskAccessLevel.STATUS_ONLY,
            AgentTaskAccessLevel.UI_READ_ONLY,
            AgentTaskAccessLevel.CATALOG_READ_ONLY -> OBSERVE_ONLY
            AgentTaskAccessLevel.NAVIGATION_READ_ONLY -> NAVIGATION_READ_ONLY
            AgentTaskAccessLevel.MUTATING -> DEVICE_OPERATION
        }
    }
}

enum class DeviceStatusField {
    IDENTITY,
    ANDROID,
    DISPLAY,
    BATTERY,
    CPU,
    MEMORY,
    STORAGE,
    NETWORK,
    FOREGROUND_APP,
    ADB_LATENCY
}

data class AgentTaskIntent(
    val kind: AgentTaskIntentKind,
    val accessLevel: AgentTaskAccessLevel,
    val requestedStatusFields: Set<DeviceStatusField> = emptySet(),
    val systemProbeId: String? = null,
    val requiresAiAnalysis: Boolean = false,
    val hasExplicitMutation: Boolean = false,
    val hasExplicitDeviceAction: Boolean = hasExplicitMutation,
    val clarificationQuestion: String? = null,
    val reason: String,
    val authority: AgentTaskAuthority = AgentTaskAuthority.forAccessLevel(accessLevel)
) {
    init {
        require((accessLevel == AgentTaskAccessLevel.MUTATING) == hasExplicitMutation) {
            "Mutating access must be backed by explicit operation language"
        }
        require(
            accessLevel != AgentTaskAccessLevel.NAVIGATION_READ_ONLY ||
                authority == AgentTaskAuthority.NAVIGATION_READ_ONLY
        ) { "Navigation read-only access requires navigation-only authority" }
        require(kind == AgentTaskIntentKind.CLARIFICATION || clarificationQuestion == null) {
            "Only clarification intents may contain a clarification question"
        }
    }
}

/**
 * Local safety boundary used before any model or device access.
 *
 * Rules intentionally prefer a read-only interpretation and never infer a device mutation from
 * an unknown sentence. A later model classifier may narrow an intent, but must not upgrade an
 * intent to [AgentTaskAccessLevel.MUTATING] unless this router already found explicit operation
 * language.
 */
class AgentTaskIntentRouter {
    fun route(task: String): AgentTaskIntent {
        val normalized = normalize(task)
        if (normalized.isBlank()) return clarification("请告诉我希望检查设备信息，还是执行设备操作。", "empty_task")

        if (GREETING.matches(normalized)) {
            return AgentTaskIntent(
                kind = AgentTaskIntentKind.CONVERSATION,
                accessLevel = AgentTaskAccessLevel.NONE,
                reason = "local_greeting"
            )
        }

        val explicitOperation = hasAffirmativeDeviceOperation(normalized)
        val explicitMutation = hasAffirmativeMutation(normalized)
        if (APP_CONTENT_READ.containsMatchIn(normalized) && !explicitMutation) {
            return AgentTaskIntent(
                kind = AgentTaskIntentKind.APP_CONTENT_READ,
                accessLevel = AgentTaskAccessLevel.NAVIGATION_READ_ONLY,
                hasExplicitDeviceAction = explicitOperation || hasAffirmativeReadNavigation(normalized),
                requiresAiAnalysis = true,
                reason = "local_app_content_read",
                authority = AgentTaskAuthority.NAVIGATION_READ_ONLY
            )
        }
        if (!explicitOperation && APP_CATALOG_READ.containsMatchIn(normalized)) {
            return AgentTaskIntent(
                kind = AgentTaskIntentKind.APP_CATALOG_READ,
                accessLevel = AgentTaskAccessLevel.CATALOG_READ_ONLY,
                reason = "local_app_catalog_read"
            )
        }

        val screenRead = SCREEN_READ.containsMatchIn(normalized) ||
            FOREGROUND_READ.containsMatchIn(normalized)
        if (!explicitOperation) {
            systemProbeId(normalized)?.let { probeId ->
                return AgentTaskIntent(
                    kind = AgentTaskIntentKind.DEVICE_STATUS,
                    accessLevel = AgentTaskAccessLevel.STATUS_ONLY,
                    systemProbeId = probeId,
                    reason = "local_system_probe"
                )
            }
        }
        if (screenRead && !explicitOperation) {
            return AgentTaskIntent(
                kind = AgentTaskIntentKind.SCREEN_READ,
                accessLevel = AgentTaskAccessLevel.UI_READ_ONLY,
                requestedStatusFields = if (FOREGROUND_READ.containsMatchIn(normalized)) {
                    setOf(DeviceStatusField.FOREGROUND_APP)
                } else {
                    emptySet()
                },
                requiresAiAnalysis = ANALYSIS_MARKERS.containsMatchIn(normalized),
                reason = "local_screen_read"
            )
        }

        if (explicitOperation) {
            return AgentTaskIntent(
                kind = AgentTaskIntentKind.DEVICE_OPERATION,
                accessLevel = AgentTaskAccessLevel.MUTATING,
                hasExplicitMutation = true,
                hasExplicitDeviceAction = true,
                reason = "explicit_device_operation"
            )
        }

        requestedStatusFields(normalized).takeIf { it.isNotEmpty() }?.let { fields ->
            return AgentTaskIntent(
                kind = AgentTaskIntentKind.DEVICE_STATUS,
                accessLevel = AgentTaskAccessLevel.STATUS_ONLY,
                requestedStatusFields = fields,
                requiresAiAnalysis = ANALYSIS_MARKERS.containsMatchIn(normalized),
                reason = "local_device_status"
            )
        }

        return clarification(
            question = "我还不能确定你是想读取设备信息，还是操作设备。请补充要查看或执行的具体内容。",
            reason = "safe_default"
        )
    }

    private fun requestedStatusFields(task: String): Set<DeviceStatusField> {
        val fields = linkedSetOf<DeviceStatusField>()
        STATUS_PATTERNS.forEach { (field, pattern) ->
            if (pattern.containsMatchIn(task)) fields += field
        }
        if (fields.isEmpty() && DEVICE_STATUS_OVERVIEW.containsMatchIn(task)) fields += DEFAULT_STATUS_FIELDS
        return fields
    }

    private fun systemProbeId(task: String): String? = when {
        AIRPLANE_STATUS.containsMatchIn(task) -> "airplane_mode"
        ROTATION_STATUS.containsMatchIn(task) -> "rotation_locked"
        WIFI_STATUS.containsMatchIn(task) -> "wifi"
        else -> null
    }

    private fun hasAffirmativeDeviceOperation(task: String): Boolean {
        return explicitOperationClauseCount(task) > 0
    }

    private fun hasAffirmativeMutation(task: String): Boolean {
        if (GLOBAL_DEVICE_OPERATION_DENIAL.containsMatchIn(task)) return false
        return task.split(OPERATION_CLAUSE_BOUNDARY).any { rawClause ->
            val clause = rawClause.trim().trim('“', '”', '"', '\'', '「', '」')
            clause.isNotBlank() &&
                !NON_MUTATING_CLAUSE.containsMatchIn(clause) &&
                !NEGATION_BEFORE_OPERATION.containsMatchIn(clause) &&
                MUTATING_DEVICE_OPERATION_VERB.findAll(clause).any { operation ->
                    val prefix = clause.substring(0, operation.range.first).trim()
                    prefix.isBlank() || IMPERATIVE_PREFIX.containsMatchIn(prefix)
                }
        }
    }

    private fun hasAffirmativeReadNavigation(task: String): Boolean =
        task.split(OPERATION_CLAUSE_BOUNDARY).any { rawClause ->
            val clause = rawClause.trim().trim('“', '”', '"', '\'', '「', '」')
            val navigation = READ_NAVIGATION_VERB.find(clause) ?: return@any false
            clause.isNotBlank() &&
                !NEGATION_BEFORE_OPERATION.containsMatchIn(clause.substring(0, navigation.range.first))
        }

    internal fun explicitOperationClauseCount(task: String): Int {
        val normalized = normalize(task)
        if (GLOBAL_DEVICE_OPERATION_DENIAL.containsMatchIn(normalized)) return 0
        return normalized.split(OPERATION_CLAUSE_BOUNDARY).count(::isAffirmativeOperationClause)
    }

    private fun isAffirmativeOperationClause(rawClause: String): Boolean {
        val clause = rawClause.trim().trim('“', '”', '"', '\'', '「', '」')
        if (
            clause.isBlank() ||
            NON_MUTATING_CLAUSE.containsMatchIn(clause) ||
            NEGATION_BEFORE_OPERATION.containsMatchIn(clause)
        ) return false
        return DEVICE_OPERATION_VERB.findAll(clause).any { operation ->
            val prefix = clause.substring(0, operation.range.first).trim()
            prefix.isBlank() || IMPERATIVE_PREFIX.containsMatchIn(prefix)
        }
    }

    private fun clarification(question: String, reason: String) = AgentTaskIntent(
        kind = AgentTaskIntentKind.CLARIFICATION,
        accessLevel = AgentTaskAccessLevel.NONE,
        clarificationQuestion = question,
        reason = reason
    )

    private fun normalize(task: String): String = task
        .trim()
        .lowercase()
        .replace(WHITESPACE, " ")
        .trimEnd('。', '！', '!', '？', '?', '～', '~')
}

internal data class AgentTaskExecutionGate(
    val intent: AgentTaskIntent,
    val requiresDevice: Boolean,
    val requiresProvider: Boolean
) {
    fun canStart(hasDevice: Boolean, hasProvider: Boolean): Boolean =
        (!requiresDevice || hasDevice) && (!requiresProvider || hasProvider)
}

/**
 * Resolves task prerequisites before either the UI or ViewModel applies availability checks.
 * Device-dependent intents are known locally, so they must be rejected before any model call when
 * no selected device is available. Conversation and clarification remain device independent.
 */
internal fun AgentTaskIntentRouter.executionGate(
    task: String
): AgentTaskExecutionGate {
    val intent = route(task)
    return AgentTaskExecutionGate(
        intent = intent,
        requiresDevice = intent.accessLevel != AgentTaskAccessLevel.NONE,
        requiresProvider = true
    )
}

private val WHITESPACE = Regex("\\s+")
private val GREETING = Regex(
    "^(?:(你好|您好|嗨|哈喽|hello|hi|hey|早上好|下午好|晚上好)(呀|啊|哦|啦)?(?:[，, ]*qadb)?|" +
        "(?:跟我|和我|向我)?(?:打个?|说声)招呼|(?:跟我|和我|向我)问好)$"
)
private val ANALYSIS_MARKERS = Regex("分析|诊断|健康|是否正常|有没有问题|建议|原因")
private val DEVICE_STATUS_OVERVIEW = Regex(
    "(手机|设备).{0,5}(状态|情况|怎么样|如何|信息|概况)|" +
        "(状态|情况|信息).{0,5}(手机|设备)|device status|phone status"
        + "|(分析|诊断).{0,4}(当前)?(手机|设备)"
)
private val APP_CATALOG_READ = Regex(
    "(安装|已有|设备上|设备里|手机里).{0,10}(哪些|什么|所有|列表|有没有).{0,8}(应用|app|设置|浏览器|chrome)|" +
        "(查看|读取|列出|显示).{0,8}(已安装|安装的).{0,4}(应用|app)|" +
        "(哪些|什么|所有).{0,4}(已安装|安装的).{0,4}(应用|app)|" +
        "(应用|app).{0,4}(列表|清单|目录)|" +
        "(查找|找一下|找出).{0,8}(应用|app|设置|浏览器|chrome)|" +
        "(设置|浏览器|chrome).{0,6}(是否已安装|有没有)|" +
        "(系统设置).{0,4}(是否已安装)|" +
        ".{1,24}(是否已?安装|有没有安装|安装了吗|安装了没)"
)
private val FOREGROUND_READ = Regex(
    "当前.{0,10}(应用|app|包名)|前台.{0,8}(应用|app|包名)|" +
        "现在(?:打开的|的)?.{0,8}(应用|app|包名)"
)
private val SCREEN_READ = Regex(
    "屏幕.{0,6}(什么|内容|显示|状态|页面|界面)|" +
        "(观察|查看|读取|读一下|看一下|识别|概括).{0,6}(当前)?(屏幕|页面|界面)|" +
        "(当前|现在).{0,4}(页面|页|界面).{0,6}(什么|内容|显示|标题)|" +
        "(读取|提取).{0,6}(当前)?(页面|页|屏幕).{0,6}(标题|内容)|" +
        "截图.{0,5}(识别|分析|内容|看看)|" +
        "当前.{0,10}(应用|app|包名)|前台.{0,8}(应用|app|包名)|现在打开的.{0,8}(应用|app)"
)
private val APP_CONTENT_READ = Regex(
    "(?:我(?:的)?|手机|设备|当前|现在).{0,16}(?:朋友圈|动态|消息|通知|订单|列表|内容|帖子|视频).{0,16}" +
        "(?:第一|最新|最后|首条|内容|标题|是什么|有谁|多少|哪(?:一)?条)|" +
        "(?:打开|进入|查看|看看|看一下|读取|读一下).{0,20}(?:朋友圈|动态|消息|通知|订单|列表|帖子|视频).{0,20}" +
        "(?:第一|最新|最后|首条|内容|标题|是什么|有谁|多少|哪(?:一)?条)|" +
        "(?:第一|最新|最后|首条|未读).{0,8}(?:朋友圈|动态|消息|通知|订单|内容|帖子|视频)"
)
private val DEVICE_OPERATION_VERB = Regex(
    "打开|启动|进入|点击|点开|点一下|点|按下|按一下|返回|回到|后退|滑动|滚动|滚回|上滑|下滑|左滑|右滑|" +
        "输入|填写|发送|发出|发(?:一条|条|个|封|句)?(?:微信|消息|短信|邮件)|" +
        "(?:回复|回)(?:这|该)?(?:一条|条|个|封|句)?(?:微信|消息|短信|邮件)|" +
        "(?:微信|消息|短信|邮件).{0,12}(?:发送|发给|回复)|" +
        "(?<!已)安装(?!了|过|着|吗|呢|的|哪些|什么|列表)|卸载|移除|删除|清除|清空|清理|重置|" +
        "清掉|重启|关闭|开启|启用|停用|切换|改变|调整|授权|允许|同意|拍照|截屏|搜索框(?:中)?找|" +
        "(?:找到|找出).{0,12}(?:入口|按钮|菜单|选项|设置|页面|控件|设备信息)|操作|继续|重试|搜索"
)
private val MUTATING_DEVICE_OPERATION_VERB = Regex(
    "输入|填写|发送|发出|回复|安装|卸载|移除|删除|清除|清空|清理|重置|清掉|重启|" +
        "关闭|开启|启用|停用|切换|改变|调整|授权|允许|同意|拍照|点赞|评论|关注|分享|保存|购买|支付|下单"
)
private val READ_NAVIGATION_VERB = Regex(
    "打开|启动|进入|点开|返回|后退|滑动|滚动|滚回|上滑|下滑|左滑|右滑"
)
private val OPERATION_CLAUSE_BOUNDARY = Regex(
    "\\s*(?:[，,。；;！!？?]|然后|接着|随后|而是|但是|但|并且|并|再)\\s*"
)
private val GLOBAL_DEVICE_OPERATION_DENIAL = Regex(
    "(?:不要|别|无需|不用|禁止|避免|不许).*(?:设备|手机).*(?:操作|执行|变更|改动)"
)
private val NEGATION_BEFORE_OPERATION = Regex(
    "不要|别|无需|不用|禁止|避免|不许|不能|不需要|不希望|不想|不允许|请勿|不得|莫"
)
private val IMPERATIVE_PREFIX = Regex(
    "^(?:帮我|请|给我|替我|麻烦|请帮我|我想|我要|需要你|去|把|将|给|用|从|向|往|先|重新|" +
        "在(?:手机|设备|设置|页面|屏幕|界面).*|(?:手机|设备|设置|页面|屏幕|界面).*|" +
        ".*(?:后|时))"
)
private val NON_MUTATING_CLAUSE = Regex(
    "(?:会发生什么|会怎么样|会有什么|会不会|是否|有没有|为什么|怎么|如何|能否|能不能|可不可以|可以吗|" +
        "是什么|是什么意思|什么意思|做什么|有什么区别|有何区别|的区别|怎么理解|含义|定义|作用|后果|影响|" +
        "介绍|说明|教程|概念|术语|文档|示例|会.{0,12}(?:什么|怎样|怎么样|后果|影响))|" +
        "^(?:手机|设备|系统|应用|app|页面|按钮|功能).{0,12}(?:会|将|可能|可以|能够|自动)|(?:吗|呢|么)$"
)

private val AIRPLANE_STATUS = Regex("飞行模式.{0,6}(状态|开着吗|开启吗)|读取飞行模式|查看飞行模式")
private val WIFI_STATUS = Regex(
    "(wi-?fi|无线网络).{0,8}(状态|开着吗|开启吗|关着吗|关闭了吗|是否关闭|是否开启)|" +
        "(wi-?fi|无线网络).{0,8}(?:是)?(?:开启|打开|关闭).{0,6}(?:还是|或).{0,6}(?:开启|打开|关闭)|" +
        "读取.{0,3}(wi-?fi|无线网络)|查看.{0,3}(wi-?fi|无线网络)"
)
private val ROTATION_STATUS = Regex("(自动旋转|自动转屏|屏幕旋转).{0,6}(状态|开着吗|开启吗)|读取.{0,3}(自动旋转|屏幕旋转)|查看.{0,3}(自动旋转|屏幕旋转)")

private val STATUS_PATTERNS = linkedMapOf(
    DeviceStatusField.IDENTITY to Regex("型号|厂商|制造商|设备名称|model|manufacturer"),
    DeviceStatusField.ANDROID to Regex("android|安卓|系统版本|版本号|构建号|build number|api|内核|kernel|rom|系统信息"),
    DeviceStatusField.DISPLAY to Regex("分辨率|屏幕尺寸|设备尺寸|屏幕大小|dpi|显示信息|字体缩放"),
    DeviceStatusField.BATTERY to Regex("电量|电池|充电|battery"),
    DeviceStatusField.CPU to Regex("cpu|处理器使用|性能占用"),
    DeviceStatusField.MEMORY to Regex("内存|ram|memory"),
    DeviceStatusField.STORAGE to Regex("存储|磁盘|剩余空间|storage"),
    DeviceStatusField.NETWORK to Regex("网络|wi-?fi|无线|ip地址|连接方式|network"),
    DeviceStatusField.FOREGROUND_APP to FOREGROUND_READ,
    DeviceStatusField.ADB_LATENCY to Regex("adb.{0,4}(延迟|速度)|连接延迟|latency")
)

private val DEFAULT_STATUS_FIELDS = setOf(
    DeviceStatusField.IDENTITY,
    DeviceStatusField.ANDROID,
    DeviceStatusField.DISPLAY,
    DeviceStatusField.BATTERY,
    DeviceStatusField.CPU,
    DeviceStatusField.MEMORY,
    DeviceStatusField.STORAGE,
    DeviceStatusField.NETWORK,
    DeviceStatusField.FOREGROUND_APP,
    DeviceStatusField.ADB_LATENCY
)
