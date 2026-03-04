package info.sorbon.hskvocabulary.presentation.preload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PreloadUiState(
    val progress: Float = 0f,
    val loadedCount: Int = 0,
    val totalCount: Int = 5000,
    val isComplete: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PreloadViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreloadUiState())
    val uiState: StateFlow<PreloadUiState> = _uiState.asStateFlow()

    init {
        startPreload()
    }

    private fun startPreload() {
        viewModelScope.launch {
            try {
                wordRepository.preloadWords { loaded, total ->
                    _uiState.value = _uiState.value.copy(
                        progress = loaded.toFloat() / total,
                        loadedCount = loaded,
                        totalCount = total
                    )
                }
                userPreferences.setWordsPreloaded(true)
                _uiState.value = _uiState.value.copy(isComplete = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
