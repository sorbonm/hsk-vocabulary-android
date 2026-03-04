package info.sorbon.hskvocabulary.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.domain.model.HskLevel
import info.sorbon.hskvocabulary.domain.model.PracticeType
import info.sorbon.hskvocabulary.presentation.components.HskLevelCard
import info.sorbon.hskvocabulary.presentation.components.PracticeCard
import info.sorbon.hskvocabulary.presentation.components.SectionHeader
import info.sorbon.hskvocabulary.presentation.components.BannerAdView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToLevel: (Int) -> Unit,
    onNavigateToPracticeLevel: (type: String) -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToContacts: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "HSK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToLanguage) {
                        Text(
                            text = uiState.activeLanguage.flag,
                            fontSize = 22.sp
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Banner Ad
            if (uiState.showAds) {
                item { BannerAdView() }
            }

            // LEVEL Section
            item { SectionHeader("LEVEL") }
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 3
                ) {
                    HskLevel.entries.forEach { level ->
                        HskLevelCard(
                            level = level,
                            rating = uiState.levelRatings.find { it.hskLevel == level },
                            onClick = { onNavigateToLevel(level.level) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // PRACTICE Section
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader("PRACTICE")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        PracticeCard(
                            title = PracticeType.Quiz.title,
                            subtitle = PracticeType.Quiz.subtitle,
                            backgroundRes = PracticeType.Quiz.backgroundRes,
                            onClick = { onNavigateToPracticeLevel("quiz") }
                        )
                    }
                    item {
                        PracticeCard(
                            title = PracticeType.Flashcard.title,
                            subtitle = PracticeType.Flashcard.subtitle,
                            backgroundRes = PracticeType.Flashcard.backgroundRes,
                            onClick = { onNavigateToPracticeLevel("flashcard") }
                        )
                    }
                    item {
                        PracticeCard(
                            title = PracticeType.OnlineTest.title,
                            subtitle = PracticeType.OnlineTest.subtitle,
                            backgroundRes = PracticeType.OnlineTest.backgroundRes,
                            onClick = { onNavigateToPracticeLevel("online_test") }
                        )
                    }
                }
            }

            // OTHER Section
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader("OTHER")
            }
            item {
                OtherSectionItem(
                    icon = Icons.Default.Bookmark,
                    title = "Bookmarks",
                    badge = "${uiState.bookmarkCount}",
                    onClick = onNavigateToBookmarks
                )
            }
            item {
                OtherSectionItem(
                    icon = Icons.Default.EmojiEvents,
                    title = "Leaderboard",
                    onClick = onNavigateToLeaderboard
                )
            }
            item {
                OtherSectionItem(
                    icon = Icons.Default.Star,
                    title = "Rate App",
                    onClick = {
                        val reviewManager = ReviewManagerFactory.create(activity)
                        reviewManager.requestReviewFlow().addOnSuccessListener { reviewInfo ->
                            reviewManager.launchReviewFlow(activity, reviewInfo)
                        }
                    }
                )
            }
            item {
                OtherSectionItem(
                    icon = Icons.Default.Info,
                    title = "Contacts",
                    onClick = onNavigateToContacts
                )
            }
        }
    }

}

@Composable
private fun OtherSectionItem(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            if (badge != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
