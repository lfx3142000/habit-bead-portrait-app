plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val ciBuildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1000
val portraitVersionCode = 1000 + ciBuildNumber
val releaseKeystorePath = System.getenv("HABIT_BEADS_RELEASE_STORE_FILE")

android {
    namespace = "com.habitbeads.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.habitbeads.portrait"
        minSdk = 24
        targetSdk = 35
        versionCode = portraitVersionCode
        versionName = "0.1.0-portrait.$portraitVersionCode"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("debug/habitbeads-portrait-debug.keystore")
            storePassword = "habitbeads"
            keyAlias = "habitbeads-portrait-debug"
            keyPassword = "habitbeads"
        }
        if (!releaseKeystorePath.isNullOrBlank()) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("HABIT_BEADS_RELEASE_STORE_PASSWORD")
                keyAlias = System.getenv("HABIT_BEADS_RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("HABIT_BEADS_RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (!releaseKeystorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
