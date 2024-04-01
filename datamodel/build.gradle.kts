plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvm()
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
                implementation(project(":core"))
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kermit)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.moshi.kotlin)
                implementation(libs.wire.moshi)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.moshi.kotlin)
                implementation(libs.wire.moshi)
                implementation(libs.kotlinpoet)
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

