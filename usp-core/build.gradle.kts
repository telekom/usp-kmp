import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.wire)
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
            baseName = "base"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/source/wire") // Avoid errors in Intellij, Gradle works without this line
            dependencies {
                api(libs.kotlinx.datetime)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kermit)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
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

/*
 * Kover configuration (see https://kotlin.github.io/kotlinx-kover/gradle-plugin/)
 */
dependencies {
    kover(projects.uspDatamodel)
    kover(projects.uspMtp)
    kover(projects.uspRecords)
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("de.telekom.usp.types.Generated")
                classes("de.telekom.usp.CommandsKt", "de.telekom.usp.ObjectsKt")
            }
        }
    }
}