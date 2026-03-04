package info.sorbon.hskvocabulary.presentation.quiz.quiztype

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.sorbon.hskvocabulary.domain.model.Language
import info.sorbon.hskvocabulary.domain.model.QuizType
import info.sorbon.hskvocabulary.presentation.theme.Blue300
import info.sorbon.hskvocabulary.presentation.theme.Blue500
import info.sorbon.hskvocabulary.presentation.theme.Blue700
import info.sorbon.hskvocabulary.presentation.theme.Blue900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTypeSheet(
    activeLanguage: Language,
    onSelectType: (QuizType) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Quiz Type",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            QuizType.entries.forEach { type ->
                val colors = when (type) {
                    QuizType.MixAll -> listOf(Blue700, Blue900)
                    QuizType.ListenToGuess -> listOf(Blue300, Blue500)
                    QuizType.LangChinese -> listOf(Color(0xFF7B4FBF), Color(0xFF5A3090))
                    QuizType.ChineseLang -> listOf(Color(0xFF2E8B57), Color(0xFF1B6040))
                }
                QuizTypeCard(
                    title = type.displayTitle(activeLanguage.nameInEnglish),
                    gradientColors = colors,
                    onClick = { onSelectType(type) }
                )
            }
            Box(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuizTypeCard(
    title: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(gradientColors))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 2
        )
    }
}
