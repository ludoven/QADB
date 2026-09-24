rootProject.name = "AdbTool-Desktop"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")

val includeAndroidHelpers = providers.gradleProperty("qadb.includeAndroidHelpers")
    .map { value ->
        when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> error("qadb.includeAndroidHelpers must be true or false")
        }
    }
    .getOrElse(true)

if (includeAndroidHelpers) {
    include(":qadb-icon-helper")
    include(":qadb-agent-ime")
}

val includeArtemisEvaluationFixture = providers.gradleProperty("qadb.includeArtemisEvaluationFixture")
    .map { value ->
        when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> error("qadb.includeArtemisEvaluationFixture must be true or false")
        }
    }
    .getOrElse(false)

if (includeArtemisEvaluationFixture) {
    include(":qadb-artemis-evaluation-fixture")
}
