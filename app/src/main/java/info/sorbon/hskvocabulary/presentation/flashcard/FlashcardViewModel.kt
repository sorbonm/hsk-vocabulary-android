package info.sorbon.hskvocabulary.presentation.flashcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.core.util.TtsManager
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import info.sorbon.hskvocabulary.presentation.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashcardUiState(
    val words: List<Word> = emptyList(),
    val currentIndex: Int = 0,
    val showTranslation: Boolean = false,
    val isTimerMode: Boolean = false,
    val timerSeconds: Int = 5,
    val timerCountdown: Int = 5,
    val progress: Float = 0f,
    val progressText: String = "0 / 0 words",
    val isLoaded: Boolean = false,
    val showPinyin: Boolean = true,
    val isCompleted: Boolean = false,
    val level: Int = 1
)

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository,
    private val ttsManager: TtsManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val levelArg: Int = savedStateHandle[Screen.Flashcard.ARG_LEVEL] ?: 1
    private val modeArg: String = savedStateHandle[Screen.Flashcard.ARG_MODE] ?: "normal"

    private val _uiState = MutableStateFlow(FlashcardUiState(isTimerMode = modeArg == "timer", level = levelArg))
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init { loadWords() }

    private fun loadWords() {
        viewModelScope.launch {
            val showPinyin = userPreferences.flashcardShowPinyin.first()
            val timerSeconds = userPreferences.getFlashcardTimerSeconds()
            val savedIndex = userPreferences.getFlashcardIndex(levelArg)

            val words = wordRepository.getWordsByLevelOnce(levelArg)
            if (words.isEmpty()) {
                _uiState.value = _uiState.value.copy(isLoaded = true, showPinyin = showPinyin)
                return@launch
            }

            val startIndex = savedIndex.coerceIn(0, words.size - 1)
            _uiState.value = _uiState.value.copy(
                words = words,
                currentIndex = startIndex,
                progressText = "${startIndex + 1} / ${words.size} words",
                progress = (startIndex + 1).toFloat() / words.size,
                isLoaded = true,
                showPinyin = showPinyin,
                timerSeconds = timerSeconds,
                timerCountdown = timerSeconds
            )
            if (_uiState.value.isTimerMode) startTimer()
        }
    }

    fun onSwipe(direction: SwipeDirection) {
        val state = _uiState.value
        if (state.currentIndex + 1 >= state.words.size) {
            // Completed all cards
            _uiState.value = state.copy(isCompleted = true, showTranslation = false)
            timerJob?.cancel()
            viewModelScope.launch { userPreferences.setFlashcardIndex(levelArg, 0) }
            return
        }
        val nextIndex = state.currentIndex + 1
        _uiState.value = state.copy(
            currentIndex = nextIndex,
            showTranslation = false,
            progress = (nextIndex + 1).toFloat() / state.words.size,
            progressText = "${nextIndex + 1} / ${state.words.size} words"
        )
        viewModelScope.launch { userPreferences.setFlashcardIndex(levelArg, nextIndex) }
        if (_uiState.value.isTimerMode) resetTimer()
    }

    fun toggleTranslation() {
        _uiState.value = _uiState.value.copy(showTranslation = !_uiState.value.showTranslation)
    }

    fun togglePinyin() {
        val newValue = !_uiState.value.showPinyin
        _uiState.value = _uiState.value.copy(showPinyin = newValue)
        viewModelScope.launch { userPreferences.setFlashcardShowPinyin(newValue) }
    }

    fun setTimerDuration(seconds: Int) {
        _uiState.value = _uiState.value.copy(timerSeconds = seconds, timerCountdown = seconds)
        viewModelScope.launch { userPreferences.setFlashcardTimerSeconds(seconds) }
        if (_uiState.value.isTimerMode) {
            timerJob?.cancel()
            startTimer()
        }
    }

    fun restartFlashcards() {
        val state = _uiState.value
        _uiState.value = state.copy(
            currentIndex = 0,
            isCompleted = false,
            showTranslation = false,
            progress = 1f / state.words.size,
            progressText = "1 / ${state.words.size} words"
        )
        viewModelScope.launch { userPreferences.setFlashcardIndex(levelArg, 0) }
        if (_uiState.value.isTimerMode) {
            resetTimer()
            startTimer()
        }
    }

    fun speak() {
        val word = currentWord() ?: return
        ttsManager.speak(word.hanzi)
    }

    fun toggleBookmark() {
        val word = currentWord() ?: return
        viewModelScope.launch {
            wordRepository.toggleBookmark(word.id, word.isBookmark)
            val updatedWords = _uiState.value.words.toMutableList()
            val idx = _uiState.value.currentIndex
            updatedWords[idx] = word.copy(isBookmark = !word.isBookmark)
            _uiState.value = _uiState.value.copy(words = updatedWords)
        }
    }

    fun currentWord(): Word? = _uiState.value.words.getOrNull(_uiState.value.currentIndex)

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _uiState.value
                val newCountdown = state.timerCountdown - 1
                if (newCountdown <= 0) {
                    onSwipe(SwipeDirection.Up)
                } else {
                    _uiState.value = state.copy(timerCountdown = newCountdown)
                }
            }
        }
    }

    private fun resetTimer() {
        _uiState.value = _uiState.value.copy(timerCountdown = _uiState.value.timerSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

enum class SwipeDirection { Left, Right, Up }
