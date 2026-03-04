package info.sorbon.hskvocabulary.presentation.quiz.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import info.sorbon.hskvocabulary.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordRepository: WordRepository
) : ViewModel() {

    private val incorrectIdsStr: String = savedStateHandle[Screen.QuizResult.ARG_INCORRECT_IDS] ?: ""

    private val _incorrectWords = MutableStateFlow<List<Word>>(emptyList())
    val incorrectWords: StateFlow<List<Word>> = _incorrectWords.asStateFlow()

    init {
        loadIncorrectWords()
    }

    private fun loadIncorrectWords() {
        val ids = incorrectIdsStr
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
        if (ids.isEmpty()) return

        viewModelScope.launch {
            _incorrectWords.value = wordRepository.getWordsByIds(ids)
        }
    }
}
