/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.wire)
    alias(libs.plugins.kover)
    `maven-publish`
}

kotlin {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
        publishLibraryVariants("release", "debug")
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
                implementation(project(":usp-core"))
                api(libs.kotlinx.datetime)
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
            }
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

wire {
    kotlin {
        // `suspending` to generate coroutines APIs that require a Kotlin coroutines context.
        // `blocking` to generate blocking APIs callable by Java and Kotlin.
        rpcCallStyle = "suspending"
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