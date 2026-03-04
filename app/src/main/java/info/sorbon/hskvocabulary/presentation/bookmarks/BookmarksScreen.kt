package info.sorbon.hskvocabulary.presentation.bookmarks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.presentation.components.EmptyStateView
import info.sorbon.hskvocabulary.presentation.components.WordListItem
import info.sorbon.hskvocabulary.presentation.levelwords.WordDetailSheet
import info.sorbon.hskvocabulary.presentation.theme.Red800
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.totalCount > 0) {
                        IconButton(onClick = viewModel::onShowDeleteAllDialog) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Remove all bookmarks",
                                tint = Red800
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.sections.isEmpty()) {
            EmptyStateView(
                title = "It's empty here",
                subtitle = "Learn words and bookmark them",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                uiState.sections.forEach { section ->
                    // Section header (grouped by HSK level)
                    stickyHeader(key = "bookmark_${section.levelTitle}") {
                        Text(
                            text = section.levelTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(
                        items = section.words,
                        key = { "bm_${it.id}" }
                    ) { word ->
                        SwipeToDeleteWordItem(
                            word = word,
                            onClick = { viewModel.onWordSelected(word) },
                            onDelete = {
                                viewModel.onToggleBookmark(word)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Word removed from bookmarks")
                                }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }

    // Delete all confirmation dialog
    if (uiState.showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteAllDialog,
            title = { Text("Remove all") },
            text = { Text("Do you really want to remove all words from your bookmarks?") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onDeleteAllBookmarks
                ) {
                    Text("Delete", color = Red800)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteAllDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // Word Detail Bottom Sheet
    if (uiState.showWordDetail && uiState.selectedWord != null) {
        WordDetailSheet(
            word = uiState.selectedWord!!,
            onDismiss = viewModel::onWordDetailDismissed,
            onToggleBookmark = { word ->
                viewModel.onToggleBookmark(word)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        if (word.isBookmark) "Word removed from bookmarks"
                        else "Word added to bookmarks"
                    )
                }
            },
            onSpeak = viewModel::onSpeak,
            onCopy = { word ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("word", viewModel.onCopyWord(word))
                clipboard.setPrimaryClip(clip)
                scope.launch {
                    snackbarHostState.showSnackbar("Copied to clipboard")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteWordItem(
    word: Word,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Red800)
                    .padding(end = 16.dp),
                contentAlignment = androidx.compose.ui.Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        WordListItem(
            word = word,
            onClick = onClick
        )
    }
}
