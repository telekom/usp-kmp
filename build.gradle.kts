/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.wire) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.dependencyLicenseReport)
}

// Local maven repository to publish artifacts to
val repoDirectory by extra { "${project.rootDir}/repo" }