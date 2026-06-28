import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val googleWebClientId = providers.gradleProperty("GOOGLE_WEB_CLIENT_ID").orNull
    ?: localProperties.getProperty("GOOGLE_WEB_CLIENT_ID")
    ?: ""

val pollinationsPublicKey = providers.gradleProperty("POLLINATIONS_PUBLIC_KEY").orNull
    ?: localProperties.getProperty("POLLINATIONS_PUBLIC_KEY")
    ?: ""

android {
    namespace = "com.pregnancydiet.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pregnancydiet.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resValue("string", "google_web_client_id", googleWebClientId)
        buildConfigField("String", "POLLINATIONS_PUBLIC_KEY", "\"$pollinationsPublicKey\"")
        buildConfigField("String", "POLLINATIONS_BASE_URL", "\"https://text.pollinations.ai\"")
        buildConfigField("String", "POLLINATIONS_GEN_BASE_URL", "\"https://gen.pollinations.ai\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.androidx.security.crypto)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.google.play.services.auth)

    testImplementation(libs.junit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}