package it.fast4x.rimusic.ui.styling

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.colorPalette

val ColorPalette.isLiquidGlass: Boolean
    get() = this === LiquidGlassColorPalette

/**
 * Dark aurora backdrop. Glass only reads as glass over something colourful, so the "Liquid Glass"
 * theme paints this instead of a flat background colour.
 */
fun Modifier.liquidGlassBackdrop(): Modifier = drawBehind {
    drawRect(
        Brush.verticalGradient(
            listOf( Color(0xff0a0f24), Color(0xff101a44), Color(0xff0b1230) )
        )
    )

    val reach = size.maxDimension

    fun glow( color: Color, center: Offset, radius: Float ) =
        drawCircle(
            brush = Brush.radialGradient( listOf( color, Color.Transparent ), center, radius ),
            radius = radius,
            center = center
        )

    glow( Color(0x664b7bff), Offset( size.width * 0.12f, size.height * 0.08f ), reach * 0.55f )
    glow( Color(0x5538d6c4), Offset( size.width * 0.95f, size.height * 0.55f ), reach * 0.5f )
    glow( Color(0x55b26bff), Offset( size.width * 0.2f, size.height * 0.95f ), reach * 0.55f )
}

/**
 * Frosted-glass surface: a translucent white sheen that's brighter at the top, plus a thin
 * specular rim that fades along its edge, like light catching the border of a pane of glass.
 */
fun Modifier.liquidGlass( shape: Shape, sheen: Float = 0.14f ): Modifier =
    this.clip( shape )
        .background(
            Brush.verticalGradient(
                listOf(
                    Color.White.copy( alpha = sheen + 0.06f ),
                    Color.White.copy( alpha = (sheen - 0.05f).coerceAtLeast( 0.02f ) )
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    Color.White.copy( alpha = 0.55f ),
                    Color.White.copy( alpha = 0.06f ),
                    Color.White.copy( alpha = 0.28f )
                )
            ),
            shape = shape
        )

/** [color] as a flat background on every theme except "Liquid Glass", where it becomes glass. */
@Composable
fun Modifier.surfaceBackground( shape: Shape, color: Color ): Modifier =
    if( colorPalette().isLiquidGlass ) liquidGlass( shape ) else background( color )
