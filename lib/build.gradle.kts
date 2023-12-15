plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
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
            implementation(project(":base"))
            implementation(project(":mtp"))
            api(libs.wire.runtime)
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
