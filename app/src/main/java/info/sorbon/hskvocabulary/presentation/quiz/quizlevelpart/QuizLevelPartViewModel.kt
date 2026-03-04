package info.sorbon.hskvocabulary.presentation.quiz.quizlevelpart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.domain.model.HskLevel
import info.sorbon.hskvocabulary.domain.model.Language
import info.sorbon.hskvocabulary.domain.model.QuizType
import info.sorbon.hskvocabulary.domain.repository.QuizResultRepository
import info.sorbon.hskvocabulary.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizLevelPart(
    val partNumber: Int,
    val startWordId: Int,
    val rating: Double = 0.0,
    val duration: Int = 0,
    val isOpen: Boolean = false,
    val isCurrent: Boolean = false
)

data class QuizLevelPartUiState(
    val level: HskLevel = HskLevel.HSK1,
    val quizType: QuizType = QuizType.MixAll,
    val languageName: String = "English",
    val parts: List<QuizLevelPart> = emptyList(),
    val learnedWords: Int = 0,
    val totalWords: Int = 0,
    val earnedStars: Int = 0,
    val maxStars: Int = 0,
    val totalTime: String = "0:00",
    val selectedPart: QuizLevelPart? = null
)

@HiltViewModel
class QuizLevelPartViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizResultRepository: QuizResultRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val levelArg: Int = savedStateHandle[Screen.QuizLevelPart.ARG_LEVEL] ?: 1
    private val quizTypeArg: String = savedStateHandle[Screen.QuizLevelPart.ARG_QUIZ_TYPE] ?: "all"

    private val _uiState = MutableStateFlow(QuizLevelPartUiState())
    val uiState: StateFlow<QuizLevelPartUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val hskLevel = HskLevel.fromLevel(levelArg) ?: HskLevel.HSK1
        val quizType = QuizType.entries.find { it.key == quizTypeArg } ?: QuizType.MixAll
        val partCount = hskLevel.getLevelWordParts()

        viewModelScope.launch {
            val langCode = userPreferences.getActiveLanguageCodeOnce()
            val language = Language.fromCode(langCode) ?: Language.English
            val results = quizResultRepository.getResultsByLevelOnce(levelArg)

            val bestByPart = results.groupBy { it.wordPart }
                .mapValues { (_, partResults) -> partResults.maxByOrNull { it.createDate } }

            var learnedWords = 0
            var earnedStars = 0
            var totalSeconds = results.sumOf { it.duration }

            val parts = (1..partCount).map { partNum ->
                val best = bestByPart[partNum]
                val rating = best?.rating ?: 0.0
                val duration = best?.duration ?: 0
                val isOpen = best != null && best.correctAnswer > 0
                learnedWords += best?.correctAnswer ?: 0
                earnedStars += rating.toInt()

                QuizLevelPart(
                    partNumber = partNum,
                    startWordId = hskLevel.wordStartId + (partNum - 1) * 10,
                    rating = rating,
                    duration = duration,
                    isOpen = isOpen,
                    isCurrent = false
                )
            }

            val currentIndex = parts.indexOfFirst { !it.isOpen }.let { if (it < 0) parts.size - 1 else it }
            val updatedParts = parts.mapIndexed { i, p -> p.copy(isCurrent = i == currentIndex) }

            val mins = totalSeconds / 60
            val secs = totalSeconds % 60

            _uiState.value = QuizLevelPartUiState(
                level = hskLevel,
                quizType = quizType,
                languageName = language.nameInEnglish,
                parts = updatedParts,
                learnedWords = learnedWords,
                totalWords = hskLevel.wordCount,
                earnedStars = earnedStars,
                maxStars = hskLevel.maxStarCount,
                totalTime = "$mins:${String.format("%02d", secs)}",
                selectedPart = updatedParts.getOrNull(currentIndex)
            )
        }
    }

    fun refreshData() {
        loadData()
    }

    fun selectPart(part: QuizLevelPart) {
        if (part.isOpen || part.isCurrent) {
            _uiState.value = _uiState.value.copy(selectedPart = part)
        }
    }
}
