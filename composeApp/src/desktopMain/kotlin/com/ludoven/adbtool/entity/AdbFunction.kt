package com.ludoven.adbtool.entity

import androidx.compose.ui.graphics.vector.ImageVector

/*data class AdbFunction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)*/

// 定义 AdbFunctionType，用于区分不同操作
enum class AdbFunctionType() {
    // 新增类型 (注意有些类型可能不需要 commandTemplate，因为它们的操作更复杂，直接在 ViewModel 中实现)
    INSTALL_APK, // 安装需要文件选择器
    INSTALL_AND_LAUNCH, // 安装后打开
    INPUT_TEXT, // 输入文本需要弹窗
    OPEN_SHELL, // 执行 shell 命令
    OPEN_FILE_MANAGER, // 打开文件管理
    DEVICE_MIRROR, // 设备镜像显示
    SCREENSHOT, // 截图需要文件选择器
    SCREEN_RECORD, // 录屏
    CAPTURE_LOGS, // 抓日志
    VIEW_CURRENT_ACTIVITY, // 查看当前 Activity，结果需要解析
    KEY_BACK, // 返回键
    KEY_HOME, // Home 键
    LAUNCH_APP_BY_PACKAGE, // 根据包名启动
    STOP_APP_BY_PACKAGE, // 根据包名停止
    CLEAR_CACHE_AND_RESTART, // 清缓存并重启

    // 系统类操作
    REBOOT_DEVICE,
    IS_ROOTED, // 判断是否 root
    WIFI_INFO,
    CPU_INFO,
    NETWORK_STATUS,
    BATTERY_STATUS,
    SCREEN_RESOLUTION,
    DEVELOPER_OPTIONS,

    //APP
    UNINSTALL,
    SUPER_UNINSTALL, // root 权限超级卸载
    DISABLE_APP, // 禁用应用 (需 root)
    ENABLE_APP, // 启用应用 (需 root)
    LAUNCH,
    FORCE_STOP,
    RESTART_APP, // 需要多步操作
    CLEAR_DATA,
    CLEAR_AND_RESTART, // 需要多步操作
    RESET_PERMISSIONS,
    RESET_PERMISSIONS_AND_RESTART, // 需要多步操作
    GRANT_ALL_PERMISSIONS, // 需要多步操作
    GET_PATH,
    EXPORT_APK, // 复杂操作
    GET_APP_SIZE,
    APP_INFO;

}

// 统一的 AdbFunction 数据类，包含类型和 UI 信息
data class AdbFunction(
    val title: String,
    val icon: ImageVector,
    val type: AdbFunctionType,
    val requiresPackage: Boolean = true // 标记是否需要选中应用
)
