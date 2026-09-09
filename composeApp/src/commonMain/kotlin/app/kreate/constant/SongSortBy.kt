package app.kreate.constant

import app.kreate.component.Drawable
import app.kreate.component.TextView
import com.libertymusic.android.composeapp.generated.resources.Res
import com.libertymusic.android.composeapp.generated.resources.album
import com.libertymusic.android.composeapp.generated.resources.artist
import com.libertymusic.android.composeapp.generated.resources.autoplay
import com.libertymusic.android.composeapp.generated.resources.bar_chart
import com.libertymusic.android.composeapp.generated.resources.calendar
import com.libertymusic.android.composeapp.generated.resources.clock_loader
import com.libertymusic.android.composeapp.generated.resources.cross_shuffle
import com.libertymusic.android.composeapp.generated.resources.heart
import com.libertymusic.android.composeapp.generated.resources.hourglass_arrow_up
import com.libertymusic.android.composeapp.generated.resources.sort_album_title
import com.libertymusic.android.composeapp.generated.resources.sort_artist
import com.libertymusic.android.composeapp.generated.resources.sort_date_added
import com.libertymusic.android.composeapp.generated.resources.sort_date_liked
import com.libertymusic.android.composeapp.generated.resources.sort_date_played
import com.libertymusic.android.composeapp.generated.resources.sort_listening_time
import com.libertymusic.android.composeapp.generated.resources.sort_random
import com.libertymusic.android.composeapp.generated.resources.sort_relative_listening_time
import com.libertymusic.android.composeapp.generated.resources.sort_song_duration
import com.libertymusic.android.composeapp.generated.resources.sort_title
import com.libertymusic.android.composeapp.generated.resources.title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource


enum class SongSortBy(
    override val iconId: DrawableResource,
    override val textId: StringResource,
    override val isRandom: Boolean = false
): Drawable, TextView, SortCategory {

    RANDOM(Res.drawable.cross_shuffle, Res.string.sort_random, true),

    TOTAL_PLAY_TIME(Res.drawable.hourglass_arrow_up, Res.string.sort_listening_time),

    RELATIVE_PLAY_TIME(Res.drawable.bar_chart, Res.string.sort_relative_listening_time),

    TITLE(Res.drawable.title, Res.string.sort_title),

    DATE_ADDED(Res.drawable.calendar, Res.string.sort_date_added),

    DATE_PLAYED(Res.drawable.autoplay, Res.string.sort_date_played),

    DATE_LIKED(Res.drawable.heart, Res.string.sort_date_liked),

    ARTIST(Res.drawable.artist, Res.string.sort_artist),

    DURATION(Res.drawable.clock_loader, Res.string.sort_song_duration),

    ALBUM(Res.drawable.album, Res.string.sort_album_title);
}