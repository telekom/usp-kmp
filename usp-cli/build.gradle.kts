/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("application")
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass = "de.telekom.usp.cli.MainKt"
    applicationName = "uspctl"
}

distributions {
    main {
        distributionBaseName = "usp-cli"
    }
}

dependencies {
    implementation(projects.uspCore)
    implementation(projects.uspRecords)
    implementation(projects.uspMtp)
    implementation(projects.uspExchange)
    implementation(libs.clikt)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.json.okio)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)
}