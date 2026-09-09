import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date

// This module is the ONLY one applying `com.android.application` (AGP 9+ forbids
// combining it with kotlin.multiplatform in the same module — see :composeApp).
// It's intentionally a thin shell: applicationId, signing, flavors' version metadata
// and output naming live here; all real code and resources live in :composeApp,
// which this module depends on and whose manifest gets merged into this APK.

val APP_NAME = "Liberty Music"

private fun String.sha256(): String {
    val digest = MessageDigest.getInstance( "SHA-256" )
    val hashBytes = digest.digest( this.toByteArray() )

    return hashBytes.joinToString("") { b -> "%02x".format(b) }
}

// Please DO NOT change this, it's intended to differentiate between
// knighthat/Kreate's build env and others' build env.
// Only official build env has passwords and keystore to sign the APK
// Other build environments can have unsigned version instead
val officialBuildPhrase: String? = System.getenv( "OFFICIAL_BUILD_PASSPHRASE" )
val isOfficialBuildEnv = !officialBuildPhrase.isNullOrBlank() && officialBuildPhrase.sha256() == "b2c778240e03b2005d23899aa02e51de049223a54d549d082e89dc20e51dd545"

plugins {
    alias(libs.plugins.android.application)
}

android {
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

    namespace = "com.libertymusic.android.app"

    defaultConfig {
        applicationId = "com.libertymusic.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
    }

    signingConfigs {
        create( "production" ) {
            storeFile = file("$rootDir/.ignore.d/keystores/production.jks")
            keyAlias = "kreate"
            storePassword = System.getenv( "STORE_PASSWORD" )
            keyPassword = System.getenv( "KEY_PASSWORD" )
        }
        create( "nightly" ) {
            storeFile = file("$rootDir/.ignore.d/keystores/nightly.jks")
            keyAlias = "nightly"
            storePassword = System.getenv( "STORE_PASSWORD" )
            keyPassword = System.getenv( "KEY_PASSWORD" )
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appName"] = "$APP_NAME-debug"
        }

        release {
            isDefault = true

            // Package optimization
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "$rootDir/composeApp/proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        create( "uncompressed" ) {
            // App's properties
            versionNameSuffix = "-f"
        }
    }

    flavorDimensions += listOf( "platform", "arch", "env" )
    //noinspection ChromeOsAbiSupport
    productFlavors {
        val vCode = libs.versions.versionCode.get().toInt()

        //<editor-fold desc="Platforms">
        create("github") {
            dimension = "platform"

            isDefault = true
        }
        create( "fdroid" ) {
            dimension = "platform"

            // App's properties
            versionNameSuffix = "-fdroid"
        }
        create( "izzy" ) {
            dimension = "platform"

            // App's properties
            versionNameSuffix = "-izzy"
        }
        //</editor-fold>
        //<editor-fold desc="Architectures">
        create("universal") {
            dimension = "arch"

            isDefault = true
        }
        create("arm32") {
            dimension = "arch"

            // App's properties
            versionCode = (vCode * 10) + 1

            // Build architecture
            ndk { abiFilters += "armeabi-v7a" }
        }
        create("arm64") {
            dimension = "arch"

            // App's properties
            versionCode = (vCode * 10) + 2

            // Build architecture
            ndk { abiFilters += "arm64-v8a" }
        }
        create("x86") {
            dimension = "arch"

            // App's properties
            versionCode = (vCode * 10) + 3

            // Build architecture
            ndk { abiFilters += "x86" }
        }
        create("x86_64") {
            dimension = "arch"

            // App's properties
            versionCode = (vCode * 10) + 4

            // Build architecture
            ndk { abiFilters += "x86_64" }
        }
        //</editor-fold>
        //<editor-fold desc="Environment">
        create( "nightly" ) {
            dimension = "env"

            // Signing config
            signingConfig = signingConfigs.getByName( "nightly" )

            val longFormat = SimpleDateFormat("yyyy.MM.dd")
            val shortFormat = SimpleDateFormat("yyMMdd")

            // App's properties
            applicationIdSuffix = ".nightly"
            versionName = longFormat.format (Date() )
            manifestPlaceholders["appName"] = "Nightly"
            // The idea is to combine build date and current version code together
            versionCode = "${shortFormat.format( Date() )}$vCode".toInt()
        }
        create( "prod" ) {
            dimension = "env"

            isDefault = true

            if( isOfficialBuildEnv )
                // Singing config
                signingConfig = signingConfigs.getByName( "production" )

            // App's properties
            versionName = libs.versions.versionName.get()
            manifestPlaceholders["appName"] = APP_NAME
            versionCode = vCode
        }
        //</editor-fold>
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

// Migrated from the obsolete applicationVariants.all{} (BaseVariantOutputImpl) API,
// which AGP 9 deprecates in favor of this Variant API.
androidComponents {
    onVariants { variant ->
        val flavorName = variant.flavorName.orEmpty()
        val buildTypeName = variant.buildType.orEmpty()

        val suffix = if( "izzy" in flavorName )
            "izzy"
        else if( "Nightly" in flavorName )
            "nightly"
        // The next 4 conditions set the APK name to the architecture
        // if it's intended for release build
        else if( "Arm64" in flavorName && buildTypeName == "release" )
            "arm64-v8a"
        else if( "Arm32" in flavorName && buildTypeName == "release" )
            "armeabi-v7a"
        else if( "X86_64" in flavorName && buildTypeName == "release" )
            "x86_64"
        else if( "X86" in flavorName && buildTypeName == "release" )
            "x86"
        // Or just append build type at the end of the APK file name
        else
            buildTypeName

        variant.outputs.forEach { output ->
            output.outputFileName.set( "${APP_NAME.replace(" ", "")}-${suffix}.apk" )
        }
    }
}

dependencies {
    // :androidLib carries the flavor-specific (github/fdroid/izzy) and debug-only
    // code, and itself depends on :composeApp — both end up on the final classpath.
    implementation( project(":androidLib") )

    coreLibraryDesugaring(libs.desugaring.nio)
}
// NOTE: copyReleaseNote moved to :composeApp — it writes into composeApp's own
// res/raw, and Gradle needs that producer/consumer relationship declared on the
// same project as the resource-merging task that reads it (packageAndroidMainResources).
