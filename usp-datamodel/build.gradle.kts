/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mockery)
    alias(libs.plugins.kover)
    `maven-publish`
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    compilerOptions {
        freeCompilerArgs.addAll(
            // Suppress warnings for Beta expect/actual classes and interfaces (KT-61573)
            "-Xexpect-actual-classes",
            // Ensure data class copy() matches non-public primary constructor visibility (KT-11914)
            "-Xconsistent-data-class-copy-visibility"
        )
    }

    jvm()
    android {
        namespace = "de.telekom.usp"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(
                JvmTarget.JVM_17
            )
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
                implementation(projects.uspCore)
                api(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.json.okio)
                implementation(libs.kermit)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.okio)
                implementation(libs.okio.fakefilesystem)
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