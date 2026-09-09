package app.kreate.constant

import app.kreate.component.Drawable
import app.kreate.component.TextView
import com.libertymusic.android.composeapp.generated.resources.Res
import com.libertymusic.android.composeapp.generated.resources.artist
import com.libertymusic.android.composeapp.generated.resources.bookmark_stacks
import com.libertymusic.android.composeapp.generated.resources.calendar
import com.libertymusic.android.composeapp.generated.resources.clock_loader
import com.libertymusic.android.composeapp.generated.resources.cross_shuffle
import com.libertymusic.android.composeapp.generated.resources.sort_album_year
import com.libertymusic.android.composeapp.generated.resources.sort_artist
import com.libertymusic.android.composeapp.generated.resources.sort_date_added
import com.libertymusic.android.composeapp.generated.resources.sort_random
import com.libertymusic.android.composeapp.generated.resources.sort_songs_count
import com.libertymusic.android.composeapp.generated.resources.sort_title
import com.libertymusic.android.composeapp.generated.resources.sort_total_duration
import com.libertymusic.android.composeapp.generated.resources.title
import com.libertymusic.android.composeapp.generated.resources.year
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource


enum class AlbumSortBy(
    override val iconId: DrawableResource,
    override val textId: StringResource,
    override val isRandom: Boolean = false
): Drawable, TextView, SortCategory {

    RANDOM(Res.drawable.cross_shuffle, Res.string.sort_random, true),

    TITLE(Res.drawable.title, Res.string.sort_title),

    YEAR(Res.drawable.year, Res.string.sort_album_year),

    DATE_ADDED(Res.drawable.calendar, Res.string.sort_date_added),

    ARTIST(Res.drawable.artist, Res.string.sort_artist),

    SONGS_COUNT(Res.drawable.bookmark_stacks, Res.string.sort_songs_count),

    TOTAL_DURATION(Res.drawable.clock_loader, Res.string.sort_total_duration);
}