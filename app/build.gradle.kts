import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

/** Sans guillemets autour de la valeur : evite `BASE_URL=""https://...""` dans BuildConfig. */
fun Properties.localProp(key: String, default: String = ""): String {
    val raw = getProperty(key, default)?.trim().orEmpty()
    return raw.removeSurrounding("\"").removeSurrounding("'")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.paparazzi)
}

val legacyBaseUrl = localProperties.localProp("BASE_URL")
val debugBackendUrl = localProperties.localProp("BACKEND_BASE_URL_DEBUG")
    .ifBlank { legacyBaseUrl.ifBlank { "http://10.0.2.2:3000/api/v1" } }
val stagingBackendUrl = localProperties.localProp("BACKEND_BASE_URL_STAGING")
    .ifBlank { "https://staging-api.turbodrive.tn/api/v1" }
val releaseBackendUrl = localProperties.localProp("BACKEND_BASE_URL_RELEASE")
    .ifBlank { legacyBaseUrl.ifBlank { "https://api.turbodrive.tn/api/v1" } }
val keystorePath = localProperties.localProp("KEYSTORE_PATH")
val keystoreStorePassword = localProperties.localProp("KEYSTORE_STORE_PASSWORD")
val keystoreKeyPassword = localProperties.localProp("KEYSTORE_KEY_PASSWORD")
val keystoreKeyAlias = localProperties.localProp("KEYSTORE_KEY_ALIAS", "my-key-alias")
val keystoreFile = keystorePath.takeIf { it.isNotEmpty() }?.let { rootProject.file(it) }
val enableReleaseSigning =
    localProperties.localProp("ENABLE_RELEASE_SIGNING").equals("true", ignoreCase = true)
val keystoreConfigured =
    enableReleaseSigning &&
        keystoreFile != null &&
        keystoreFile.exists() &&
        keystoreStorePassword.isNotEmpty() &&
        keystoreKeyPassword.isNotEmpty()

android {
    namespace = "com.dadadrive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dadadrive"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.localProp("GOOGLE_WEB_CLIENT_ID")}\""
        )
        buildConfigField("String", "CERTIFICATE_PINS", "\"${localProperties.localProp("CERTIFICATE_PINS")}\"")
        buildConfigField("String", "HERE_ACCESS_KEY_ID",
            "\"${localProperties.localProp("HERE_ACCESS_KEY_ID")}\"")
        buildConfigField("String", "HERE_ACCESS_KEY_SECRET",
            "\"${localProperties.localProp("HERE_ACCESS_KEY_SECRET")}\"")
        manifestPlaceholders["HERE_ACCESS_KEY_ID"] =
            localProperties.localProp("HERE_ACCESS_KEY_ID")
        manifestPlaceholders["HERE_ACCESS_KEY_SECRET"] =
            localProperties.localProp("HERE_ACCESS_KEY_SECRET")
    }

    // Opt-in release signing (ENABLE_RELEASE_SIGNING=true); otherwise release uses debug keystore so ./gradlew build works locally.
    signingConfigs {
        if (keystoreConfigured) {
            create("release") {
                storeFile = keystoreFile
                storePassword = keystoreStorePassword
                keyAlias = keystoreKeyAlias
                keyPassword = keystoreKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("String", "BASE_URL", "\"$debugBackendUrl\"")
            buildConfigField("boolean", "ENABLE_CERT_PINNING", "false")
        }
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            buildConfigField("String", "BASE_URL", "\"$stagingBackendUrl\"")
            buildConfigField("boolean", "ENABLE_CERT_PINNING", "true")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"$releaseBackendUrl\"")
            buildConfigField("boolean", "ENABLE_CERT_PINNING", "true")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig =
                if (keystoreConfigured) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
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
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files(rootProject.file("detekt.yml")))
    ignoreFailures = true
}

ktlint {
    android.set(true)
    ignoreFailures.set(true)
}

dependencies {
    implementation(libs.timber)
    implementation(libs.androidx.core.ktx)
    // AppCompatDelegate.setApplicationLocales — équiv. Swift : .environment(\\.locale, …)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
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

    // Google Sign-In : flux classique (sélecteur de compte système) — plus fiable que Credential Manager seul sur certains OEM (ex. MIUI).
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    // Une seule dépendance location (évite résolution/transitifs doublonnés).
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // OTP auto depuis SMS (API SMS Retriever — ne s’applique pas à WhatsApp)
    implementation("com.google.android.gms:play-services-auth-api-phone:18.1.0")
    // Coil — chargement d'images (photos de profil)
    implementation("io.coil-kt:coil-compose:2.7.0")
    // Équivalent Swift : TokenStore.swift (stockage chiffré)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(files("libs/heresdk-explore-android-4.25.5.0.274356.aar"))
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.socketIoClient)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("io.mockk:mockk:1.13.11")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}