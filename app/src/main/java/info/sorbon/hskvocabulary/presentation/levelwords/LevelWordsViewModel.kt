package info.sorbon.hskvocabulary.presentation.levelwords

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.util.TtsManager
import info.sorbon.hskvocabulary.domain.model.HskLevel
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import info.sorbon.hskvocabulary.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordSection(
    val letter: String,
    val words: List<Word>
)

data class LevelWordsUiState(
    val level: HskLevel = HskLevel.HSK1,
    val sections: List<WordSection> = emptyList(),
    val allWords: List<Word> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedWord: Word? = null,
    val showWordDetail: Boolean = false
)

@HiltViewModel
class LevelWordsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val levelArg: Int = savedStateHandle[Screen.LevelWords.ARG_LEVEL] ?: 1

    private val _uiState = MutableStateFlow(LevelWordsUiState())
    val uiState: StateFlow<LevelWordsUiState> = _uiState.asStateFlow()

    init {
        val hskLevel = HskLevel.fromLevel(levelArg) ?: HskLevel.HSK1
        _uiState.value = _uiState.value.copy(level = hskLevel)
        observeWords(levelArg)
    }

    private fun observeWords(level: Int) {
        viewModelScope.launch {
            wordRepository.getWordsByLevel(level).collectLatest { words ->
                _uiState.value = _uiState.value.copy(allWords = words)
                applyFilter(words, _uiState.value.searchQuery)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilter(_uiState.value.allWords, query)
    }

    fun onSearchActiveChanged(active: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchActive = active)
        if (!active) {
            onSearchQueryChanged("")
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

    fun onSpeak(text: String) {
        ttsManager.speak(text)
    }

    fun onCopyWord(word: Word): String {
        return "${word.hanzi} ${word.pinyin}\n${word.localizedDefinition}"
    }

    private fun applyFilter(words: List<Word>, query: String) {
        val filtered = if (query.isBlank()) {
            words
        } else {
            val q = query.lowercase()
            words.filter { word ->
                word.hanzi.lowercase().contains(q) ||
                word.pinyin.lowercase().contains(q) ||
                word.definition.lowercase().contains(q) ||
                word.localizedDefinition.lowercase().contains(q)
            }
        }

        val sections = filtered
            .groupBy { it.sectionTitle.uppercase().firstOrNull()?.toString() ?: "#" }
            .toSortedMap()
            .map { (letter, sectionWords) ->
                WordSection(letter = letter, words = sectionWords)
            }

        _uiState.value = _uiState.value.copy(sections = sections)
    }
}
