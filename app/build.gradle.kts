plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

object AppConfig {
    const val PACKAGE_NAME = "com.android.soundrecorder"
    const val APP_NAME = "SoundRecorder"
    const val VERSION_NAME = "1.0.5"
    const val VERSION_CODE = 10005
    const val SDK_VERSION_TARGET = 35
    const val SDK_VERSION_MIN = 33
}

android {
    namespace = AppConfig.PACKAGE_NAME
    compileSdk = AppConfig.SDK_VERSION_TARGET

    defaultConfig {
        applicationId = AppConfig.PACKAGE_NAME
        minSdk = AppConfig.SDK_VERSION_MIN
        targetSdk = AppConfig.SDK_VERSION_TARGET
        versionCode = AppConfig.VERSION_CODE
        versionName = AppConfig.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Android 기본 라이브러리
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3-android:1.3.1")

    // Android 추가 라이브러리
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("com.google.dagger:hilt-android:2.54")
    ksp("com.google.dagger:hilt-android-compiler:2.54")

    // 외부 라이브러리

    // Kotlin Test
    testImplementation(libs.junit)

    // Android Test
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug Test
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register<Copy>(name = "release") {
    dependsOn("assembleRelease")
    from("${layout.buildDirectory.get()}/outputs/apk/release/app-release.apk")
    into(rootDir.parent)
    rename { _ -> "${AppConfig.APP_NAME}_${AppConfig.VERSION_NAME}.apk" }
    include("**/*release.apk")
    mustRunAfter(tasks.named("createReleaseApkListingFileRedirect"))
}
