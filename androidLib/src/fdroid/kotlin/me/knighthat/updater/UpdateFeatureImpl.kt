package me.knighthat.updater

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import app.kreate.android.themed.common.component.settings.SettingEntrySearch
import app.kreate.android.themed.common.screens.settings.general.updateSection as updateSectionImpl
import app.kreate.android.updater.UpdateFeature

// Public + no-arg constructor: instantiated by reflection from
// UpdateFeatureHolder.init, per the class name declared in this flavor's
// AndroidManifest.xml <meta-data> entry.
class UpdateFeatureImpl : UpdateFeature {
    override fun LazyListScope.updateSection( search: SettingEntrySearch ) = updateSectionImpl( search )
    @Composable override fun UpdateHandler() = me.knighthat.updater.UpdateHandler()
}
