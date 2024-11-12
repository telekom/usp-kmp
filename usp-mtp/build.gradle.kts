plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kover)
    `maven-publish`
}

kotlin {
    jvm()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        publishLibraryVariants("release", "debug")
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "lib"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":usp-core"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(libs.kmqtt.common)
            implementation(libs.kmqtt.client)
            implementation(libs.mqtt.core)
            implementation(libs.mqtt.client)
            implementation(libs.okio)
            api(libs.kotlinx.datetime)
            api(libs.wire.runtime)
            api(libs.kotlinx.coroutines.core)
            api(libs.ktor.client.core)
            api(libs.ktor.client.cio)
            api(libs.ktor.client.websockets)
            api(libs.ktor.client.logging)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.mock)
            implementation(libs.okio)
        }
    }
}

android {
    namespace = "de.telekom.usp"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}

group = "de.telekom.usp"
version = libs.versions.usp.get()

publishing {
    val repoDirectory: String by rootProject.extra
    repositories {
        maven {
            name = "usp"
            url = uri(repoDirectory)
        }
    }
}