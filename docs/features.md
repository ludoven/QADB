# 功能亮点

QADB 将高频 ADB 工作流拆成清晰模块，覆盖设备连接、应用管理、日志查看、按键模拟和命令执行。

<div class="module-grid">
  <div class="module-card">
    <h3>首页</h3>
    <p>展示设备状态、快捷入口和常用操作，进入应用后先确认当前设备是否可用。</p>
  </div>
  <div class="module-card">
    <h3>命令</h3>
    <p>把截图、录屏、重启、打开设置、查看 Activity 等命令整理成可点击动作。</p>
  </div>
  <div class="module-card">
    <h3>终端</h3>
    <p>内置命令行终端，可直接执行 adb 或 shell 命令。</p>
  </div>
  <div class="module-card">
    <h3>设备控制</h3>
    <p>模拟 Android 返回、主页、菜单、音量、方向键等 KeyEvent。</p>
  </div>
  <div class="module-card">
    <h3>应用</h3>
    <p>查看应用列表，支持安装、卸载、清除数据、强制停止和导出 APK。</p>
  </div>
  <div class="module-card">
    <h3>文件</h3>
    <p>浏览设备文件，支持上传、下载和常用文件管理操作。</p>
  </div>
  <div class="module-card">
    <h3>诊断</h3>
    <p>在统一工作区切换日志与进程，支持进程搜索、排序和资源占用查看。</p>
  </div>
  <div class="module-card">
    <h3>AI Agent</h3>
    <p>默认关闭；使用单一视觉 Agent 根据每一步最新截图执行一个受控动作，UI 结构只作可选提示。</p>
  </div>
  <div class="module-card">
    <h3>设置</h3>
    <p>配置 ADB、设备恢复、外观、实验功能、模型 Provider 与更新。</p>
  </div>
</div>

## 批量与桌面交互

- 应用页可从文件夹递归安装 APK（含子目录），固定执行设备，逐个显示完成进度和失败详情；目录选择期间也禁止重复启动。列表和网格支持双击打开应用信息。
- 文件页支持拖入本地文件或目录，整批使用拖入时的设备和目录；上传期间切换目录不会改变后续文件的目标。与 ADB push 一致，同名目标可能被覆盖。
- 日志支持拖动多选、右键复制和全选。复制使用选择时的内容快照，不随筛选或缓冲区淘汰而变成其他日志；切换设备会清空选择。再次点击当前快捷筛选可取消该条件。
- 命令库按 UTF-8 读取，避免系统默认编码导致中文乱码。
- 无线连接继续使用现有入口，保留地址校验、配对与连接历史。

### 验证批量功能

常规回归：`./gradlew :composeApp:desktopTest -Pqadb.includeAndroidHelpers=false`。

真机测试 `Pr9DeviceSmokeTest` 默认跳过。通过环境变量 `QADB_PR9_DEVICE` 指定测试设备序列号，`QADB_PR9_APKS` 指定仅含 `one.apk`、`two.apk` 的测试目录；包名分别为 `com.ludoven.qadb.pr9test.one` 和 `com.ludoven.qadb.pr9test.two`，应使用无权限、无业务代码的专用测试 APK。测试要求这两个包未安装，执行后卸载它们并删除本次创建的临时设备目录。不要使用业务 APK 作为此测试的输入。

此测试验证真实安装结果、上传文件内容及上传期间切换目录；拖放手势、双击和日志菜单仍需桌面交互检查。

## AI Agent 执行边界

- 生产入口只使用截图主导的单引擎，不在 V1、V2、Bridge 或 Workflow 之间选择和回退。
- 每个模型决策都必须包含最新设备截图；模型需通过 L3 视觉与工具调用能力测试。
- 模型只能选择打开应用、点击、输入、滑动、按键、等待、完成、询问用户或阻塞，不接收任意 shell/ADB 指令。
- 点击和滑动使用 `0..1000` 归一化坐标，由本地执行层映射到真实设备分辨率并校验截图 revision。
- 执行20步后进入软限制，在第20、25、30、35步检查实际页面变化和目标进展；第40步后强制交还用户。
- 危险操作由本地策略确认；结果不明确的发送、删除或购买动作不会自动重放。

## 界面预览

<div class="screenshot-strip">
  <img src="/screenshots/home.png" alt="首页截图">
  <img src="/screenshots/common.png" alt="命令页面截图">
  <img src="/screenshots/keyevent.png" alt="按键模拟截图">
  <img src="/screenshots/terminal.png" alt="终端截图">
</div>

## 规划中

- 性能面板：查看 CPU、内存、网络等设备指标。
- 更丰富的命令中心：沉淀更多高频调试动作。
