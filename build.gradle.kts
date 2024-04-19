plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.org.jetbrains.kotlin.jvm).apply(false)
}

// Local maven repository to publish artifacts to
val repoDirectory by extra { "${project.rootDir}/repo" }