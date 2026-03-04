package info.sorbon.hskvocabulary.presentation.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.util.TtsManager
import info.sorbon.hskvocabulary.domain.model.HskLevel
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarkSection(
    val levelTitle: String,
    val words: List<Word>
)

data class BookmarksUiState(
    val sections: List<BookmarkSection> = emptyList(),
    val totalCount: Int = 0,
    val selectedWord: Word? = null,
    val showWordDetail: Boolean = false,
    val showDeleteAllDialog: Boolean = false
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        observeBookmarks()
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            wordRepository.getBookmarkedWords().collectLatest { words ->
                val sections = words
                    .groupBy { it.level }
                    .toSortedMap()
                    .map { (level, levelWords) ->
                        val hskLevel = HskLevel.fromLevel(level)
                        BookmarkSection(
                            levelTitle = hskLevel?.title ?: "LEVEL $level",
                            words = levelWords
                        )
                    }

                _uiState.value = _uiState.value.copy(
                    sections = sections,
                    totalCount = words.size
                )
            }
        }
    }

    fun onWordSelected(word: Word) {
        _uiState.value = _uiState.value.copy(
            selectedWord = word,
            showWordDetail = true
        )
    }

    fun onWordDetailDismissed() {
        _uiState.value = _uiState.value.copy(showWordDetail = false)
    }

    fun onToggleBookmark(word: Word) {
        viewModelScope.launch {
            wordRepository.toggleBookmark(word.id, word.isBookmark)
        }
    }

    fun onShowDeleteAllDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAllDialog = true)
    }

    fun onDismissDeleteAllDialog() {
        _uiState.value = _uiState.value.copy(showDeleteAllDialog = false)
    }

    fun onDeleteAllBookmarks() {
        viewModelScope.launch {
            _uiState.value.sections.flatMap { it.words }.forEach { word ->
                wordRepository.toggleBookmark(word.id, true)
            }
            _uiState.value = _uiState.value.copy(showDeleteAllDialog = false)
        }
    }

    fun onSpeak(text: String) {
        ttsManager.speak(text)
    }

    fun onCopyWord(word: Word): String {
        return "${word.hanzi} ${word.pinyin}\n${word.localizedDefinition}"
    }
}
