package info.sorbon.hskvocabulary.presentation.quiz.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.domain.model.QuizResult
import info.sorbon.hskvocabulary.domain.model.Rating
import info.sorbon.hskvocabulary.presentation.components.EmptyStateView
import info.sorbon.hskvocabulary.presentation.components.StarRatingBar
import info.sorbon.hskvocabulary.presentation.theme.Blue700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHistoryScreen(
    viewModel: QuizHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val sections by viewModel.sections.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz History") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (sections.isEmpty()) {
            EmptyStateView("No quiz history yet", "Start a quiz to see your results here", Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEach { section ->
                    item {
                        Text(
                            section.dateString,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(section.results, key = { it.id }) { result ->
                        HistoryCard(result)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(result: QuizResult) {
    val mins = result.duration / 60
    val secs = result.duration % 60
    val percent = if (result.correctAnswer > 0) (result.correctAnswer * 100) / 10 else 0
    val rating = Rating.fromPercent(percent)
    val timeText = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(result.createDate))

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "HSK ${result.level} \u2022 Part ${result.wordPart}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue700
                )
                Text(
                    timeText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                result.quizType,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${result.correctAnswer}/10 correct  \u2022  $mins:${String.format("%02d", secs)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StarRatingBar(rating = rating.score, starSize = 16.dp)
            }
        }
    }
}
