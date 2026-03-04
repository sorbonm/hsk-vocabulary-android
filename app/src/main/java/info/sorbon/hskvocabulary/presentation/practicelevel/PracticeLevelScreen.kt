package info.sorbon.hskvocabulary.presentation.practicelevel

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.presentation.flashcard.FlashcardModeSheet
import info.sorbon.hskvocabulary.presentation.quiz.quiztype.QuizTypeSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeLevelScreen(
    viewModel: PracticeLevelViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToQuizLevelPart: (level: Int, quizType: String) -> Unit,
    onNavigateToFlashcard: (level: Int, mode: String) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToQuizHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedQuizLevel by remember { mutableIntStateOf(1) }
    var showQuizTypeSheet by remember { mutableStateOf(false) }
    var selectedFlashcardLevel by remember { mutableIntStateOf(1) }
    var showFlashcardModeSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.practiceType == "quiz") {
                        IconButton(onClick = onNavigateToLeaderboard) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Leaderboard"
                            )
                        }
                        IconButton(onClick = onNavigateToQuizHistory) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "SELECT LEVEL",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(uiState.levels) { item ->
                LevelCard(
                    item = item,
                    practiceType = uiState.practiceType,
                    onClick = {
                        when (uiState.practiceType) {
                            "quiz" -> {
                                selectedQuizLevel = item.hskLevel.level
                                showQuizTypeSheet = true
                            }
                            "flashcard" -> {
                                selectedFlashcardLevel = item.hskLevel.level
                                showFlashcardModeSheet = true
                            }
                            "online_test" -> {
                                val packageName = item.hskLevel.onlineTestPackageName
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                } else {
                                    try {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                                        )
                                    } catch (_: ActivityNotFoundException) {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showQuizTypeSheet) {
        QuizTypeSheet(
            activeLanguage = uiState.activeLanguage,
            onSelectType = { type ->
                showQuizTypeSheet = false
                onNavigateToQuizLevelPart(selectedQuizLevel, type.key)
            },
            onDismiss = { showQuizTypeSheet = false }
        )
    }

    if (showFlashcardModeSheet) {
        FlashcardModeSheet(
            onSelectMode = { mode ->
                showFlashcardModeSheet = false
                onNavigateToFlashcard(selectedFlashcardLevel, mode)
            },
            onDismiss = { showFlashcardModeSheet = false }
        )
    }
}

@Composable
private fun LevelCard(
    item: PracticeLevelItem,
    practiceType: String,
    onClick: () -> Unit
) {
    val level = item.hskLevel
    val gradientColors = listOf(level.backgroundColor, level.backgroundColor.copy(alpha = 0.85f))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(gradientColors))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = level.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.weight(1f))

            when (practiceType) {
                "quiz" -> {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${item.learnedWords}/${level.wordCount} \u6587",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "${item.earnedStars}/${level.maxStarCount} \u2B50",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                "flashcard" -> {
                    Text(
                        text = "0/${level.wordCount} \u6587",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                "online_test" -> {
                    Text(
                        text = "${level.wordCount} \u6587",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
