---
layout: home

hero:
  name: QADB
  text: 现代化 ADB 桌面调试工具
  tagline: 面向 Android 开发、测试和 TV 盒子调试场景，把高频 ADB 命令整理成直观的跨平台图形界面。
  image:
    src: /screenshots/home.png
    alt: QADB 首页截图
  actions:
    - theme: brand
      text: 立即下载
      link: /download
    - theme: alt
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: GitHub
      link: https://github.com/ludoven/QADB

features:
  - title: 设备管理
    details: 自动检测 USB 与网络 ADB 设备，快速切换当前调试目标。
  - title: 应用管理
    details: 安装 APK、卸载应用、清除数据、强制停止、导出 APK 都能在界面中完成。
  - title: 日志与终端
    details: 内置 Logcat 查看器和命令终端，减少在多个工具之间切换。
  - title: 按键模拟
    details: 支持返回、主页、菜单、音量、方向键等常用 KeyEvent。
  - title: TV / 盒子调试
    details: 适合 Android TV、机顶盒、系统应用和预装应用调试。
  - title: 跨平台
    details: 基于 Jetpack Compose Multiplatform，支持 Windows 与 macOS。
---

<section class="home-section">
  <h2>把常用 ADB 操作变成清晰的工作台</h2>
  <p>
    QADB 适合经常执行 adb devices、adb install、adb logcat、adb shell input keyevent 等命令的用户。
    它保留命令行的灵活性，同时把设备状态、应用列表、日志、快捷命令和按键操作集中在一个桌面应用里。
  </p>

  <div class="screenshot-strip">
    <img src="/screenshots/applist.png" alt="应用管理截图">
    <img src="/screenshots/logcat.png" alt="日志查看截图">
  </div>
</section>

<section class="home-section">
  <h2>适合这些调试场景</h2>
  <div class="scenario-grid">
    <div class="scenario-card">
      <h3>Android 应用开发</h3>
      <p>快速安装 APK、清除应用数据、强制停止应用、查看当前 Activity，并在复现问题时收集截图和日志。</p>
    </div>
    <div class="scenario-card">
      <h3>测试与问题复现</h3>
      <p>切换设备、执行常用命令、筛选 Logcat，让测试人员更快定位应用运行问题。</p>
    </div>
    <div class="scenario-card">
      <h3>Android TV / 盒子</h3>
      <p>通过网络 ADB 连接局域网设备，模拟遥控器按键，调试系统设置、启动页和预装应用。</p>
    </div>
  </div>
</section>
