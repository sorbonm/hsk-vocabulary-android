package info.sorbon.hskvocabulary.presentation.quiz.quiz

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.domain.model.QuizType
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.presentation.components.BannerAdView
import info.sorbon.hskvocabulary.presentation.components.OptionState
import info.sorbon.hskvocabulary.presentation.components.QuizOptionCard
import info.sorbon.hskvocabulary.presentation.theme.Green500

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel = hiltViewModel(),
    onFinish: (level: Int, part: Int, quizType: String, correct: Int, total: Int, duration: Int, incorrectIds: String) -> Unit,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            val incorrectIds = uiState.incorrectWords.joinToString(",") { it.id.toString() }
            onFinish(uiState.level, uiState.part, uiState.quizType.key, uiState.correctCount, uiState.totalCount, uiState.duration, incorrectIds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.quizType.displayTitle(uiState.languageName)) },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Close, "Close")
                    }
                },
                actions = {
                    Text(uiState.timerText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.padding(end = 16.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Green500,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            uiState.question?.let { q ->
                val type = uiState.quizType
                val idx = uiState.currentIndex
                val showHanzi = type == QuizType.ChineseLang || (type == QuizType.MixAll && idx in 3..5)
                val showDefinition = type == QuizType.LangChinese || (type == QuizType.MixAll && idx in 6..9)
                val showSpeaker = type == QuizType.ListenToGuess || (type == QuizType.MixAll && idx < 3)
                val useGrid = type == QuizType.LangChinese || (type == QuizType.MixAll && idx >= 6)

                // Question area — fills remaining space
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        showSpeaker -> {
                            IconButton(onClick = { viewModel.speakCurrentQuestion() }, modifier = Modifier.size(100.dp)) {
                                Icon(Icons.Default.VolumeUp, "Play", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        showHanzi -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(q.question.hanzi, fontSize = 50.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Center)
                                Text(q.question.pinyin, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        showDefinition -> {
                            Text(q.question.localizedDefinition, fontSize = 20.sp, textAlign = TextAlign.Center, maxLines = 5)
                        }
                    }
                }

                // Banner ad below question
                BannerAdView(modifier = Modifier.padding(vertical = 8.dp))

                // Options (bottom-aligned)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (useGrid) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), maxItemsInEachRow = 2) {
                            q.options.forEach { option ->
                                QuizOptionCard(
                                    text = option.hanzi,
                                    state = optionState(option, uiState),
                                    isGrid = true,
                                    onClick = { viewModel.onAnswer(option) },
                                    enabled = uiState.answerState == AnswerState.Idle,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        q.options.forEach { option ->
                            val displayText = if (showHanzi || showSpeaker) option.localizedDefinition else option.hanzi
                            QuizOptionCard(
                                text = displayText,
                                state = optionState(option, uiState),
                                isGrid = false,
                                onClick = { viewModel.onAnswer(option) },
                                enabled = uiState.answerState == AnswerState.Idle
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Quiz?") },
            text = { Text("Your progress will be lost.") },
            confirmButton = { TextButton(onClick = { showExitDialog = false; onClose() }) { Text("Exit") } },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("Continue") } }
        )
    }
}

private fun optionState(option: Word, uiState: QuizUiState): OptionState {
    if (uiState.answerState == AnswerState.Idle) return OptionState.Default
    return when {
        option.id == uiState.correctOptionId -> OptionState.Correct
        option.id == uiState.selectedOptionId -> OptionState.Incorrect
        else -> OptionState.Dimmed
    }
}
