package info.sorbon.hskvocabulary.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.domain.model.HskLevel
import info.sorbon.hskvocabulary.domain.model.Language
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import info.sorbon.hskvocabulary.domain.usecase.GetLearnedWordsPerLevelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LevelRating(
    val hskLevel: HskLevel,
    val learnedWordCount: Int = 0,
    val earnedStarCount: Int = 0
)

data class HomeUiState(
    val levelRatings: List<LevelRating> = HskLevel.entries.map { LevelRating(it) },
    val activeLanguage: Language = Language.English,
    val bookmarkCount: Int = 0,
    val showAds: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val userPreferences: UserPreferences,
    private val getLearnedWordsPerLevel: GetLearnedWordsPerLevelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeState()
        loadLearnedWords()
    }

    private fun loadLearnedWords() {
        viewModelScope.launch {
            val learnedMap = getLearnedWordsPerLevel()
            _uiState.value = _uiState.value.copy(
                levelRatings = HskLevel.entries.map { level ->
                    LevelRating(
                        hskLevel = level,
                        learnedWordCount = learnedMap[level.level] ?: 0
                    )
                }
            )
        }
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                userPreferences.activeLanguageCode,
                wordRepository.getBookmarkCount(),
                userPreferences.adsRemoved
            ) { langCode, bookmarks, adsRemoved ->
                Triple(langCode, bookmarks, adsRemoved)
            }.collect { (langCode, bookmarks, adsRemoved) ->
                _uiState.value = _uiState.value.copy(
                    activeLanguage = Language.fromCode(langCode) ?: Language.English,
                    bookmarkCount = bookmarks,
                    showAds = !adsRemoved
                )
            }
        }
    }
}
