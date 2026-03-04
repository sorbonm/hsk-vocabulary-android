package info.sorbon.hskvocabulary.presentation.flashcard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.presentation.theme.Blue50
import info.sorbon.hskvocabulary.presentation.theme.Blue600
import info.sorbon.hskvocabulary.presentation.theme.Blue700
import info.sorbon.hskvocabulary.presentation.theme.Green500
import info.sorbon.hskvocabulary.presentation.theme.Red500
import info.sorbon.hskvocabulary.presentation.theme.Red800
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    viewModel: FlashcardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToNextLevel: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val swipeThreshold = 200f
    var showTimerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.progressText) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { viewModel.togglePinyin() }) {
                        Icon(
                            if (uiState.showPinyin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.showPinyin) "Hide Pinyin" else "Show Pinyin"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoaded && uiState.words.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No words available", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Text("Words haven't been loaded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                return@Scaffold
            }

            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Green500, trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            viewModel.currentWord()?.let { word ->
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Swipe overlay labels
                    if (offsetX.value < -50f) {
                        Text("REVIEW", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Red500.copy(alpha = (abs(offsetX.value) / swipeThreshold).coerceIn(0f, 1f)),
                            modifier = Modifier.align(Alignment.TopEnd).padding(top = 30.dp, end = 14.dp).rotate(18f))
                    }
                    if (offsetX.value > 50f) {
                        Text("REMEMBER", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Green500.copy(alpha = (abs(offsetX.value) / swipeThreshold).coerceIn(0f, 1f)),
                            modifier = Modifier.align(Alignment.TopStart).padding(top = 26.dp, start = 14.dp).rotate(-18f))
                    }

                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(400.dp)
                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                            .graphicsLayer { rotationZ = offsetX.value / 40f }
                            .then(
                                if (!uiState.isTimerMode) Modifier.pointerInput(Unit) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            scope.launch {
                                                if (abs(offsetX.value) > swipeThreshold) {
                                                    val dir = if (offsetX.value > 0) SwipeDirection.Right else SwipeDirection.Left
                                                    offsetX.animateTo(if (offsetX.value > 0) 1000f else -1000f, tween(200))
                                                    viewModel.onSwipe(dir)
                                                    offsetX.snapTo(0f)
                                                } else {
                                                    offsetX.animateTo(0f, tween(200))
                                                }
                                            }
                                        },
                                        onHorizontalDrag = { _, dragAmount -> scope.launch { offsetX.snapTo(offsetX.value + dragAmount) } }
                                    )
                                } else Modifier
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!uiState.isTimerMode) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = { viewModel.speak() }) { Icon(Icons.Default.VolumeUp, "Speak", tint = Blue600, modifier = Modifier.size(30.dp)) }
                                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                                        Icon(if (word.isBookmark) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, "Bookmark", tint = Red800, modifier = Modifier.size(30.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.weight(1f))
                            if (uiState.showPinyin) {
                                Text(word.pinyin, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(word.hanzi, fontSize = 50.sp, fontWeight = FontWeight.SemiBold, color = Blue600, textAlign = TextAlign.Center)
                            if (uiState.showTranslation || uiState.isTimerMode) {
                                Spacer(Modifier.height(8.dp))
                                Text(word.localizedDefinition, fontSize = 17.sp, textAlign = TextAlign.Center)
                            }
                            Spacer(Modifier.weight(1f))

                            if (!uiState.isTimerMode) {
                                Button(
                                    onClick = { viewModel.toggleTranslation() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Blue50, contentColor = Blue700),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(if (uiState.showTranslation) "Hide translation" else "Show translation")
                                }
                            }
                        }
                    }
                }

                // Timer display (only in timer mode)
                if (uiState.isTimerMode) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val minutes = uiState.timerCountdown / 60
                        val seconds = uiState.timerCountdown % 60
                        Text(
                            text = "%d:%02d".format(minutes, seconds),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        IconButton(onClick = { showTimerDialog = true }) {
                            Icon(Icons.Default.Edit, "Edit timer", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Timer duration picker dialog
    if (showTimerDialog) {
        var sliderValue by remember { mutableFloatStateOf(uiState.timerSeconds.toFloat()) }
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Timer Duration") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${sliderValue.toInt()} seconds", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(16.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setTimerDuration(sliderValue.toInt())
                    showTimerDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Completion dialog
    if (uiState.isCompleted) {
        val isMaxLevel = uiState.level >= 6
        val wordCount = uiState.words.size
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    if (isMaxLevel) "Congratulations!" else "Level Complete!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isMaxLevel) "You've reviewed all $wordCount words!"
                    else "You've reviewed all $wordCount words!\nMove to next level or start over?"
                )
            },
            confirmButton = {
                if (isMaxLevel) {
                    TextButton(onClick = { viewModel.restartFlashcards() }) { Text("Start Over") }
                } else {
                    TextButton(onClick = { onNavigateToNextLevel(uiState.level + 1) }) { Text("Next Level") }
                }
            },
            dismissButton = {
                if (isMaxLevel) {
                    TextButton(onClick = onNavigateBack) { Text("Close") }
                } else {
                    TextButton(onClick = { viewModel.restartFlashcards() }) { Text("Start Over") }
                }
            }
        )
    }
}
