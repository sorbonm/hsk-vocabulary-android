package info.sorbon.hskvocabulary.presentation.leaderboard

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import info.sorbon.hskvocabulary.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.domain.model.LeaderboardEntry
import info.sorbon.hskvocabulary.presentation.components.EmptyStateView
import info.sorbon.hskvocabulary.presentation.theme.Blue50
import info.sorbon.hskvocabulary.presentation.theme.Blue600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    val myEntry = uiState.entries.find { it.userId == uiState.currentUserId }
                    if (myEntry != null) {
                        IconButton(onClick = {
                            val rankText = when (myEntry.rank) {
                                1 -> "1st"; 2 -> "2nd"; 3 -> "3rd"; else -> "#${myEntry.rank}"
                            }
                            val text = "I'm ranked $rankText in HSK ${uiState.selectedLevel} Leaderboard " +
                                    "with ${myEntry.totalCorrect} points! Can you beat me?\n\n" +
                                    "Android: https://play.google.com/store/apps/details?id=info.sorbon.hskvocabulary\n" +
                                    "iOS: https://apps.apple.com/app/id1449305092"
                            val intent = Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                },
                                "Share your ranking"
                            )
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Share, "Share")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(6) { i ->
                    val level = i + 1
                    FilterChip(
                        selected = level == uiState.selectedLevel,
                        onClick = { viewModel.selectLevel(level) },
                        label = { Text("HSK $level") }
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp))
            } else if (uiState.entries.isEmpty()) {
                EmptyStateView("No results yet", "Be the first to complete a quiz!")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(uiState.entries, key = { "${it.userId}_${it.rank}" }) { entry ->
                        LeaderboardRow(entry, isCurrentUser = entry.userId == uiState.currentUserId)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, isCurrentUser: Boolean) {
    val rankText = when (entry.rank) { 1 -> "\uD83E\uDD47"; 2 -> "\uD83E\uDD48"; 3 -> "\uD83E\uDD49"; else -> "#${entry.rank}" }
    val mins = entry.totalDuration / 60; val secs = entry.totalDuration % 60
    val bgColor = if (isCurrentUser) Blue50 else MaterialTheme.colorScheme.surface

    Surface(shape = MaterialTheme.shapes.small, color = bgColor, tonalElevation = if (isCurrentUser) 2.dp else 0.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(rankText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.nickname, fontSize = 15.sp, fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium)
                    if (entry.platform.isNotBlank()) {
                        Spacer(Modifier.width(4.dp))
                        val platformRes = if (entry.platform == "ios") R.drawable.ic_apple else R.drawable.ic_android
                        Icon(painterResource(platformRes), contentDescription = entry.platform, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (entry.country.isNotBlank()) Text("${flagEmoji(entry.country)} ${java.util.Locale("", entry.country).displayCountry}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${entry.totalCorrect}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Blue600)
                Text("$mins:${String.format("%02d", secs)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
