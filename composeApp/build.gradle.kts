import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@CacheableTask
abstract class GenerateAppVersionTask : DefaultTask() {
    @get:Input
    abstract val versionName: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.ludoven.adbtool

            object AppVersion {
                const val CURRENT = "${versionName.get()}"
            }
            """.trimIndent()
        )
    }
}

abstract class RenameWindowsPackageFilesTask : DefaultTask() {
    @get:Input
    abstract val versionName: Property<String>

    @get:Internal
    abstract val binariesDir: DirectoryProperty

    @TaskAction
    fun rename() {
        val outputRoots = listOf("main", "main-release")
        val packageTypes = listOf("msi", "exe")
        val rootDir = binariesDir.get().asFile
        val versionSuffix = "-${versionName.get()}"

        outputRoots.forEach { outputRoot ->
            packageTypes.forEach packageTypeLoop@{ packageType ->
                val packageDir = rootDir.resolve("$outputRoot/$packageType")
                if (!packageDir.isDirectory) return@packageTypeLoop

                packageDir.listFiles { file ->
                    file.isFile && file.extension.equals(packageType, ignoreCase = true)
                }?.forEach sourceLoop@{ source ->
                    if (source.nameWithoutExtension.contains(versionSuffix)) return@sourceLoop

                    val target = source.resolveSibling("${source.nameWithoutExtension}$versionSuffix.${source.extension}")
                    if (target.exists() && !target.delete()) {
                        throw GradleException("Unable to replace existing Windows package: ${target.absolutePath}")
                    }
                    if (!source.renameTo(target)) {
                        throw GradleException("Unable to rename Windows package: ${source.absolutePath}")
                    }
                }
            }
        }
    }
}

abstract class VerifyHelperReleaseSigningTask : DefaultTask() {
    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun verify() {
        val requiredVariables = listOf(
            "QADB_HELPER_KEYSTORE",
            "QADB_HELPER_STORE_PASSWORD",
            "QADB_HELPER_KEY_PASSWORD",
            "QADB_HELPER_KEY_ALIAS"
        )
        val missingVariables = requiredVariables.filter { System.getenv(it).isNullOrBlank() }
        if (missingVariables.isNotEmpty()) {
            throw GradleException(
                "Release packaging requires helper signing variables: ${missingVariables.joinToString()}."
            )
        }

        val keystore = File(requireNotNull(System.getenv("QADB_HELPER_KEYSTORE")))
        if (!keystore.isFile) {
            throw GradleException("QADB helper release keystore does not exist: ${keystore.absolutePath}")
        }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val appVersion = "2.1.2"
val currentOsName = System.getProperty("os.name").lowercase()
val nativeTargetFormats = when {
    currentOsName.contains("mac") -> arrayOf(TargetFormat.Dmg)
    currentOsName.contains("windows") -> arrayOf(TargetFormat.Msi, TargetFormat.Exe)
    currentOsName.contains("linux") -> arrayOf(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.AppImage)
    else -> emptyArray()
}
val generatedVersionSourceDir = layout.buildDirectory.dir("generated/source/appVersion/desktopMain/kotlin")
val generatedIconHelperResourcesDir = layout.buildDirectory.dir("generated/resources/iconHelper")
val generatedAgentImeResourcesDir = layout.buildDirectory.dir("generated/resources/agentIme")
val includeAndroidHelpers = providers.gradleProperty("qadb.includeAndroidHelpers")
    .map { value ->
        when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> error("qadb.includeAndroidHelpers must be true or false")
        }
    }
    .getOrElse(true)
val agentImeReleaseSigningReady = listOf(
    "QADB_HELPER_KEYSTORE",
    "QADB_HELPER_STORE_PASSWORD",
    "QADB_HELPER_KEY_PASSWORD",
    "QADB_HELPER_KEY_ALIAS"
).all { !System.getenv(it).isNullOrBlank() }
val agentImeVariant = if (agentImeReleaseSigningReady) "release" else "debug"
val verifyHelperReleaseSigning = tasks.register<VerifyHelperReleaseSigningTask>("verifyHelperReleaseSigning") {
    group = "verification"
    description = "Fails release packaging unless both bundled Android helpers can use the stable release signing key."
}
val generateDesktopAppVersion = tasks.register<GenerateAppVersionTask>("generateDesktopAppVersion") {
    versionName.set(appVersion)
    outputFile.set(generatedVersionSourceDir.map { it.file("com/ludoven/adbtool/AppVersion.kt") })
}
val syncIconHelperResource = if (includeAndroidHelpers) {
    tasks.register<Copy>("syncIconHelperResource") {
        group = "build"
        description = "Copies qadb-icon-helper into desktop runtime resources."
        dependsOn(":qadb-icon-helper:assembleIconHelperDex")
        from(project(":qadb-icon-helper").layout.buildDirectory.file("outputs/qadb-icon-helper.jar"))
        into(generatedIconHelperResourcesDir.map { it.dir("qadb") })
    }
} else {
    tasks.register("syncIconHelperResource") {
        group = "build"
        description = "Skips qadb-icon-helper for JDK-only desktop verification."
    }
}
val syncAgentImeResource = if (includeAndroidHelpers) {
    tasks.register<Copy>("syncAgentImeResource") {
        group = "build"
        description = "Copies the QADB Unicode input helper APK into desktop runtime resources."
        dependsOn(":qadb-agent-ime:assemble${agentImeVariant.replaceFirstChar { it.uppercase() }}")
        from(
            project(":qadb-agent-ime").layout.buildDirectory.file(
                "outputs/apk/$agentImeVariant/qadb-agent-ime-$agentImeVariant.apk"
            )
        )
        into(generatedAgentImeResourcesDir.map { it.dir("qadb") })
        rename { "qadb-agent-ime.apk" }
    }
} else {
    tasks.register("syncAgentImeResource") {
        group = "build"
        description = "Skips qadb-agent-ime for JDK-only desktop verification."
    }
}
val renameWindowsPackageFiles = tasks.register<RenameWindowsPackageFilesTask>("renameWindowsPackageFiles") {
    group = "compose desktop"
    description = "Adds the app version to generated Windows installer filenames."
    versionName.set(appVersion)
    binariesDir.set(layout.buildDirectory.dir("compose/binaries"))
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting {
            kotlin.srcDir(generatedVersionSourceDir)
            resources.srcDir(generatedIconHelperResourcesDir)
            resources.srcDir(generatedAgentImeResourcesDir)
        }
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // JetBrains Compose Multiplatform 的 ViewModel 支持
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta03")


            val fileKit = "0.10.0"
            implementation("io.github.vinceglb:filekit-core:$fileKit")
            implementation("io.github.vinceglb:filekit-dialogs:$fileKit")
            implementation("io.github.vinceglb:filekit-dialogs-compose:$fileKit")
            implementation("io.github.vinceglb:filekit-coil:$fileKit")
            implementation("net.java.dev.jna:jna:5.18.1")
            implementation("net.java.dev.jna:jna-platform:5.18.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("org.xerial:sqlite-jdbc:3.53.1.0")

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("br.com.devsrsouza.compose.icons:tabler-icons-desktop:1.1.1")
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.ludoven.adbtool.MainKt"
        jvmArgs += listOf(
            "-Djna.nosys=true",
            "-Dawt.useSystemAAFontSettings=on"
        )
        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard-rules.pro"))
        }

        nativeDistributions {
            targetFormats(*nativeTargetFormats)
            modules("java.base", "java.desktop", "java.logging", "java.net.http", "java.sql", "java.naming", "jdk.unsupported")
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/desktopMain/appResources"))

            packageVersion = appVersion
            packageName = "QADB"

//            iconFile.set(project.file("src/desktopMain/composeResources/icons/app_icon.icns")) // macOS 图标
            windows.iconFile.set(project.file("src/desktopMain/composeResources/drawable/app_icon.ico")) // Windows 图标
            windows {
                // Ensure installers create desktop/start-menu entries by default on Windows.
                shortcut = true
                menu = true
                menuGroup = "QADB"
            }

            linux {
                packageName = "qadb"
                shortcut = true
                menuGroup = "Development"
                appCategory = "Development"
                debMaintainer = "ludoven"
                rpmLicenseType = "MIT"
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/ic_logo.png"))
            }

            macOS {
                bundleID = "com.ludoven.adbtool"
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/app_icon.icns")) // macOS 图标
            }
        }
    }
}
tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateDesktopAppVersion)
    compilerOptions {
        freeCompilerArgs.add("-Xnon-local-break-continue")
    }
}
tasks.withType<Test>().configureEach {
    listOf(
        "qadb.agent.device",
        "qadb.agent.realModelDevice",
        "qadb.agent.realV2ReadDevice"
    ).forEach { propertyName ->
        System.getProperty(propertyName)?.let { systemProperty(propertyName, it) }
    }
}
tasks.matching { it.name == "desktopProcessResources" || it.name == "processDesktopMainResources" }.configureEach {
    dependsOn(syncIconHelperResource)
    dependsOn(syncAgentImeResource)
}
tasks.matching {
    it.name.startsWith("packageRelease") || it.name == "createReleaseDistributable"
}.configureEach {
    dependsOn(verifyHelperReleaseSigning)
}
tasks.matching {
    it.name.startsWith("package") || it.name.contains("Distributable")
}.configureEach {
    if (!includeAndroidHelpers) {
        doFirst {
            throw GradleException("Desktop packaging requires -Pqadb.includeAndroidHelpers=true.")
        }
    }
}
listOf(
    "packageMsi",
    "packageExe",
    "packageReleaseMsi",
    "packageReleaseExe",
).forEach { taskName ->
    tasks.matching { it.name == taskName }.configureEach {
        finalizedBy(renameWindowsPackageFiles)
    }
}
