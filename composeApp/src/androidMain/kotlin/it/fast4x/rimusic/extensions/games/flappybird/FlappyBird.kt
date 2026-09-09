package it.fast4x.rimusic.extensions.games.flappybird

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.kreate.android.R
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Pipe(
    var x: Float,
    val gapCenterY: Float,
    var scored: Boolean = false
)

private const val GRAVITY = 0.55f
private const val FLAP_VELOCITY = -11f
private const val PIPE_SPEED = 5f
private const val PIPE_WIDTH = 110f
private const val GAP_HEIGHT = 320f
private const val PIPE_SPACING = 480f
private const val BIRD_RADIUS = 22f
private const val BIRD_X_FRACTION = 0.28f
private const val FRAME_DELAY_MS = 16L

@Composable
fun FlappyBirdGame() {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    var birdY by remember { mutableFloatStateOf(0f) }
    var velocity by remember { mutableFloatStateOf(0f) }
    var pipes by remember { mutableStateOf(listOf<Pipe>()) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }
    var gameId by remember { mutableIntStateOf(0) }

    fun reset() {
        birdY = canvasHeight / 2f
        velocity = 0f
        pipes = emptyList()
        score = 0
        isGameOver = false
        hasStarted = false
        gameId++
    }

    fun flap() {
        if (canvasHeight <= 0f) return
        if (isGameOver) {
            reset()
            return
        }
        if (!hasStarted) {
            hasStarted = true
            birdY = canvasHeight / 2f
        }
        velocity = FLAP_VELOCITY
    }

    LaunchedEffect(gameId, canvasHeight) {
        if (canvasHeight <= 0f) return@LaunchedEffect
        if (birdY == 0f) birdY = canvasHeight / 2f

        while (!isGameOver) {
            delay(FRAME_DELAY_MS)
            if (!hasStarted) continue

            velocity += GRAVITY
            birdY += velocity

            pipes = pipes.map { it.copy(x = it.x - PIPE_SPEED) }
                .filter { it.x > -PIPE_WIDTH }

            val last = pipes.maxByOrNull { it.x }
            if (last == null || last.x < canvasWidth - PIPE_SPACING) {
                val margin = GAP_HEIGHT
                val gapCenter = Random.nextFloat() * (canvasHeight - 2 * margin) + margin
                pipes = pipes + Pipe(x = canvasWidth, gapCenterY = gapCenter)
            }

            val birdX = canvasWidth * BIRD_X_FRACTION
            pipes = pipes.map { pipe ->
                if (!pipe.scored && pipe.x + PIPE_WIDTH < birdX) {
                    score++
                    pipe.copy(scored = true)
                } else pipe
            }

            val hitGround = birdY + BIRD_RADIUS >= canvasHeight
            val hitCeiling = birdY - BIRD_RADIUS <= 0f
            val hitPipe = pipes.any { pipe ->
                val withinPipeX = birdX + BIRD_RADIUS > pipe.x && birdX - BIRD_RADIUS < pipe.x + PIPE_WIDTH
                val withinGap = birdY - BIRD_RADIUS > pipe.gapCenterY - GAP_HEIGHT / 2f &&
                                birdY + BIRD_RADIUS < pipe.gapCenterY + GAP_HEIGHT / 2f
                withinPipeX && !withinGap
            }

            if (hitGround || hitCeiling || hitPipe) {
                isGameOver = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures( onTap = { flap() } )
                }
        ) {
            canvasWidth = size.width
            canvasHeight = size.height

            drawRect( color = Color(0xFF70C5CE), size = size )

            pipes.forEach { pipe ->
                drawPipe( pipe, size.height )
            }

            drawCircle(
                color = Color(0xFFFFD54F),
                radius = BIRD_RADIUS,
                center = Offset( size.width * BIRD_X_FRACTION, birdY.coerceIn(0f, size.height) )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "$score",
                style = TextStyle( color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold )
            )
        }

        if (isGameOver) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text( text = stringResource( R.string.game_over ), color = Color.Red )
                Button( onClick = { reset() } ) {
                    Text( stringResource( R.string.game_restart ) )
                }
            }
        } else if (!hasStarted) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text( text = "Tap to flap", color = Color.White )
            }
        }
    }
}

private fun DrawScope.drawPipe( pipe: Pipe, canvasHeight: Float ) {
    val color = Color(0xFF2E8B2E)
    val gapTop = pipe.gapCenterY - GAP_HEIGHT / 2f
    val gapBottom = pipe.gapCenterY + GAP_HEIGHT / 2f

    drawRect(
        color = color,
        topLeft = Offset( pipe.x, 0f ),
        size = Size( PIPE_WIDTH, gapTop )
    )
    drawRect(
        color = color,
        topLeft = Offset( pipe.x, gapBottom ),
        size = Size( PIPE_WIDTH, canvasHeight - gapBottom )
    )
}
