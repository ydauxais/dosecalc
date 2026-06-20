import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Signature release résolue dans cet ordre :
//  1. variables d'environnement (CI GitHub Actions) ;
//  2. keystore.properties local (non versionné) ;
//  3. aucune -> build release non signé (F-Droid signe en aval).
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

data class ReleaseSigning(val storeFile: String, val storePassword: String, val keyAlias: String, val keyPassword: String)

val releaseSigning: ReleaseSigning? = when {
    System.getenv("SIGNING_KEYSTORE_FILE") != null -> ReleaseSigning(
        storeFile = System.getenv("SIGNING_KEYSTORE_FILE"),
        storePassword = System.getenv("SIGNING_STORE_PASSWORD").orEmpty(),
        keyAlias = System.getenv("SIGNING_KEY_ALIAS").orEmpty(),
        keyPassword = System.getenv("SIGNING_KEY_PASSWORD").orEmpty(),
    )
    keystorePropsFile.exists() -> ReleaseSigning(
        storeFile = keystoreProps.getProperty("storeFile"),
        storePassword = keystoreProps.getProperty("storePassword"),
        keyAlias = keystoreProps.getProperty("keyAlias"),
        keyPassword = keystoreProps.getProperty("keyPassword"),
    )
    else -> null
}

android {
    namespace = "fr.dauxais.dosecalc"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.dauxais.dosecalc"
        minSdk = 24
        targetSdk = 36
        // Surchargés par la CI (tag git) ; valeurs par défaut pour les builds locaux.
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = (System.getenv("VERSION_NAME") ?: "1.0").removePrefix("v")
    }

    signingConfigs {
        releaseSigning?.let { s ->
            create("release") {
                storeFile = rootProject.file(s.storeFile)
                storePassword = s.storePassword
                keyAlias = s.keyAlias
                keyPassword = s.keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (releaseSigning != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    // Sources Kotlin dans src/<set>/kotlin (cœur métier pur isolé sous logic/).
    sourceSets {
        getByName("main").kotlin.srcDir("src/main/kotlin")
        getByName("test").kotlin.srcDir("src/test/kotlin")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Le cœur métier est du Kotlin pur : tests JUnit en JVM, sans émulateur.
    testImplementation(libs.junit)
}
