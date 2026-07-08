plugins {
    alias(libs.plugins.android.application)
}

fun releaseSigningValue(name: String): String? {
    return providers.gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .orNull
        ?.takeIf { it.isNotBlank() }
}

android {
    namespace = "org.nodocentral.miviaje"
    compileSdk = 37

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "org.nodocentral.miviaje"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 27
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    val releaseKeystoreFile = releaseSigningValue("RELEASE_KEYSTORE_FILE")
    val releaseKeystorePassword = releaseSigningValue("RELEASE_KEYSTORE_PASSWORD")
    val releaseKeyAlias = releaseSigningValue("RELEASE_KEY_ALIAS")
    val releaseKeyPassword = releaseSigningValue("RELEASE_KEY_PASSWORD")
    val hasReleaseSigning = releaseKeystoreFile != null &&
            releaseKeystorePassword != null &&
            releaseKeyAlias != null &&
            releaseKeyPassword != null

    signingConfigs {
        getByName("debug") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(requireNotNull(releaseKeystoreFile))
                storePassword = requireNotNull(releaseKeystorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("androidTest") {
            assets.directories.add("$projectDir/schemas")
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.flexbox)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.preference)
    implementation(libs.room.runtime)
    implementation(libs.recyclerview)
    implementation(libs.recyclerview.fastscroll)
    implementation(libs.gson)
    implementation(libs.glide)
    implementation(libs.ucrop)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.room.testing)
    annotationProcessor(libs.room.compiler)
}
