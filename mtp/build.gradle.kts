plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.wire)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "proto"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":base"))
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = "de.telekom.usp.proto"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

wire {
    kotlin {
        // `suspending` to generate coroutines APIs that require a Kotlin coroutines context.
        // `blocking` to generate blocking APIs callable by Java and Kotlin.
        rpcCallStyle = "suspending"
    }
}