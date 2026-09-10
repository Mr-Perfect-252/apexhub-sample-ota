plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.apexhub.ota.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.apexhub.ota.sample"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "APEXHUB_PUBLIC_KEY", "\"pk_live_x-ApA2FX5yvnnDD-GjxXUAl9k9bAx10n\"")
        buildConfigField("String", "APEXHUB_APP_ID",     "\"c1804dc7-dcc9-45d7-9b7a-0f5e447e85ee\"")
        buildConfigField("String", "APEXHUB_PACKAGE",    "\"com.apexhub.ota.sample\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = "1.8" }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":sdk"))
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.lifecycle.runtime)
    implementation(libs.kotlinx.coroutines.android)

    // Unit tests (Robolectric) — deterministic, machine-checkable proof
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)

    // Instrumented tests (UiAutomator screenshot proof)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.uiautomator)
}
