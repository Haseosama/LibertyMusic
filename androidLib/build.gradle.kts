// Classic Android library holding the flavor-specific (github/fdroid/izzy) and
// debug-only code that used to live in :composeApp's androidGithub/androidFdroid/
// androidIzzy/androidDebug source sets. It moved here because AGP 9's
// com.android.kotlin.multiplatform.library (used by :composeApp) is single-variant:
// no build types, no product flavors, no BuildConfig. This module supplies all of
// those, matching :app's flavor/build-type names so Gradle wires each app variant
// to the matching source set here.
//
// Uses AGP 9's built-in Kotlin support instead of the separate kotlin("android")
// plugin (which is deprecated for this purpose) — JVM target comes from
// compileOptions below instead of a separate `kotlin { compilerOptions {...} }`
// block, since that extension needs the now-removed plugin to exist.

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.kreate.android.variant"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            isDefault = true
        }
        create( "uncompressed" ) {}
    }

    // Must mirror :app's dimensions/flavor names exactly so Gradle matches each
    // app variant to the right source set here (only "platform" has real
    // flavor-specific code — arch/env exist so BuildConfig.FLAVOR_arch/FLAVOR_env
    // are generated correctly, which me.knighthat.updater.Updater reads).
    flavorDimensions += listOf( "platform", "arch", "env" )
    productFlavors {
        create("github") {
            dimension = "platform"
            isDefault = true
        }
        create( "fdroid" ) {
            dimension = "platform"
        }
        create( "izzy" ) {
            dimension = "platform"
        }

        create("universal") {
            dimension = "arch"
            isDefault = true
        }
        create("arm32") {
            dimension = "arch"
        }
        create("arm64") {
            dimension = "arch"
        }
        create("x86") {
            dimension = "arch"
        }
        create("x86_64") {
            dimension = "arch"
        }

        create( "nightly" ) {
            dimension = "env"
        }
        create( "prod" ) {
            dimension = "env"
            isDefault = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    // Exposed as `api`: this module's public surface (e.g. Updater.checkForUpdate's
    // Context param, UI composables) is built directly on top of :composeApp's types.
    api( project(":composeApp") )

    implementation( libs.compose.kmp.ui )
    implementation( libs.compose.kmp.foundation )
    implementation( libs.compose.kmp.material3 )
    implementation( libs.compose.kmp.animation )

    implementation( libs.bundles.ktor )
    implementation( libs.kotlinx.serialization.json )

    implementation( libs.koin.core )

    implementation( libs.kermit )

    implementation( libs.androidx.appcompat )
}
