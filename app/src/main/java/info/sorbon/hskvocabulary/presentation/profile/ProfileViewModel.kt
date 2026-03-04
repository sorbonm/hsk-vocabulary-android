package info.sorbon.hskvocabulary.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.data.remote.firebase.AuthDataSource
import info.sorbon.hskvocabulary.data.remote.firebase.FirestoreDataSource
import info.sorbon.hskvocabulary.domain.model.HskLevel
import info.sorbon.hskvocabulary.domain.repository.QuizResultRepository
import info.sorbon.hskvocabulary.domain.usecase.GetLearnedWordsPerLevelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LevelProgress(
    val hskLevel: HskLevel,
    val learnedWords: Int = 0,
    val progressPercent: Float = 0f
)

data class RankingEntry(
    val hskLevel: HskLevel,
    val totalCorrect: Int = 0,
    val totalDuration: String = "0:00"
)

data class ProfileUiState(
    val nickname: String = "",
    val country: String = "",
    val levelProgress: List<LevelProgress> = HskLevel.entries.map { LevelProgress(it) },
    val bestRanking: RankingEntry? = null,
    val showInLeaderboard: Boolean = false,
    val targetHskLevel: Int = 0,
    val totalQuizzes: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
    private val quizResultRepository: QuizResultRepository,
    private val userPreferences: UserPreferences,
    private val getLearnedWordsPerLevel: GetLearnedWordsPerLevelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userPreferences.userIsPublic.collect { isPublic ->
                _uiState.value = _uiState.value.copy(showInLeaderboard = isPublic)
            }
        }
        viewModelScope.launch {
            userPreferences.targetHskLevel.collect { level ->
                _uiState.value = _uiState.value.copy(targetHskLevel = level)
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val uid = authDataSource.currentUserId ?: run {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            // Local data from Room
            val nickname = firestoreDataSource.generateNickname(uid)
            val allResults = quizResultRepository.getAllResultsOnce()

            // Progress per level using shared use case
            val learnedMap = getLearnedWordsPerLevel()
            val progress = HskLevel.entries.map { level ->
                val learnedWords = learnedMap[level.level] ?: 0
                LevelProgress(
                    hskLevel = level,
                    learnedWords = learnedWords,
                    progressPercent = if (level.wordCount > 0) learnedWords.toFloat() / level.wordCount else 0f
                )
            }

            // Best ranking: find level with highest totalCorrect
            val resultsByLevel = allResults.groupBy { it.level }
            val bestRanking = resultsByLevel.entries
                .map { (levelNum, results) ->
                    val hskLevel = HskLevel.fromLevel(levelNum) ?: return@map null
                    val totalCorrect = results.sumOf { it.correctAnswer }
                    val totalSeconds = results.sumOf { it.duration }
                    val mins = totalSeconds / 60
                    val secs = totalSeconds % 60
                    RankingEntry(
                        hskLevel = hskLevel,
                        totalCorrect = totalCorrect,
                        totalDuration = "$mins:${String.format("%02d", secs)}"
                    )
                }
                .filterNotNull()
                .maxByOrNull { it.totalCorrect }

            _uiState.value = _uiState.value.copy(
                nickname = nickname,
                totalQuizzes = allResults.size,
                levelProgress = progress,
                bestRanking = bestRanking,
                isLoading = false
            )

            // Fetch profile from Firestore (nickname/country)
            try {
                val profiles = firestoreDataSource.fetchUserProfiles(listOf(uid))
                val profile = profiles[uid]
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(
                        nickname = profile.nickname.ifBlank { nickname },
                        country = profile.country
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun updateProfile(newNickname: String, newCountry: String) {
        viewModelScope.launch {
            val uid = authDataSource.currentUserId ?: return@launch
            try {
                firestoreDataSource.saveUserProfile(uid, newNickname, newCountry)
                _uiState.value = _uiState.value.copy(nickname = newNickname, country = newCountry)
            } catch (_: Exception) { }
        }
    }

    fun setShowInLeaderboard(value: Boolean) {
        viewModelScope.launch {
            userPreferences.setUserIsPublic(value)
            val uid = authDataSource.currentUserId ?: return@launch
            try {
                firestoreDataSource.updateUserField(uid, "isPublic", value)
            } catch (_: Exception) { }
        }
    }

    fun setTargetHskLevel(level: Int) {
        viewModelScope.launch {
            userPreferences.setTargetHskLevel(level)
        }
    }
}
