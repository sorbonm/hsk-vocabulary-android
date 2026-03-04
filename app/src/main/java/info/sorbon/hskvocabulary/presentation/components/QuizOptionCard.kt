package info.sorbon.hskvocabulary.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.sorbon.hskvocabulary.presentation.theme.Green500
import info.sorbon.hskvocabulary.presentation.theme.Red500

enum class OptionState { Default, Correct, Incorrect, Dimmed }

@Composable
fun QuizOptionCard(
    text: String,
    state: OptionState,
    isGrid: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            OptionState.Correct -> Green500
            OptionState.Incorrect -> Red500
            OptionState.Default, OptionState.Dimmed -> Color.Transparent
        },
        label = "border"
    )
    val bgColor = when (state) {
        OptionState.Default -> MaterialTheme.colorScheme.surface
        OptionState.Dimmed -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    val elevation = if (state == OptionState.Default) 0.dp else 1.dp

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = bgColor,
        tonalElevation = elevation,
        border = if (borderColor != Color.Transparent) BorderStroke(2.dp, borderColor) else null,
        modifier = modifier.then(
            if (isGrid) Modifier.aspectRatio(1f) else Modifier.fillMaxWidth().heightIn(min = 52.dp)
        )
    ) {
        val hasIcon = state == OptionState.Correct || state == OptionState.Incorrect
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                fontSize = if (isGrid) 36.sp else 17.sp,
                fontWeight = if (isGrid) FontWeight.Normal else FontWeight.Light,
                textAlign = TextAlign.Center,
                maxLines = if (isGrid) 1 else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = if (hasIcon) Modifier.padding(end = 28.dp) else Modifier
            )
            if (hasIcon) {
                Icon(
                    imageVector = if (state == OptionState.Correct) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (state == OptionState.Correct) Green500 else Red500,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(20.dp)
                )
            }
        }
    }
}
