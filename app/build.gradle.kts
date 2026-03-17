import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    // Firebase
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// ============================================
// LOCAL PROPERTIES (Supabase & Google credentials)
// ============================================
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

fun requireProperty(name: String): String {
    return localProperties.getProperty(name)
        ?: throw GradleException("Missing: $name in local.properties")
}

android {
    namespace = "co.alcheclub.ai.trading.assistant"
    compileSdk = 36

    defaultConfig {
        applicationId = "co.alcheclub.ai.trading.assistant"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase & Google credentials from local.properties
        buildConfigField("String", "SUPABASE_URL", "\"${requireProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${requireProperty("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${requireProperty("GOOGLE_WEB_CLIENT_ID")}\"")

        // API keys for networking layer
        buildConfigField("String", "GEMINI_API_KEYS", "\"${requireProperty("GEMINI_API_KEYS")}\"")
        buildConfigField("String", "BINANCE_API_KEYS", "\"${requireProperty("BINANCE_API_KEYS")}\"")
        buildConfigField("String", "ALPACA_API_KEY_ID", "\"${requireProperty("ALPACA_API_KEY_ID")}\"")
        buildConfigField("String", "ALPACA_API_SECRET_KEY", "\"${requireProperty("ALPACA_API_SECRET_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    // ACCCore - AlcheClub DI & Services (includes Firebase BOM + Analytics)
    implementation(libs.acccore)
    implementation(libs.acccore.firebase)

    // AndroidX Core
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth.kt)
    implementation(libs.supabase.postgrest.kt)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)

    // Google Sign-In (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.googleid)

    // Play In-App Review
    implementation(libs.play.review)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
