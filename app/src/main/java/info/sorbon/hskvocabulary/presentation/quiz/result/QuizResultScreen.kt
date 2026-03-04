package info.sorbon.hskvocabulary.presentation.quiz.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.R
import info.sorbon.hskvocabulary.domain.model.Rating
import info.sorbon.hskvocabulary.presentation.components.StarRatingBar
import info.sorbon.hskvocabulary.presentation.components.WordListItem
import info.sorbon.hskvocabulary.presentation.theme.Blue50
import info.sorbon.hskvocabulary.presentation.theme.Blue700

@Composable
fun QuizResultScreen(
    level: Int,
    part: Int,
    quizType: String,
    correct: Int,
    total: Int,
    duration: Int,
    viewModel: QuizResultViewModel = hiltViewModel(),
    onRepeat: () -> Unit,
    onComplete: () -> Unit,
    onNext: () -> Unit
) {
    val incorrectWords by viewModel.incorrectWords.collectAsStateWithLifecycle()

    val percent = if (total > 0) (correct * 100) / total else 0
    val rating = Rating.fromPercent(percent)
    val isPerfect = correct == total
    val mins = duration / 60
    val secs = duration % 60

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with rating
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Blue700),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(rating.description, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                StarRatingBar(rating = rating.score, starSize = 30.dp, filledColor = Color(0xFFFFC107), emptyColor = Color.White.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))
                Text("HSK $level - Part $part", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        // Stats
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$correct/$total", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Correct", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$mins:${String.format("%02d", secs)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Duration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        HorizontalDivider()

        // Incorrect words or success message
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            if (isPerfect) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.party_popper),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Wow! Keep up the good work!\nAll answers are correct!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(incorrectWords, key = { it.id }) { word ->
                    WordListItem(word = word, onClick = { })
                    HorizontalDivider()
                }
            }
        }

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isPerfect) {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue50, contentColor = Blue700),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.small
                ) { Text("Complete") }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.small
                ) { Text("Next") }
            } else {
                Button(
                    onClick = onRepeat,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue50, contentColor = Blue700),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.small
                ) { Text("Repeat") }
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.small
                ) { Text("Complete") }
            }
        }
    }
}
