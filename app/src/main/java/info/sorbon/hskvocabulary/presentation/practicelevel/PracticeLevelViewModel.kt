package info.sorbon.hskvocabulary.presentation.practicelevel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.domain.model.HskLevel
import info.sorbon.hskvocabulary.domain.model.Language
import info.sorbon.hskvocabulary.domain.repository.QuizResultRepository
import info.sorbon.hskvocabulary.domain.usecase.GetLearnedWordsPerLevelUseCase
import info.sorbon.hskvocabulary.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PracticeLevelItem(
    val hskLevel: HskLevel,
    val learnedWords: Int = 0,
    val earnedStars: Int = 0
)

data class PracticeLevelUiState(
    val title: String = "",
    val practiceType: String = "quiz",
    val levels: List<PracticeLevelItem> = emptyList(),
    val activeLanguage: Language = Language.English
)

@HiltViewModel
class PracticeLevelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizResultRepository: QuizResultRepository,
    private val userPreferences: UserPreferences,
    private val getLearnedWordsPerLevel: GetLearnedWordsPerLevelUseCase
) : ViewModel() {

    private val practiceType: String = savedStateHandle[Screen.PracticeLevel.ARG_TYPE] ?: "quiz"

    private val _uiState = MutableStateFlow(
        PracticeLevelUiState(
            title = when (practiceType) {
                "quiz" -> "Quiz"
                "flashcard" -> "Flashcard"
                "online_test" -> "Online Test"
                else -> ""
            },
            practiceType = practiceType
        )
    )
    val uiState: StateFlow<PracticeLevelUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val langCode = userPreferences.getActiveLanguageCodeOnce()
            val language = Language.fromCode(langCode) ?: Language.English

            val levels = when (practiceType) {
                "quiz" -> loadQuizLevels()
                else -> HskLevel.entries.map { PracticeLevelItem(hskLevel = it) }
            }

            _uiState.value = _uiState.value.copy(
                levels = levels,
                activeLanguage = language
            )
        }
    }

    private suspend fun loadQuizLevels(): List<PracticeLevelItem> {
        val allResults = quizResultRepository.getAllResultsOnce()
        val learnedMap = getLearnedWordsPerLevel()

        val resultsByLevel = allResults.groupBy { it.level }

        return HskLevel.entries.map { hskLevel ->
            val levelResults = resultsByLevel[hskLevel.level].orEmpty()
            val earnedStars = levelResults.groupBy { it.wordPart }
                .values
                .sumOf { partResults ->
                    partResults.maxByOrNull { it.createDate }?.rating?.toInt() ?: 0
                }
            PracticeLevelItem(
                hskLevel = hskLevel,
                learnedWords = learnedMap[hskLevel.level] ?: 0,
                earnedStars = earnedStars
            )
        }
    }
}
