package info.sorbon.hskvocabulary.presentation.levelwords

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.presentation.components.EmptyStateView
import info.sorbon.hskvocabulary.presentation.components.WordListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LevelWordsScreen(
    viewModel: LevelWordsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Context menu state
    var contextMenuWord by remember { mutableStateOf<Word?>(null) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.level.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text("中文, pinyin, translation") },
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) { }

            // Words List or Empty State
            if (uiState.sections.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                EmptyStateView(
                    title = "No results found",
                    subtitle = "No results found for \"${uiState.searchQuery}\""
                )
            } else if (uiState.sections.isEmpty()) {
                EmptyStateView(
                    title = "No words",
                    subtitle = "Words haven't been loaded yet"
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    uiState.sections.forEach { section ->
                        // Section Header (sticky)
                        stickyHeader(key = "header_${section.letter}") {
                            Text(
                                text = section.letter,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Words in section
                        items(
                            items = section.words,
                            key = { it.id }
                        ) { word ->
                            WordListItem(
                                word = word,
                                searchQuery = uiState.searchQuery,
                                onClick = { viewModel.onWordSelected(word) }
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
