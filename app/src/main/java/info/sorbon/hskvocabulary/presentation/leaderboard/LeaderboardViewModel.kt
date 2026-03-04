package info.sorbon.hskvocabulary.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.data.remote.firebase.AuthDataSource
import info.sorbon.hskvocabulary.data.remote.firebase.FirestoreDataSource
import info.sorbon.hskvocabulary.data.remote.firebase.RemoteConfigDataSource
import info.sorbon.hskvocabulary.domain.model.LeaderboardEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val selectedLevel: Int = 1,
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUserId: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val authDataSource: AuthDataSource,
    private val remoteConfigDataSource: RemoteConfigDataSource
) : ViewModel() {

    companion object {
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
        private val cache = mutableMapOf<Int, CachedLeaderboard>()

        private data class CachedLeaderboard(
            val timestamp: Long,
            val entries: List<LeaderboardEntry>
        )
    }

    private val _uiState = MutableStateFlow(LeaderboardUiState(currentUserId = authDataSource.currentUserId))
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init { loadLeaderboard(1) }

    fun selectLevel(level: Int) {
        _uiState.value = _uiState.value.copy(selectedLevel = level)
        loadLeaderboard(level)
    }

    private fun loadLeaderboard(level: Int) {
        // Return cached data if fresh enough
        val cached = cache[level]
        if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_DURATION_MS) {
            _uiState.value = _uiState.value.copy(entries = cached.entries, isLoading = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val limit = remoteConfigDataSource.leaderboardLimit
                val entries = firestoreDataSource.fetchLevelLeaderboard(level, limit)
                val userIds = entries.map { it.userId }
                val profiles = firestoreDataSource.fetchUserProfiles(userIds)
                val enriched = entries.map { entry ->
                    val profile = profiles[entry.userId]
                    entry.copy(
                        nickname = profile?.nickname ?: firestoreDataSource.generateNickname(entry.userId),
                        country = profile?.country ?: "",
                        platform = profile?.platform ?: ""
                    )
                }
                cache[level] = CachedLeaderboard(System.currentTimeMillis(), enriched)
                _uiState.value = _uiState.value.copy(entries = enriched, isLoading = false)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
