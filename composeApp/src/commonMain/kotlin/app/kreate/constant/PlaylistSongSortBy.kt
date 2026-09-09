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
import com.libertymusic.android.composeapp.generated.resources.position
import com.libertymusic.android.composeapp.generated.resources.recent_actors
import com.libertymusic.android.composeapp.generated.resources.sort_album_and_artist
import com.libertymusic.android.composeapp.generated.resources.sort_album_title
import com.libertymusic.android.composeapp.generated.resources.sort_album_year
import com.libertymusic.android.composeapp.generated.resources.sort_artist
import com.libertymusic.android.composeapp.generated.resources.sort_date_added
import com.libertymusic.android.composeapp.generated.resources.sort_date_liked
import com.libertymusic.android.composeapp.generated.resources.sort_date_played
import com.libertymusic.android.composeapp.generated.resources.sort_listening_time
import com.libertymusic.android.composeapp.generated.resources.sort_position
import com.libertymusic.android.composeapp.generated.resources.sort_random
import com.libertymusic.android.composeapp.generated.resources.sort_relative_listening_time
import com.libertymusic.android.composeapp.generated.resources.sort_song_duration
import com.libertymusic.android.composeapp.generated.resources.sort_title
import com.libertymusic.android.composeapp.generated.resources.title
import com.libertymusic.android.composeapp.generated.resources.year
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource


enum class PlaylistSongSortBy(
    override val iconId: DrawableResource,
    override val textId: StringResource,
    override val isRandom: Boolean = false
): Drawable, TextView, SortCategory {

    RANDOM(Res.drawable.cross_shuffle, Res.string.sort_random, true),

    ALBUM(Res.drawable.album, Res.string.sort_album_title),

    ALBUM_YEAR(Res.drawable.year, Res.string.sort_album_year),

    ARTIST(Res.drawable.artist, Res.string.sort_artist),

    ARTIST_AND_ALBUM(Res.drawable.recent_actors, Res.string.sort_album_and_artist),

    DATE_PLAYED(Res.drawable.autoplay, Res.string.sort_date_played),

    TOTAL_PLAY_TIME(Res.drawable.hourglass_arrow_up, Res.string.sort_listening_time),

    RELATIVE_PLAY_TIME(Res.drawable.bar_chart, Res.string.sort_relative_listening_time),

    POSITION(Res.drawable.position, Res.string.sort_position),

    TITLE(Res.drawable.title, Res.string.sort_title),

    DURATION(Res.drawable.clock_loader, Res.string.sort_song_duration),

    DATE_LIKED(Res.drawable.heart, Res.string.sort_date_liked),

    DATE_ADDED(Res.drawable.calendar, Res.string.sort_date_added);
}