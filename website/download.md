# 下载

请前往 GitHub Releases 下载最新版本：

[打开 Releases](https://github.com/ludoven/QADB/releases)

## 推荐安装包

| 平台 | 推荐安装包 | 说明 |
| --- | --- | --- |
| Windows | `.msi` | 推荐普通用户使用 |
| Windows | `.exe` | 免安装 / 便携版本 |
| Windows | `.zip` | 解压即用（便携包） |
| macOS | `.dmg` | 适用于 macOS 用户 |
| Linux | `.deb` | Debian / Ubuntu / Linux Mint 等发行版 |
| Linux | `.rpm` | Fedora / openSUSE / RHEL 系发行版 |
| Linux | `.tar.gz` | 解压即用（便携包） |

## macOS 打开提示

如果 macOS 提示“无法验证开发者”或“无法打开”，可以在「系统设置」->「隐私与安全性」中允许打开。

## Linux 提示

Linux 版本会优先使用内置 ADB。设备镜像依赖 `scrcpy`，如果安装包内没有对应的 Linux `scrcpy` 资源，请先通过系统包管理器安装 `scrcpy`，或通过 `SCRCPY_PATH` 环境变量指定可执行文件路径。

## 从源码构建

如果你想从源码运行桌面应用，可以在项目根目录执行：

```bash
./gradlew :composeApp:run
```

只验证桌面代码时可关闭 Android helper 子项目，使测试仅依赖 JDK：

```powershell
.\gradlew.bat :composeApp:desktopTest '-Pqadb.includeAndroidHelpers=false'
```

`qadb.includeAndroidHelpers` 默认为 `true`。正常运行和制作安装包时不要关闭；发布产物仍会同步图标与输入法 helper。

如果只是预览官网，可以执行：

```bash
npm run docs:dev
```
