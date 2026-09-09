plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

// Converted from plain kotlin("jvm") to Kotlin Multiplatform, mirroring the exact
// target shape of extensions/discord (androidLibrary + jvm) — the one project()
// dependency that has never failed to resolve for :composeApp's commonMain/
// commonTest/androidMain/jvmMain. A plain kotlin("jvm") project (no multiplatform
// metadata) consistently failed to resolve there instead. Source directories kept
// as-is (src/main, src/test) via explicit srcDir instead of moving files to
// src/commonMain.
kotlin {
    // `androidLibrary` is deprecated (kotlinc warns) in favor of `android`.
    android {
        namespace = "it.fast4x.innertube"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        withHostTest {}
    }

    jvm()

    // Re-applied explicitly — see the comment in :composeApp's build.gradle.kts.
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation( libs.bundles.ktor )
            implementation(libs.okhttp3.logging.interceptor)
            implementation(libs.ksoup.html)
            implementation(libs.ksoup.entities)
        }
    }
}
