// Vespa - Root Build Configuration
// Java 17 is MANDATORY globally - not optional

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.protobuf) apply false
}

// Global Java 17 configuration - MANDATORY per spec
// This ensures ALL subprojects use Java 17 regardless of their individual configs
subprojects {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            // Enable all warnings as errors for production
            // Use -Xwarning to suppress specific warnings if needed
            allWarningsAsErrors = false
        }
    }
}

// Apply Android configuration to all subprojects that use it
subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")) {
            extensions.configure<com.android.build.gradle.BaseExtension> {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
    }
}

// Configure protobuf for all subprojects
subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.google.protobuf")) {
            extensions.configure<com.google.protobuf.gradle.ProtobufExtension> {
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
        }
    }
}
