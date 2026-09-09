package app.kreate.android

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Replaces the old `BuildConfig`-based lookups. `:composeApp` is a single-variant
 * Kotlin Multiplatform library (AGP 9's `com.android.kotlin.multiplatform.library`
 * doesn't support BuildConfig/flavors/build types), so anything that used to vary
 * per build variant (debug flag, version name, applicationId) is now read from the
 * OS at runtime instead of from a generated compile-time constant — the one
 * exception is [NAME], which was never variant-dependent to begin with.
 *
 * [init] must be called once, as early as possible (in `MainApplication.onCreate`),
 * before any of the other properties are read.
 */
object AppInfo {

    const val NAME = "Liberty Music"

    private lateinit var appContext: Context

    fun init( context: Context ) {
        appContext = context.applicationContext
    }

    val isDebug: Boolean by lazy {
        ( appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE ) != 0
    }

    val versionName: String by lazy {
        appContext.packageManager.getPackageInfo( appContext.packageName, 0 ).versionName ?: ""
    }

    val applicationId: String by lazy { appContext.packageName }
}
