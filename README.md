<h1 align="center">QADB</h1>

<p align="center">
  <b>An open-source, cross-platform, modern ADB desktop debugging tool</b>
</p>

<p align="center">
  Make common ADB operations more intuitive and efficient, without repeatedly typing commands.
</p>

<p align="center">
  <a href="https://ludoven.github.io/QADB/">Website</a> |
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

## 📝 Changelog

### Current development build (2.1.2)

- Unified logs and processes in Diagnostics, with process search, sorting, and resource usage.
- Added Linux `.deb`, `.rpm`, and `.tar.gz` release artifacts.
- Replaced the AI Agent Beta runtime with one screenshot-first engine that requires visual tool use; UI structure is only an optional hint.

### v2.0.7 - 2026-05-29

- Performance-focused release across terminal output, Logcat filtering, app list loading, and device info parsing.
- UX improvements for risky actions, empty-state guidance, file management, and process-page interactions.
- Dark theme and i18n fixes with broader semantic color usage and localization coverage.
- Full notes: [CHANGELOG-v2.0.7.md](./CHANGELOG-v2.0.7.md)

### v2.0.6 - 2026-05-19

- Added device mirroring support to view and control Android devices in a standalone window.

### v2.0.5 - 2026-05-12

- Added built-in ADB for out-of-the-box usage with less manual setup.
- Refactored the terminal page for improved command input, execution, and output viewing.
- Optimized the command center layout to make frequent commands easier to find.

---

## 🚀 Project Overview

**QADB** is a cross-platform ADB GUI tool built with **Jetpack Compose Multiplatform**, supporting **Windows**, **macOS**, and **Linux**.

It is designed for Android developers, testers, Android TV / TV box debugging users, and advanced users who frequently use ADB commands. It turns common ADB workflows into a visual interface so you can debug devices, manage apps, inspect logs, take screenshots and recordings, run terminal commands, and simulate key events more efficiently.

If you often run commands like:

```bash
adb devices
adb install app.apk
adb shell pm clear package.name
adb shell am force-stop package.name
adb logcat
adb shell screencap
adb shell input keyevent 3
```

QADB can make these tasks much easier.

---

## ✨ Key Features

- **Device Management**: Detect USB / network ADB devices and quickly switch the current target device
- **Device Info**: View model, Android version, screen information, connection status, and more
- **App Management**: Install APKs, uninstall apps, clear data, force stop apps, and export APKs
- **Common Operations**: Reboot, shutdown, screenshot, screen recording, open settings, inspect current Activity, etc.
- **Key Event Simulation**: Quickly trigger common key events such as Back, Home, Menu, Volume, and D-pad keys
- **Built-in Terminal**: Run adb / shell commands directly inside the app
- **Log Viewer**: View and filter Logcat logs to troubleshoot runtime issues
- **Diagnostics**: Inspect Logcat and device processes together, including process search, sorting, and resource usage
- **Command Center**: Organize frequently used ADB commands into visual actions, with multi-device execution, per-device results, and failed-device retry
  - Batch inputs are fixed at confirmation and reused for failed-device retries; results are isolated between batches.
- **AI Agent Beta**: Off by default; a visual model executes one guarded action from each fresh screenshot
- **TV / Box Debugging**: Suitable for Android TV, set-top boxes, and system app debugging scenarios
- **Cross-Platform**: Built on Compose Multiplatform, supports Windows, macOS, and Linux

---

## 🎯 Target Users

QADB is suitable for:

- Android developers
- Android testers
- Android TV / TV box debugging engineers
- System app / preinstalled app debugging engineers
- Advanced users who frequently use ADB commands
- Development teams that want to improve debugging efficiency

---

## 🧩 Functional Modules

| Module | Description |
|---|---|
| Home | Device status, quick entries, and common actions |
| Commands | Quick execution for frequently used ADB commands |
| Device Control | Simulate Android keys and common device controls |
| Apps | App list, install, uninstall, clear data, force stop, etc. |
| Files | Browse, upload, download, and manage device files |
| Diagnostics | View and filter Logcat, then search, sort, and inspect device processes |
| Terminal | Built-in command terminal for adb / shell commands |
| AI Agent | Screenshot-first single-engine execution with local safety confirmation |
| Settings | Overview plus General, ADB, Experimental, and About sections; configure startup detection, remembered devices, tray behavior, ADB sources, and updates |

---

## 📦 Download

Please go to [GitHub Releases](https://github.com/ludoven/QADB/releases) to download the latest version.

| Platform | Recommended Package | Notes |
|---|---|---|
| Windows | `.msi` | Recommended for regular users |
| Windows | `.exe` | Portable / no installation |
| Windows | `.zip` | Extract and run (portable package) |
| macOS | `.dmg` | For macOS users |
| Linux | `.deb` / `.rpm` | For mainstream Linux distributions |
| Linux | `.tar.gz` | Extract and run (portable package) |

> If macOS shows "cannot verify developer" or "cannot open", allow it in "System Settings" -> "Privacy & Security".

---

## 📸 Interface Preview

![Home](./screenshots/home.png)
![Apps](./screenshots/applist.png)
![Logcat](./screenshots/logcat.png)
![Terminal](./screenshots/terminal.png)

---

## ⚡ Quick Start

### 1. Prepare ADB

QADB includes built-in ADB since v2.0.5 and works out of the box.

If you prefer your own ADB installation, you can configure a custom ADB path in Settings. Common sources include:

- Android Studio (usually includes ADB)
- Android SDK Platform Tools (standalone download)

To verify a custom ADB environment, run:

```bash
adb version
```

If you can see the ADB version output, your custom environment is ready.

---

### 2. Enable Device Debugging

On your Android device:

1. Open "Developer options"
2. Enable "USB debugging"
3. Connect the device via USB
4. Allow USB debugging authorization on the device

Then run:

```bash
adb devices
```

If the device list appears, the connection is successful.

---

### 3. Launch QADB

After opening QADB:

1. Select the current device
2. Check device status
3. Use common actions, app management, logs, terminal, and other features

---

## 🔌 Network ADB

If you need to connect Android TV, TV boxes, or LAN devices, you can use network ADB.

Common connection command:

```bash
adb connect 192.168.1.100:5555
```

After a successful connection, the device will appear in the QADB device list.

> The way to enable network ADB varies by device. Some devices require USB or system settings to enable wireless debugging first.

---

## 🛠️ Common Scenarios

### Android App Development

- Quickly install APKs
- Clear app data
- Force stop apps
- Inspect app logs
- View current Activity
- Take screenshots and recordings

### Android TV / TV Box Debugging

- Connect via network ADB
- Simulate remote control key events
- Install or uninstall APKs
- View system app information
- Debug settings pages or launcher/startup pages

### QA / Testing

- Quickly switch devices
- Execute common commands in batches
- View logs and screenshots
- Collect information quickly during issue reproduction

---

## ❓ FAQ

### 1. Why is my device not detected?

Please confirm:

- Built-in ADB is enabled, or your custom ADB path is configured correctly
- `adb devices` works in your terminal (when using custom ADB)
- USB debugging is enabled on the device
- USB debugging authorization is accepted on the device
- Your USB cable supports data transfer
- Windows users have installed the correct device drivers

---

### 2. Why does it say adb is not found?

Please check the current ADB source and path status in Settings. QADB prefers available built-in ADB. If you switch to custom ADB, make sure the configured path is valid.

You can run:

```bash
adb version
```

If the command is not found, install Android Platform Tools first, or switch back to QADB built-in ADB.

---

### 3. What should I do if macOS says the app cannot be opened?

If macOS cannot verify the developer, try:

1. Open "System Settings"
2. Go to "Privacy & Security"
3. Find the blocked prompt for QADB
4. Click "Open Anyway"

---

### 4. Does QADB include ADB?

QADB includes built-in ADB since v2.0.5 for out-of-the-box usage.

You can still switch to a custom ADB path in Settings when you need to use your system-installed tools.

---

### 5. Is Linux supported?

Yes. The release workflow produces `.deb`, `.rpm`, and `.tar.gz` artifacts. Device mirroring still requires an available `scrcpy`; see the [download guide](./docs/download.md).

---

## 🗺️ Roadmap

- [x] Device management
- [x] Device information display
- [x] Common ADB operations
- [x] App management
- [x] Key event simulation
- [x] Screenshot / screen recording
- [x] Built-in terminal
- [x] Log viewer
- [x] Process viewing, search, and sorting
- [x] File management
- [x] Visual ADB path configuration
- [x] Linux installers and portable package
- [x] AI Agent Beta (off by default)
- [ ] Performance monitoring
- [ ] Command favorites
- [ ] Custom command grouping
- [x] Multi-device batch operations (Command Center v1)
- [ ] Plugin-based command extensions

---

## 🤝 Contributing

Issues, feature suggestions, and Pull Requests are welcome.

You can contribute by:

- Reporting bugs
- Submitting useful ADB commands
- Improving UI / interaction design
- Enhancing Windows / macOS / Linux compatibility
- Improving English documentation
- Adding usage tutorials
- Improving Linux packaging and runtime compatibility

If you have frequently used ADB commands, feel free to suggest them and help improve the QADB command center.

---

## 🧪 Local Development

### Requirements

- JDK 17 or above
- Android Studio / IntelliJ IDEA
- Gradle
- Android SDK Platform Tools

### Clone Project

```bash
git clone https://github.com/ludoven/QADB.git
cd QADB
```

### Run Project

Open the project with Android Studio or IntelliJ IDEA and run the Desktop configuration.

> Specific Gradle tasks may change with project structure updates. Please refer to the actual project configuration.

---

## 📄 License

This project is released under an open-source license. See [LICENSE](./LICENSE) for details.

---

## ⭐ Star Support

If QADB helps you, please consider giving this project a Star.

Your Star is an important motivation for continuous updates.

<p align="center">
  <b>QADB: Make Android debugging easier.</b>
</p>
