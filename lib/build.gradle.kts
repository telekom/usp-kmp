plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
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
            implementation(project(":core"))
            implementation(project(":records"))
            implementation(project(":mtp"))
            implementation(libs.kotlinx.datetime)
            implementation(libs.kermit)
            api(libs.wire.runtime)
            api(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "de.telekom.usp"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "de.telekom.usp"
            artifactId = "usp-core"
            version = "1.0.0"

//            afterEvaluate {
//                from(components["release"])
//            }
        }
    }
    repositories {
        maven {
            name = "local-repo"
            url = uri("${project.rootDir}/repo")
        }
    }
}