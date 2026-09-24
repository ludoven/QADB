<h1 align="center">QADB</h1>

<p align="center">
  <b>一款开源、跨平台、现代化的 ADB 桌面调试工具</b>
</p>

<p align="center">
  让常用 ADB 操作变得更直观、更高效，不再反复手敲命令。
</p>

<p align="center">
  <a href="https://ludoven.github.io/QADB/">官网</a> |
  <a href="./README_CN.md">中文</a> |
  <a href="./README.md">English</a>
</p>

<p align="center">
  <img src="https://img.shields.io/github/stars/ludoven/QADB?style=flat-square" alt="GitHub Stars" />
  <img src="https://img.shields.io/github/forks/ludoven/QADB?style=flat-square" alt="GitHub Forks" />
  <img src="https://img.shields.io/github/v/release/ludoven/QADB?style=flat-square" alt="GitHub Release" />
  <img src="https://img.shields.io/github/license/ludoven/QADB?style=flat-square" alt="License" />
</p>

---

## 🏠 首页预览

![Home](./screenshots/home.png)

---

## 📝 更新日志

### 当前开发版（2.1.2）

- 日志与进程统一到“诊断”工作区，支持进程搜索、排序和资源占用查看
- 增加 Linux 的 `.deb`、`.rpm` 与 `.tar.gz` 发布产物
- AI Agent Beta 改为截图主导的单引擎循环，要求视觉与工具调用能力；UI 结构仅作可选提示

### v2.0.7 - 2026-05-29

- 聚焦性能优化：终端输出、Logcat 过滤、应用列表加载与设备信息解析等链路整体提速
- 交互体验增强：危险操作确认、空状态引导、文件管理与进程页细节体验优化
- 深色模式与国际化修复：多页面主题色语义化和文案国际化覆盖完善
- 详细内容见 [CHANGELOG-v2.0.7.md](./CHANGELOG-v2.0.7.md)

### v2.0.6 - 2026-05-19

- 新增镜像功能，支持在独立窗口中查看并控制 Android 设备

### v2.0.5 - 2026-05-12

- 新增内置 ADB，默认开箱即用，减少手动安装和配置环境变量的步骤
- 重构终端页面，优化命令输入、执行和结果展示体验
- 优化命令中心布局，让常用命令入口更清晰、更易查找

---

## 🚀 项目介绍

**QADB** 是一款基于 **Jetpack Compose Multiplatform** 开发的跨平台 ADB 图形化工具，支持 **Windows**、**macOS** 和 **Linux**。

它面向 Android 开发者、测试人员、Android TV / 电视盒子调试人员以及经常使用 ADB 命令的高级用户，将常用 ADB 操作封装为直观的可视化界面，帮助你更高效地完成设备调试、应用管理、日志查看、截图录屏、终端执行、按键模拟等操作。

如果你经常需要输入类似下面的命令：

```bash
adb devices
adb install app.apk
adb shell pm clear package.name
adb shell am force-stop package.name
adb logcat
adb shell screencap
adb shell input keyevent 3
```

那么 QADB 可以让这些操作变得更简单。

---

## ✨ 功能亮点

- **设备管理**：检测 USB / 网络 ADB 设备，快速切换当前设备
- **设备信息**：查看设备型号、Android 版本、屏幕信息、连接状态等
- **应用管理**：安装 APK、卸载应用、清除数据、强制停止、导出 APK；支持从文件夹批量安装 APK、双击查看应用信息
- **常用操作**：重启、关机、截图、录屏、打开设置、查看 Activity 等
- **按键模拟**：支持返回、主页、菜单、音量、方向键等常用 KeyEvent
- **内置终端**：无需切换系统终端，直接执行 adb / shell 命令
- **日志查看**：查看 Logcat 日志，辅助定位应用运行问题
- **诊断工作区**：集中查看 Logcat 与设备进程，支持进程搜索、排序和资源占用查看
- **命令中心**：将高频 ADB 命令整理为可视化按钮，支持多设备并发执行、逐台结果查看和失败重试
  - 批量执行确认后固定本次输入参数；失败重试沿用原参数，新批次结果与旧批次隔离。
- **AI Agent Beta**：默认关闭；启用后由视觉模型根据每一步最新截图执行单个受控动作
- **TV / 盒子调试**：适合 Android TV、机顶盒、系统应用调试等场景
- **跨平台支持**：基于 Compose Multiplatform，支持 Windows、macOS 与 Linux

---

## 🎯 适合人群

QADB 适合以下用户：

- Android 开发者
- Android 测试人员
- Android TV / 电视盒子调试人员
- 系统应用 / 预装应用调试人员
- 经常使用 ADB 命令的高级用户
- 需要提升调试效率的开发团队

---

## 🧩 功能模块

| 模块 | 说明 |
|---|---|
| 首页 | 展示设备状态、快捷入口、常用操作 |
| 命令 | 常用 ADB 命令快捷执行 |
| 设备控制 | 模拟 Android 设备按键与常用控制操作 |
| 应用 | 应用列表、安装、卸载、清数据、强制停止等 |
| 文件 | 浏览、上传、下载和管理设备文件 |
| 诊断 | 查看和筛选 Logcat，搜索、排序并查看设备进程 |
| 终端 | 内置命令行终端，可执行 adb / shell 命令 |
| AI Agent | 截图主导的单引擎逐步执行，危险操作由本地确认 |
| 设置 | 提供概览、通用、ADB、实验功能和关于分区，可配置启动检测、设备恢复、托盘行为、ADB 来源与更新 |

---

## 📦 下载

请前往 [GitHub Releases](https://github.com/ludoven/QADB/releases) 下载最新版本。

| 平台 | 推荐安装包 | 说明 |
|---|---|---|
| Windows | `.msi` | 推荐普通用户使用 |
| Windows | `.exe` | 免安装 / 便携版本 |
| Windows | `.zip` | 解压即用（便携包） |
| macOS | `.dmg` | 适用于 macOS 用户 |
| Linux | `.deb` / `.rpm` | 适用于主流 Linux 发行版 |
| Linux | `.tar.gz` | 解压即用（便携包） |

> macOS 如果提示“无法验证开发者”或“无法打开”，可以在「系统设置」→「隐私与安全性」中允许打开。

---

## 📸 界面预览

![Apps](./screenshots/applist.png)
![Logcat](./screenshots/logcat.png)
![Terminal](./screenshots/terminal.png)

---

## ⚡ 快速开始

### 1. 准备 ADB

QADB v2.0.5 起已内置 ADB，默认可直接使用。

如果你需要使用自己安装的 ADB，也可以在设置中配置自定义 ADB 路径。常见来源包括：

- Android Studio 用户通常已经自带 ADB
- 单独下载 Android SDK Platform Tools

如需验证自定义 ADB 环境，可以在终端中执行：

```bash
adb version
```

如果能看到 ADB 版本信息，说明自定义环境正常。

---

### 2. 开启设备调试

在 Android 设备上开启：

1. 打开「开发者选项」
2. 开启「USB 调试」
3. 使用 USB 连接电脑
4. 在设备上允许 USB 调试授权

然后执行：

```bash
adb devices
```

如果能看到设备列表，说明设备连接成功。

---

### 3. 启动 QADB

打开 QADB 后：

1. 选择当前设备
2. 查看设备状态
3. 使用常用操作、应用管理、日志、终端等功能

---

## 🔌 网络 ADB 使用方式

如果你需要连接 Android TV、电视盒子或局域网设备，可以使用网络 ADB。

常见连接命令：

```bash
adb connect 192.168.1.100:5555
```

连接成功后，设备会出现在 QADB 的设备列表中。

> 不同设备开启网络 ADB 的方式可能不同，部分设备需要先通过 USB 或系统设置开启无线调试。

---

## 🛠️ 常见使用场景

### Android 应用开发

- 快速安装 APK
- 清除应用数据
- 强制停止应用
- 查看应用日志
- 查看当前 Activity
- 截图和录屏

### Android TV / 盒子调试

- 网络 ADB 连接设备
- 模拟遥控器按键
- 安装或卸载 APK
- 查看系统应用信息
- 调试系统设置或启动页面

### 测试人员

- 快速切换设备
- 批量执行常用命令
- 查看日志和截图
- 复现问题时快速收集信息

---

## ❓ 常见问题

### 1. 为什么检测不到设备？

请确认：

- 已使用内置 ADB，或自定义 ADB 路径配置正确
- `adb devices` 可以在终端正常执行（使用自定义 ADB 时）
- 手机或设备已开启 USB 调试
- 设备已允许 USB 调试授权
- USB 数据线支持数据传输
- Windows 用户已安装对应设备驱动

---

### 2. 为什么提示找不到 adb？

请先在设置中确认当前 ADB 来源和路径状态。QADB 会优先使用可用的内置 ADB；如果你切换为自定义 ADB，请确认路径配置正确。

使用自定义 ADB 时，可以在终端执行：

```bash
adb version
```

如果提示命令不存在，需要安装 Android Platform Tools，或改回使用 QADB 内置 ADB。

---

### 3. macOS 提示无法打开怎么办？

如果 macOS 提示无法验证开发者，可以尝试：

1. 打开「系统设置」
2. 进入「隐私与安全性」
3. 找到 QADB 的拦截提示
4. 点击「仍要打开」

---

### 4. 是否内置 ADB？

QADB v2.0.5 起已内置 ADB，默认开箱即用，同时保留自定义 ADB 路径配置，方便使用系统已安装的 Platform Tools。

---

### 5. 是否支持 Linux？

支持。发布流程会生成 `.deb`、`.rpm` 和 `.tar.gz`；设备镜像仍依赖可用的 `scrcpy`，详见[下载说明](./website/download.md)。

---

## 🗺️ Roadmap

- [x] 设备管理
- [x] 设备信息展示
- [x] 常用 ADB 操作
- [x] 应用管理
- [x] 按键模拟
- [x] 截图 / 录屏
- [x] 内置终端
- [x] 日志查看
- [x] 进程查看、搜索和排序
- [x] 文件管理
- [x] ADB 路径可视化配置
- [x] Linux 安装包与便携包
- [x] AI Agent Beta（默认关闭）
- [ ] 性能监控
- [ ] 命令收藏
- [ ] 命令分组自定义
- [x] 多设备批量操作（命令中心 v1）
- [ ] 插件化命令扩展

---

## 🤝 参与贡献

欢迎提交 Issue、功能建议或 Pull Request。

你可以参与：

- 反馈 Bug
- 提交新的 ADB 命令
- 优化 UI / 交互体验
- 完善 Windows / macOS / Linux 兼容性
- 改进英文文档
- 补充使用教程
- 改进 Linux 打包与运行兼容性

如果你有常用的 ADB 命令，也欢迎提交建议，让 QADB 的命令中心更加完善。

---

## 🧪 本地开发

### 环境要求

- JDK 17 或更高版本
- Android Studio / IntelliJ IDEA
- Gradle
- Android SDK Platform Tools

### 克隆项目

```bash
git clone https://github.com/ludoven/QADB.git
cd QADB
```

### 运行项目

请使用 Android Studio 或 IntelliJ IDEA 打开项目，并运行对应的 Desktop 配置。

> 具体 Gradle task 可能会根据项目结构变化，请以项目实际配置为准。

---

## 📄 License

本项目基于开源协议发布，具体请查看 [LICENSE](./LICENSE)。

---

## 📮 联系方式

- 邮箱：ludoven2019@gmail.com

---

## ⭐ Star 支持

如果 QADB 对你有帮助，欢迎点一个 Star 支持项目。

你的 Star 是项目持续更新的重要动力。

<p align="center">
  <b>QADB：让 Android 调试更简单。</b>
</p>
