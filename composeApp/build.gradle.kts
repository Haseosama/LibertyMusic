import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.ExcludeTransitiveDependenciesFilter
import com.github.jk1.license.render.JsonReportRenderer
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val APP_NAME = "Liberty Music"

plugins {
    // Multiplatform
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)

    // Android
    // NOTE: AGP 9+ forbids combining kotlin.multiplatform with EITHER com.android.application
    // OR com.android.library in the same module — only this special plugin is allowed here.
    // It's single-variant: no build types, no product flavors, no BuildConfig (see AppInfo.kt
    // for the BuildConfig replacement). Flavor/build-type-specific code lives in :androidLib
    // instead (a classic com.android.library, which still supports all of that), and
    // applicationId/signing/flavor version metadata live in the thin :app module.
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.room)

    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias( libs.plugins.license.report )
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

kotlin {
    // NOTE: `androidLibrary` is deprecated in favor of `android` (kotlinc warns on it) —
    // switched here, was worth trying since it may also be why commonTest wasn't
    // wiring to jvmTest/androidHostTest correctly.
    android {
        namespace = "app.kreate.android"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        // This module still has its own res/ (strings, drawables...) consumed by
        // code that stays here (DI/core layer) — needs the R class generated.
        androidResources.enable = true

        withHostTest {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    compilerOptions {
        freeCompilerArgs.add( "-Xexpect-actual-classes" )
    }

    jvm()

    // Re-applied explicitly: the default hierarchy template's automatic pass seems to
    // run before withHostTest{} above creates the androidHostTest source set, so it
    // never gets connected to commonTest ("Unused Kotlin Source Set" warning). Calling
    // this again now that androidHostTest/jvmTest both exist should pick it up.
    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings {
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }

        jvmMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.desktop.currentOs)

            implementation(libs.material.icons.desktop.ext)
            implementation(libs.vlcj)
        }
        androidMain.dependencies {
            implementation( projects.metrolistInnertube )

            // Android-only: no jvmMain/desktop code references it, and it has real
            // Java sources, so it only declares an androidLibrary target.
            implementation( projects.innertube )

            implementation(libs.kotlinx.coroutines.guava)
            implementation(libs.nanojson)
            implementation(libs.androidx.webkit)

            implementation( libs.androidx.glance.widgets )
            implementation( libs.androidx.constraintlayout )

            implementation( libs.androidx.appcompat )
            implementation( libs.androidx.appcompat.resources )
            implementation( libs.androidx.palette )

            implementation( libs.monetcompat )
            implementation(libs.androidmaterial)

            // Player implementations
            implementation( libs.media3.exoplayer )
            implementation(libs.media3.session)
            implementation( libs.media3.datasource.okhttp )
            implementation( libs.androidyoutubeplayer )

            implementation( libs.toasty )

            // Dependency injection
            implementation( libs.koin.android )

            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.process)
        }
        getByName("androidHostTest") {
            dependencies {
                implementation( libs.junit4 )
                implementation( libs.robolectric )
                implementation( libs.androidx.test )
            }
        }
        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(projects.oldtube)
            implementation(projects.kugou)
            implementation(projects.lrclib)
            implementation( projects.discord )

            // Room KMP
            implementation( libs.room.runtime )
            implementation( libs.sqlite.bundled )

            implementation(libs.navigation.kmp)

            //coil3 mp
            implementation( libs.coil3.compose )
            implementation( libs.coil3.network.ktor )

            implementation(libs.translator)

            implementation( libs.bundles.compose.kmp )

            implementation ( libs.hypnoticcanvas )
            implementation ( libs.hypnoticcanvas.shaders )

            implementation( libs.kotlin.csv )

            implementation( libs.bundles.ktor )
            implementation( libs.okhttp3.logging.interceptor )
            implementation( libs.okhttp3.dns.over.https )

            implementation( libs.math3 )

            implementation( libs.material.icons.kmp )

            // Dependency injection
            implementation( libs.koin.core )
            implementation( libs.koin.navigation )

            // Logging
            implementation( libs.kermit )
            implementation( libs.kermit.io )
        }
        commonTest.dependencies {
            implementation( libs.kotlin.test )
        }
    }
}

// NOTE: no top-level `android {}` block anymore — namespace/compileSdk/minSdk/jvmTarget
// are configured above, inside `kotlin { android { ... } }`. The new plugin doesn't
// support buildTypes/productFlavors/BuildConfig/testOptions/compileOptions the way the
// classic com.android.library/application plugins did; those concerns now live in
// :androidLib (flavors/build types/BuildConfig) and :app (application-only bits).

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

compose.desktop {
    application {

        mainClass = "MainKt"

        //conveyor
        version = "0.0.1"
        group = "com.libertymusic.android"

        //jpackage
        nativeDistributions {
            //conveyor
            vendor = "LibertyMusic.DesktopApp"
            description = "Liberty Music Desktop Music Player"

            targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "LibertyMusic.DesktopApp"
            packageVersion = "0.0.1"
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Room
    add( "kspAndroid", libs.room.compiler )
    add( "kspJvm", libs.room.compiler )

    coreLibraryDesugaring(libs.desugaring.nio)
}

// Use `gradlew dependencies` to get report in composeApp/build/reports/dependency-license
licenseReport {
    // Select projects to examine for dependencies.
    // Defaults to current project and all its subprojects
    projects = arrayOf( project )

    // :composeApp is single-variant now (no flavors/build types), so there's no
    // "githubUniversalProdUncompressedRuntimeClasspath"-style configuration anymore.
    // Falls back to the plugin's default ('runtimeClasspath').

    // Don't include artifacts of project's own group into the report
    excludeOwnGroup = true

    // Don't exclude bom dependencies.
    // If set to true, then all BOMs will be excluded from the report
    excludeBoms = true

    // Set custom report renderer, implementing ReportRenderer.
    // Yes, you can write your own to support any format necessary.
    renderers = arrayOf( JsonReportRenderer() )

    filters = arrayOf<DependencyFilter>( ExcludeTransitiveDependenciesFilter() )
}

val copyReleaseNote = tasks.register<Copy>("copyReleaseNote" ) {
    description = "Copy release note that matches current versionCode to raw folder"
    group = JavaBasePlugin.BUILD_DEPENDENTS_TASK_NAME

    from( "$rootDir/fastlane/metadata/android/en-US/changelogs" )

    val fileName = "${libs.versions.versionCode.get()}.txt"
    setIncludes( listOf( fileName ) )

    into( "$projectDir/src/androidMain/res/raw" )

    rename {
        if( it == fileName ) "release_notes.txt" else it
    }
}

// Declared explicitly: packageAndroidMainResources reads composeApp/src/androidMain/res,
// which copyReleaseNote writes into — Gradle needs this producer/consumer edge spelled
// out or task ordering isn't guaranteed.
tasks.matching { it.name == "packageAndroidMainResources" }.configureEach {
    dependsOn( copyReleaseNote )
}
