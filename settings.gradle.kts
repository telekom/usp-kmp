enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "USP-Library"
include(":usp-core")
include(":usp-records")
include(":usp-cli")
include(":usp-mtp")
include(":usp-datamodel")
include(":usp-builder")
