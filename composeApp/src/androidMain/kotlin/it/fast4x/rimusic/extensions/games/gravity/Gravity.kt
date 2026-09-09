package it.fast4x.rimusic.extensions.games.gravity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

// A parody of Google's "Gravity" easter egg: a fake mini homepage whose elements
// suddenly lose gravity and tumble to the bottom of the screen.
private const val GRAVITY = 0.7f
private const val BOUNCE_DAMPING = 0.45f
private const val FRICTION = 0.92f
private const val LAND_VELOCITY_THRESHOLD = 60f
private const val FRAME_DELAY_MS = 16L
private const val FALL_START_DELAY_MS = 1000L
private const val BOTTOM_PADDING_DP = 32

private data class FallItem(
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val rotation: Float = 0f,
    val angularVelocity: Float = 0f,
    val landed: Boolean = false,
    val widthPx: Float,
    val heightPx: Float
)

@Composable
fun GravityEasterEgg() {
    val density = LocalDensity.current

    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    var phase by remember { mutableStateOf("idle") } // idle, falling, settled
    var gameId by remember { mutableIntStateOf(0) }
    val items = remember { mutableStateListOf<FallItem>() }

    fun buildItems() {
        items.clear()
        with( density ) {
            val logoW = 190.dp.toPx(); val logoH = 64.dp.toPx()
            val subW = 90.dp.toPx(); val subH = 30.dp.toPx()
            val barW = canvasWidth * 0.82f; val barH = 56.dp.toPx()
            val btnW = 170.dp.toPx(); val btnH = 48.dp.toPx()
            val footW = 260.dp.toPx(); val footH = 26.dp.toPx()
            val gap = 16.dp.toPx()

            val logoY = canvasHeight * 0.16f
            val subY = logoY + logoH + 8.dp.toPx()
            val barY = subY + subH + 40.dp.toPx()
            val rowY = barY + barH + 24.dp.toPx()

            items.add( FallItem( x = (canvasWidth - logoW) / 2, y = logoY, widthPx = logoW, heightPx = logoH ) )
            items.add( FallItem( x = (canvasWidth - subW) / 2, y = subY, widthPx = subW, heightPx = subH ) )
            items.add( FallItem( x = (canvasWidth - barW) / 2, y = barY, widthPx = barW, heightPx = barH ) )
            items.add( FallItem( x = canvasWidth / 2 - btnW - gap / 2, y = rowY, widthPx = btnW, heightPx = btnH ) )
            items.add( FallItem( x = canvasWidth / 2 + gap / 2, y = rowY, widthPx = btnW, heightPx = btnH ) )
            items.add( FallItem( x = (canvasWidth - footW) / 2, y = canvasHeight * 0.9f, widthPx = footW, heightPx = footH ) )
        }
    }

    fun triggerFall() {
        for ( i in items.indices ) {
            val item = items[i]
            items[i] = item.copy(
                vx = Random.nextFloat() * 6f - 3f,
                vy = -Random.nextFloat() * 4f,
                angularVelocity = Random.nextFloat() * 10f - 5f,
                landed = false
            )
        }
        phase = "falling"
    }

    fun resetHomepage() {
        gameId++
    }

    LaunchedEffect( gameId, canvasWidth, canvasHeight ) {
        if ( canvasWidth <= 0f || canvasHeight <= 0f ) return@LaunchedEffect

        buildItems()
        phase = "idle"
        delay( FALL_START_DELAY_MS )
        triggerFall()
    }

    LaunchedEffect( phase, gameId ) {
        if ( phase != "falling" ) return@LaunchedEffect

        val floorY = canvasHeight - with( density ) { BOTTOM_PADDING_DP.dp.toPx() }
        while ( phase == "falling" ) {
            delay( FRAME_DELAY_MS )

            var allLanded = true
            for ( i in items.indices ) {
                val item = items[i]
                if ( item.landed ) continue
                allLanded = false

                var vy = item.vy + GRAVITY
                var y = item.y + vy
                val x = (item.x + item.vx).coerceIn( 0f, (canvasWidth - item.widthPx).coerceAtLeast( 0f ) )
                val rotation = item.rotation + item.angularVelocity
                var vx = item.vx * FRICTION
                var angularVelocity = item.angularVelocity * FRICTION
                var landed = false

                val bottom = y + item.heightPx
                if ( bottom >= floorY ) {
                    y = floorY - item.heightPx
                    if ( abs( vy ) > LAND_VELOCITY_THRESHOLD ) {
                        vy = -vy * BOUNCE_DAMPING
                    } else {
                        vy = 0f
                        landed = true
                    }
                }

                items[i] = item.copy( x = x, y = y, vx = vx, vy = vy, rotation = rotation, angularVelocity = angularVelocity, landed = landed )
            }
            if ( allLanded ) phase = "settled"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background( Color(0xFF202124) )
            .onSizeChanged {
                canvasWidth = it.width.toFloat()
                canvasHeight = it.height.toFloat()
            }
            .pointerInput( Unit ) {
                detectTapGestures( onTap = { if ( phase == "settled" ) triggerFall() } )
            }
    ) {
        items.forEachIndexed { index, item ->
            val wDp = with( density ) { item.widthPx.toDp() }
            val hDp = with( density ) { item.heightPx.toDp() }

            Box(
                modifier = Modifier
                    .offset { IntOffset( item.x.roundToInt(), item.y.roundToInt() ) }
                    .graphicsLayer { rotationZ = item.rotation }
            ) {
                FallItemContent( index, wDp, hDp )
            }
        }

        if ( phase == "settled" ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                                   .align( Alignment.TopCenter )
                                   .padding( top = 24.dp, start = 24.dp, end = 24.dp ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy( 8.dp )
            ) {
                Text(
                    text = "Oups… on dirait que la gravité vient de lâcher.",
                    color = Color.White,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Button( onClick = { resetHomepage() } ) {
                    Text( "Tout remettre debout" )
                }
                Text(
                    text = "(ou tape n'importe où pour secouer encore)",
                    color = Color( 0xFF9AA0A6 ),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FallItemContent( index: Int, w: Dp, h: Dp ) {
    when ( index ) {
        0 -> Box( modifier = Modifier.size( w, h ), contentAlignment = Alignment.Center ) {
            Text( text = googleStyleLogo(), fontSize = 32.sp, fontWeight = FontWeight.Bold )
        }
        1 -> Box( modifier = Modifier.size( w, h ), contentAlignment = Alignment.Center ) {
            Text( text = "Music", color = Color( 0xFF9AA0A6 ), fontSize = 18.sp )
        }
        2 -> Box(
            modifier = Modifier
                .size( w, h )
                .clip( RoundedCornerShape( 28.dp ) )
                .background( Color(0xFF303134) )
                .border( 1.dp, Color(0xFF5F6368), RoundedCornerShape( 28.dp ) ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding( horizontal = 20.dp )
            ) {
                Text( "🔍", fontSize = 18.sp )
                Box( modifier = Modifier.width( 12.dp ) )
                Text( "Rechercher un titre, un artiste…", color = Color(0xFF9AA0A6), fontSize = 14.sp )
            }
        }
        3 -> Button(
            onClick = {},
            modifier = Modifier.size( w, h ),
            colors = ButtonDefaults.buttonColors( containerColor = Color(0xFF303134), contentColor = Color(0xFFE8EAED) )
        ) {
            Text( "Recherche Liberty", fontSize = 13.sp )
        }
        4 -> Button(
            onClick = {},
            modifier = Modifier.size( w, h ),
            colors = ButtonDefaults.buttonColors( containerColor = Color(0xFF303134), contentColor = Color(0xFFE8EAED) )
        ) {
            Text( "J'ai de la chance", fontSize = 13.sp )
        }
        5 -> Box( modifier = Modifier.size( w, h ), contentAlignment = Alignment.Center ) {
            Text( "🎧 Propulsé par beaucoup de café", color = Color(0xFF9AA0A6), fontSize = 12.sp )
        }
    }
}

private fun googleStyleLogo() = buildAnnotatedString {
    val text = "Liberty"
    val colors = listOf( 0xFF4285F4, 0xFFEA4335, 0xFFFBBC05, 0xFF4285F4, 0xFF34A853, 0xFFEA4335, 0xFF4285F4 )
    text.forEachIndexed { i, c ->
        withStyle( SpanStyle( color = Color( colors[i % colors.size] ) ) ) {
            append( c )
        }
    }
}
