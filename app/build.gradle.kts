// Vespa - App Module Build Configuration
// Package: dev.vskelk.cdf

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
}

// KSP must run BEFORE KSP for Kotlin in this module
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val v = variant.name.replaceFirstChar { it.uppercase() }
            tasks.findByName("ksp${v}Kotlin")?.let { kspTask ->
                tasks.findByName("generate${v}Proto")?.let { protoTask ->
                    kspTask.dependsOn(protoTask)
                }
            }
        }
    }
}

android {
    namespace = "dev.vskelk.cdf"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.vskelk.cdf"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
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
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
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

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    // Compose BOM - pinned to 2024.12.01 per spec
    implementation(platform(androidx.compose.bom))
    implementation(androidx.compose.ui)
    implementation(androidx.compose.ui.graphics)
    implementation(androidx.compose.ui.tooling.preview)
    implementation(androidx.compose.material3)
    implementation(androidx.compose.material.icons.extended)
    implementation(androidx.compose.material3.adaptive.navsuite)
    implementation(androidx.compose.runtime.livedata)
    debugImplementation(androidx.compose.ui.tooling)

    // Core Android
    implementation(androidx.core.ktx)
    implementation(androidx.activity.compose)

    // Lifecycle
    implementation(androidx.lifecycle.runtime.ktx)
    implementation(androidx.lifecycle.viewmodel.compose)
    implementation(androidx.lifecycle.runtime.compose)

    // Navigation
    implementation(androidx.navigation.compose)

    // Room - FallbackToDestructiveMigration during development per spec
    implementation(androidx.room.runtime)
    implementation(androidx.room.ktx)
    ksp(androidx.room.compiler)

    // DataStore
    implementation(androidx.datastore)
    implementation(androidx.datastore.preferences)

    // Protobuf for Proto DataStore
    implementation(libs.protobuf.javalite)
    implementation(libs.protobuf.kotlin.lite)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // WorkManager + Hilt integration
    implementation(libs.workmanager)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Security for encrypted preferences
    implementation(libs.androidx.security.crypto)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(platform(androidx.compose.bom))
    androidTestImplementation(androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
