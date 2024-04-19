@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("application")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
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
    implementation(project(":core"))
    implementation(project(":records"))
    implementation(project(":mtp"))
    implementation(project(":lib"))
    implementation(libs.clikt)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.json.okio)
    implementation(libs.slj4j.api)
    implementation(libs.slj4j.simple)
}