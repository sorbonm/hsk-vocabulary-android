package info.sorbon.hskvocabulary.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.domain.model.HskLevel
import info.sorbon.hskvocabulary.presentation.components.SectionHeader
import info.sorbon.hskvocabulary.presentation.leaderboard.NicknameSetupSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToQuizHistory: (level: Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNicknameSheet by remember { mutableStateOf(false) }
    var showTargetLevelPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // ── PROGRESS ──
                item {
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            uiState.levelProgress.forEachIndexed { index, progress ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = progress.hskLevel.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${progress.learnedWords}/${progress.hskLevel.wordCount} words",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progress.progressPercent.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                )
                                if (index < uiState.levelProgress.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }

                // ── MY RANKING ──
                item {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader("MY RANKING")
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Surface(
                        onClick = {
                            val level = uiState.bestRanking?.hskLevel?.level
                            if (level != null) onNavigateToQuizHistory(level)
                        },
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.bestRanking != null) {
                            val ranking = uiState.bestRanking!!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ranking.hskLevel.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Score: ${ranking.totalCorrect} \u00B7 ${ranking.totalDuration}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "No quiz results yet",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // ── SETTINGS ──
                item {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader("SETTINGS")
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Show in leaderboard
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Show in leaderboard",
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = uiState.showInLeaderboard,
                                    onCheckedChange = { viewModel.setShowInLeaderboard(it) }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                            // Target HSK level
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTargetLevelPicker = true }
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Target HSK level",
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (uiState.targetHskLevel > 0) "HSK ${uiState.targetHskLevel}" else "Not set",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ── ACCOUNT ──
                item {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader("ACCOUNT")
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Link Google Account (coming soon)",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showNicknameSheet) {
        NicknameSetupSheet(
            currentNickname = uiState.nickname,
            currentCountry = uiState.country,
            onSave = { newNickname, newCountry ->
                viewModel.updateProfile(newNickname, newCountry)
                showNicknameSheet = false
            },
            onDismiss = { showNicknameSheet = false }
        )
    }

    if (showTargetLevelPicker) {
        TargetLevelPickerSheet(
            currentLevel = uiState.targetHskLevel,
            onSelect = { level ->
                viewModel.setTargetHskLevel(level)
                showTargetLevelPicker = false
            },
            onDismiss = { showTargetLevelPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetLevelPickerSheet(
    currentLevel: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Text(
            text = "Target HSK Level",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HskLevel.entries.forEach { level ->
            ListItem(
                headlineContent = { Text(level.title, fontSize = 16.sp) },
                trailingContent = {
                    if (level.level == currentLevel) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.clickable { onSelect(level.level) }
            )
            if (level != HskLevel.entries.last()) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
