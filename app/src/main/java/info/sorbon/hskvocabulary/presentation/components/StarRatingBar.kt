package info.sorbon.hskvocabulary.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import info.sorbon.hskvocabulary.presentation.theme.StarColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StarRatingBar(
    rating: Double,
    maxRating: Int = 3,
    starSize: Dp = 20.dp,
    filledColor: Color = StarColor,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        for (i in 0 until maxRating) {
            val fillFraction = when {
                rating >= i + 1 -> 1f
                rating > i -> (rating - i).toFloat()
                else -> 0f
            }
            StarIcon(
                fillFraction = fillFraction,
                filledColor = filledColor,
                emptyColor = emptyColor,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
private fun StarIcon(
    fillFraction: Float,
    filledColor: Color,
    emptyColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val starPath = createStarPath(size)
        // Draw empty star
        drawPath(starPath, color = emptyColor)
        // Draw filled portion
        if (fillFraction > 0f) {
            clipRect(right = size.width * fillFraction) {
                drawPath(starPath, color = filledColor)
            }
        }
    }
}

private fun DrawScope.createStarPath(size: Size): Path {
    val path = Path()
    val cx = size.width / 2
    val cy = size.height / 2
    val outerRadius = size.width / 2
    val innerRadius = outerRadius * 0.4f
    val startAngle = -PI / 2

    for (i in 0 until 5) {
        val outerAngle = startAngle + (i * 2 * PI / 5)
        val innerAngle = startAngle + ((i * 2 + 1) * PI / 5)
        val ox = cx + (outerRadius * cos(outerAngle)).toFloat()
        val oy = cy + (outerRadius * sin(outerAngle)).toFloat()
        val ix = cx + (innerRadius * cos(innerAngle)).toFloat()
        val iy = cy + (innerRadius * sin(innerAngle)).toFloat()
        if (i == 0) path.moveTo(ox, oy) else path.lineTo(ox, oy)
        path.lineTo(ix, iy)
    }
    path.close()
    return path
}
