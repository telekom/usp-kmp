/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.jvm).apply(false)
    alias(libs.plugins.kover).apply(false)
    id("com.github.jk1.dependency-license-report") version "3.1.4"
}

// Local maven repository to publish artifacts to
val repoDirectory by extra { "${project.rootDir}/repo" }