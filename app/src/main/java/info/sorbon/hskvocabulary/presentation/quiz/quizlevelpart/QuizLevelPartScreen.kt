package info.sorbon.hskvocabulary.presentation.quiz.quizlevelpart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.presentation.components.StarRatingBar
import info.sorbon.hskvocabulary.presentation.theme.Blue100
import info.sorbon.hskvocabulary.presentation.theme.Blue700
import info.sorbon.hskvocabulary.presentation.theme.StarColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuizLevelPartScreen(
    viewModel: QuizLevelPartViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onStartQuiz: (level: Int, part: Int, startWordId: Int, quizType: String, isRepeat: Boolean) -> Unit,
    onNavigateToHistory: (level: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${uiState.level.title} - ${uiState.quizType.displayTitle(uiState.languageName)}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToHistory(uiState.level.level) }) {
                        Icon(Icons.Default.History, "History")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stats card
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow(Icons.Default.MenuBook, "Learned words", "${uiState.learnedWords}/${uiState.totalWords}")
                    StatRow(Icons.Default.Star, "Got stars", "${uiState.earnedStars}/${uiState.maxStars}")
                    StatRow(Icons.Default.Schedule, "Total time", uiState.totalTime)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Parts grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.parts.forEach { part ->
                    PartCell(
                        part = part,
                        onClick = { viewModel.selectPart(part) },
                        isSelected = part.partNumber == uiState.selectedPart?.partNumber,
                        modifier = Modifier.size(62.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Start button
            if (uiState.selectedPart != null) {
                Button(
                    onClick = {
                        uiState.selectedPart?.let { part ->
                            val isRepeat = part.isOpen && !part.isCurrent
                            onStartQuiz(uiState.level.level, part.partNumber, part.startWordId, uiState.quizType.key, isRepeat)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Start ${uiState.selectedPart!!.partNumber} round", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun StatRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Icon(icon, null, modifier = Modifier.padding(start = 8.dp).size(20.dp), tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun PartCell(part: QuizLevelPart, onClick: () -> Unit, isSelected: Boolean, modifier: Modifier) {
    val isLocked = !part.isOpen && !part.isCurrent
    val bgColor = when {
        part.isCurrent -> Blue700
        part.isOpen -> Blue100
        else -> Blue100
    }
    val textColor = if (part.isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
    val borderMod = if (isSelected && !part.isCurrent) {
        Modifier.border(2.dp, Blue700, MaterialTheme.shapes.small)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(borderMod)
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .clickable(enabled = !isLocked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${part.partNumber}",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = textColor
            )
            if (part.isOpen && !part.isCurrent) {
                StarRatingBar(rating = part.rating, starSize = 10.dp)
            }
        }
        if (isLocked) {
            Box(
                modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.padding(4.dp).size(12.dp))
            }
        }
    }
}
