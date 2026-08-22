plugins {
    // Applied in module build.gradle.kts
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kapt) apply false
}


