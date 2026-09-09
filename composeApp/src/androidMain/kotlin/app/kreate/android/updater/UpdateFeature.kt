package app.kreate.android.updater

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import app.kreate.android.themed.common.component.settings.SettingEntrySearch

/**
 * The GitHub-updater feature is flavor-specific (github/fdroid/izzy) and now lives
 * in :androidLib, a separate Gradle module that depends on :composeApp — not the
 * other way around. :composeApp's own screens (GeneralSettings, AppNavigation)
 * can't reference it directly, so they go through this holder instead.
 *
 * :androidLib's manifest (one per flavor) declares which class implements this via
 * a <meta-data> entry keyed [UpdateFeatureHolder.META_DATA_KEY] — [UpdateFeatureHolder.init]
 * reads it and instantiates that class by reflection. This indirection (instead of
 * :androidLib calling into :composeApp directly) is required because a plain class/
 * interface reference across this dependency boundary isn't possible: :androidLib
 * depends on :composeApp, not the other way around.
 */
interface UpdateFeature {
    fun LazyListScope.updateSection( search: SettingEntrySearch )
    @Composable fun UpdateHandler()
}

object UpdateFeatureHolder {

    const val META_DATA_KEY = "app.kreate.android.updater.UPDATE_FEATURE_IMPL"

    var feature: UpdateFeature? = null
        private set

    /** Called once, early in `MainApplication.onCreate()`. */
    fun init( context: Context ) {
        val className = context.packageManager
                                .getApplicationInfo( context.packageName, PackageManager.GET_META_DATA )
                                .metaData
                                ?.getString( META_DATA_KEY )
                         ?: return

        feature = Class.forName( className )
                        .getDeclaredConstructor()
                        .newInstance() as UpdateFeature
    }
}
