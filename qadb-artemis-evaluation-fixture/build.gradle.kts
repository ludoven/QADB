plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.ludoven.qadb.artemisevaluation"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ludoven.qadb.artemisevaluation"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
