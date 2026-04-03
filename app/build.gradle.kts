import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.dadadrive"
    compileSdk = 36  // ✅ CORRIGÉ (max actuel)

    defaultConfig {
        applicationId = "com.dadadrive"
        minSdk = 24
        targetSdk = 36  // ✅ CORRIGÉ
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\""
        )
        buildConfigField(
            "String",
            "BASE_URL",
            "\"${localProperties.getProperty("BASE_URL", "")}\""
        )
        buildConfigField("String", "HERE_ACCESS_KEY_ID",
            "\"${localProperties.getProperty("HERE_ACCESS_KEY_ID", "")}\"")
        buildConfigField("String", "HERE_ACCESS_KEY_SECRET",
            "\"${localProperties.getProperty("HERE_ACCESS_KEY_SECRET", "")}\"")
        manifestPlaceholders["HERE_ACCESS_KEY_ID"] =
            localProperties.getProperty("HERE_ACCESS_KEY_ID", "")
        manifestPlaceholders["HERE_ACCESS_KEY_SECRET"] =
            localProperties.getProperty("HERE_ACCESS_KEY_SECRET", "")
    }

    // ✅ APRÈS (sécurisé)
    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("KEYSTORE_PATH", ""))
            storePassword = localProperties.getProperty("KEYSTORE_STORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("KEYSTORE_KEY_ALIAS", "my-key-alias")
            keyPassword = localProperties.getProperty("KEYSTORE_KEY_PASSWORD", "")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.material)
    implementation("androidx.compose.material:material-icons-core")
    // Icônes carte / calques (sélecteur de fond, parité Swift MapTypePickerPanel).
    implementation("androidx.compose.material:material-icons-extended")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation.compose)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Google Sign-In via Credential Manager
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // Une seule dépendance location (évite résolution/transitifs doublonnés).
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Coil — chargement d'images (photos de profil)
    implementation("io.coil-kt:coil-compose:2.7.0")
    // Équivalent Swift : TokenStore.swift (stockage chiffré)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(files("libs/heresdk-explore-android-4.25.5.0.274356.aar"))


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}