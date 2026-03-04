package info.sorbon.hskvocabulary.presentation.quiz.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.domain.model.QuizResult
import info.sorbon.hskvocabulary.domain.repository.QuizResultRepository
import info.sorbon.hskvocabulary.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HistorySection(val dateString: String, val results: List<QuizResult>)

@HiltViewModel
class QuizHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizResultRepository: QuizResultRepository
) : ViewModel() {

    private val filterLevel: Int? = savedStateHandle.get<Int>(Screen.QuizHistory.ARG_LEVEL)?.takeIf { it > 0 }
    private val filterQuizType: String? = savedStateHandle.get<String>(Screen.QuizHistory.ARG_QUIZ_TYPE)?.takeIf { it.isNotEmpty() }

    private val _sections = MutableStateFlow<List<HistorySection>>(emptyList())
    val sections: StateFlow<List<HistorySection>> = _sections.asStateFlow()

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    init { loadHistory() }

    private fun loadHistory() {
        viewModelScope.launch {
            var all = quizResultRepository.getAllResultsOnce().sortedByDescending { it.createDate }
            if (filterLevel != null) {
                all = all.filter { it.level == filterLevel }
            }
            if (filterQuizType != null) {
                all = all.filter { it.quizType == filterQuizType }
            }
            val grouped = all.groupBy { dateFormat.format(Date(it.createDate)) }
            _sections.value = grouped.map { (date, results) -> HistorySection(date, results) }
        }
    }
}
