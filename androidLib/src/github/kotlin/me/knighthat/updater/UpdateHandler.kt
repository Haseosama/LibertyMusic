package me.knighthat.updater

import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import app.kreate.android.AppInfo
import app.kreate.android.Preferences
import it.fast4x.rimusic.enums.CheckUpdateState
import java.io.File

@Composable
fun UpdateHandler() {
    val context = LocalContext.current

    DownloadAndInstallDialog.Render()
    NewUpdatePrompt.Render()

    val check4UpdateState by Preferences.CHECK_UPDATE
    LaunchedEffect( check4UpdateState ) {
        if( check4UpdateState != CheckUpdateState.DISABLED )
            Updater.checkForUpdate( context )
    }

    LaunchedEffect( Unit ) {
        val filename = Updater.getFileName()
        val apkFile = File(
            context.getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ),
            filename
        )
        if( apkFile.exists() && !AppInfo.isDebug )
            apkFile.delete()
    }
}