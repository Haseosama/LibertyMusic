package it.fast4x.rimusic.extensions.games.donkeykong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kreate.android.R
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

// 5 levels, 0 = top (Donkey Kong + Pauline), 4 = bottom (Jumpman starts here).
private const val LEVEL_COUNT = 5
private const val PLAYER_SPEED = 14f
private const val LADDER_SPEED = 10f
private const val BARREL_SPEED = 6f
private const val COLLISION_DISTANCE = 40f
private const val LADDER_HALF_WIDTH = 40f
private const val JUMP_DURATION_TICKS = 18
private const val FRAME_DELAY_MS = 16L
private const val BARREL_SPAWN_TICKS = 90

private data class Barrel( var x: Float, var levelIndex: Int, var direction: Int )

@Composable
fun DonkeyKongGame() {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    var playerX by remember { mutableFloatStateOf(0f) }
    var playerLevel by remember { mutableIntStateOf( LEVEL_COUNT - 1 ) }
    // Fractional position between playerLevel and playerLevel-1 while climbing a ladder (0=on
    // playerLevel, 1=fully on the level above). 0 while not climbing.
    var climbProgress by remember { mutableFloatStateOf(0f) }
    var jumpTicksLeft by remember { mutableIntStateOf(0) }

    var barrels by remember { mutableStateOf(listOf<Barrel>()) }
    var ticksSinceSpawn by remember { mutableIntStateOf(0) }

    var isGameOver by remember { mutableStateOf(false) }
    var hasWon by remember { mutableStateOf(false) }
    var gameId by remember { mutableIntStateOf(0) }

    var moveDir by remember { mutableIntStateOf(0) }   // -1 left, 0 idle, 1 right
    var climbDir by remember { mutableIntStateOf(0) }  // -1 down, 0 idle, 1 up

    fun levelY( level: Int, height: Float ): Float {
        val topMargin = height * 0.12f
        val bottomMargin = height * 0.92f
        val step = (bottomMargin - topMargin) / (LEVEL_COUNT - 1)
        return topMargin + step * level
    }

    // One ladder between each pair of adjacent levels, alternating side of the screen —
    // mirrors the zigzag layout of the original game's first stage.
    fun ladderX( levelBelow: Int, width: Float ): Float =
        if ( levelBelow % 2 == 0 ) width * 0.75f else width * 0.25f

    fun reset() {
        playerX = canvasWidth * 0.5f
        playerLevel = LEVEL_COUNT - 1
        climbProgress = 0f
        jumpTicksLeft = 0
        barrels = emptyList()
        ticksSinceSpawn = 0
        isGameOver = false
        hasWon = false
        moveDir = 0
        climbDir = 0
        gameId++
    }

    LaunchedEffect( gameId, canvasWidth, canvasHeight ) {
        if ( canvasWidth <= 0f || canvasHeight <= 0f ) return@LaunchedEffect
        if ( playerX == 0f ) playerX = canvasWidth * 0.5f

        while ( !isGameOver && !hasWon ) {
            delay( FRAME_DELAY_MS )

            if ( jumpTicksLeft > 0 ) jumpTicksLeft--

            // Horizontal movement (only while not mid-climb)
            if ( climbProgress == 0f && moveDir != 0 ) {
                playerX = (playerX + moveDir * PLAYER_SPEED).coerceIn( 20f, canvasWidth - 20f )
            }

            // Ladder climbing: only when standing within range of the ladder connecting
            // playerLevel and the level above it (playerLevel - 1).
            if ( playerLevel > 0 ) {
                val ladderXPos = ladderX( playerLevel - 1, canvasWidth )
                val onLadder = abs( playerX - ladderXPos ) < LADDER_HALF_WIDTH
                if ( onLadder && climbDir != 0 ) {
                    climbProgress = (climbProgress + climbDir * (LADDER_SPEED / 100f)).coerceIn( 0f, 1f )
                    playerX = ladderXPos
                }
            }
            if ( climbProgress >= 1f ) {
                playerLevel--
                climbProgress = 0f
                if ( playerLevel == 0 ) hasWon = true
            } else if ( climbProgress <= 0f ) {
                climbProgress = 0f
            }

            // Barrel spawning at the top level, rolling down toward Jumpman.
            ticksSinceSpawn++
            if ( ticksSinceSpawn >= BARREL_SPAWN_TICKS ) {
                ticksSinceSpawn = 0
                barrels = barrels + Barrel( x = canvasWidth * 0.15f, levelIndex = 0, direction = 1 )
            }

            barrels = barrels.mapNotNull { barrel ->
                barrel.x += barrel.direction * BARREL_SPEED
                val reachedRightEdge = barrel.x >= canvasWidth - 30f
                val reachedLeftEdge = barrel.x <= 30f
                if ( reachedRightEdge || reachedLeftEdge ) {
                    if ( barrel.levelIndex >= LEVEL_COUNT - 1 )
                        return@mapNotNull null // rolled off the bottom
                    barrel.levelIndex++
                    barrel.direction *= -1
                    barrel.x = barrel.x.coerceIn( 35f, canvasWidth - 35f )
                }
                barrel
            }

            val isInvulnerable = jumpTicksLeft > 0
            val hit = !isInvulnerable && climbProgress == 0f && barrels.any { barrel ->
                barrel.levelIndex == playerLevel && abs( barrel.x - playerX ) < COLLISION_DISTANCE
            }
            if ( hit ) isGameOver = true
        }
    }

    Box( modifier = Modifier.fillMaxSize() ) {
        Canvas( modifier = Modifier.fillMaxSize() ) {
            canvasWidth = size.width
            canvasHeight = size.height

            drawRect( color = Color(0xFF1B1B2A), size = size )

            for ( level in 0 until LEVEL_COUNT ) {
                val y = levelY( level, size.height )
                drawRect(
                    color = Color(0xFFE0507A),
                    topLeft = Offset( 20f, y ),
                    size = Size( size.width - 40f, 14f )
                )
                if ( level < LEVEL_COUNT - 1 ) {
                    val lx = ladderX( level, size.width )
                    val yTop = levelY( level, size.height )
                    val yBottom = levelY( level + 1, size.height )
                    drawLadder( lx, yTop, yBottom )
                }
            }

            // Donkey Kong + Pauline marker at the top level
            drawCircle(
                color = Color(0xFF8B4513),
                radius = 26f,
                center = Offset( size.width * 0.2f, levelY( 0, size.height ) - 30f )
            )
            drawCircle(
                color = Color(0xFFFF69B4),
                radius = 16f,
                center = Offset( size.width * 0.85f, levelY( 0, size.height ) - 20f )
            )

            barrels.forEach { barrel ->
                drawCircle(
                    color = Color(0xFF8B5A2B),
                    radius = 18f,
                    center = Offset( barrel.x, levelY( barrel.levelIndex, size.height ) - 18f )
                )
            }

            val playerY = if ( climbProgress > 0f && playerLevel > 0 )
                levelY( playerLevel, size.height ) - climbProgress * ( levelY( playerLevel, size.height ) - levelY( playerLevel - 1, size.height ) )
            else
                levelY( playerLevel, size.height )
            val jumpOffset = if ( jumpTicksLeft > 0 ) 30f else 0f

            drawRect(
                color = Color(0xFFFF0000),
                topLeft = Offset( playerX - 16f, playerY - 34f - jumpOffset ),
                size = Size( 32f, 34f )
            )
        }

        Column( modifier = Modifier.fillMaxSize() ) {
            Text(
                text = "Reach the top and save Pauline!",
                style = TextStyle( color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold ),
                modifier = Modifier.padding( 12.dp )
            )
        }

        if ( isGameOver || hasWon ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if ( hasWon ) "You saved Pauline!" else stringResource( R.string.game_over ),
                    color = if ( hasWon ) Color.Green else Color.Red
                )
                Button( onClick = { reset() } ) {
                    Text( stringResource( R.string.game_restart ) )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row( horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(8.dp) ) {
                    DkControlButton( Icons.Default.KeyboardArrowUp, "Climb up") { climbDir = 1 }
                }
                Row( horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(.8f).padding(8.dp) ) {
                    DkControlButton( Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Left" ) { moveDir = -1 }
                    Button( onClick = { jumpTicksLeft = JUMP_DURATION_TICKS } ) { Text( "Jump" ) }
                    DkControlButton( Icons.AutoMirrored.Filled.KeyboardArrowRight, "Right" ) { moveDir = 1 }
                }
                Row( horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(8.dp) ) {
                    DkControlButton( Icons.Default.KeyboardArrowDown, "Climb down") { climbDir = -1 }
                }
                Spacer( Modifier.width( 8.dp ) )
            }
        }
    }

    // Buttons above set the intent; clear it right after one tick so movement/climbing only
    // happens while a button is actively being held is out of scope here — instead each press
    // nudges the state and this stops it drifting forever once released isn't tracked, so
    // simplify: stop movement automatically shortly after a tap via LaunchedEffect below.
    LaunchedEffect( moveDir, climbDir ) {
        if ( moveDir != 0 || climbDir != 0 ) {
            delay( 250 )
            moveDir = 0
            climbDir = 0
        }
    }
}

@Composable
private fun DkControlButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        modifier = Modifier
            .clip( RoundedCornerShape(20.dp) )
            .background( color = Color.LightGray ),
        onClick = onClick
    ) {
        Icon( modifier = Modifier.size(48.dp), imageVector = imageVector, contentDescription = contentDescription )
    }
}

private fun DrawScope.drawLadder( x: Float, yTop: Float, yBottom: Float ) {
    val color = Color(0xFFCCCCCC)
    drawRect( color = color, topLeft = Offset( x - 20f, yTop ), size = Size( 4f, yBottom - yTop ) )
    drawRect( color = color, topLeft = Offset( x + 16f, yTop ), size = Size( 4f, yBottom - yTop ) )
    var rungY = yTop + 20f
    while ( rungY < yBottom ) {
        drawRect( color = color, topLeft = Offset( x - 20f, rungY ), size = Size( 40f, 4f ) )
        rungY += 24f
    }
}
