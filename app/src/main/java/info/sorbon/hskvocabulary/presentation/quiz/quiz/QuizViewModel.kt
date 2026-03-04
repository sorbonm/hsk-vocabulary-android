package info.sorbon.hskvocabulary.presentation.quiz.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.util.SoundPlayer
import info.sorbon.hskvocabulary.core.util.TtsManager
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.domain.model.Language
import info.sorbon.hskvocabulary.domain.model.QuizQuestion
import info.sorbon.hskvocabulary.domain.model.QuizResult
import info.sorbon.hskvocabulary.domain.model.QuizType
import info.sorbon.hskvocabulary.domain.model.Rating
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.domain.usecase.GenerateQuizUseCase
import info.sorbon.hskvocabulary.data.remote.firebase.RemoteConfigDataSource
import info.sorbon.hskvocabulary.domain.usecase.SaveQuizResultUseCase
import info.sorbon.hskvocabulary.presentation.navigation.Screen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AnswerState { Idle, Answered }

data class QuizUiState(
    val currentIndex: Int = 0,
    val totalCount: Int = 10,
    val progress: Float = 0f,
    val timerText: String = "0:00",
    val question: QuizQuestion? = null,
    val quizType: QuizType = QuizType.MixAll,
    val answerState: AnswerState = AnswerState.Idle,
    val selectedOptionId: Int? = null,
    val correctOptionId: Int? = null,
    val isFinished: Boolean = false,
    val correctCount: Int = 0,
    val duration: Int = 0,
    val level: Int = 1,
    val part: Int = 1,
    val incorrectWords: List<Word> = emptyList(),
    val error: String? = null,
    val languageName: String = ""
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val saveQuizResultUseCase: SaveQuizResultUseCase,
    private val userPreferences: UserPreferences,
    private val soundPlayer: SoundPlayer,
    private val ttsManager: TtsManager,
    private val remoteConfigDataSource: RemoteConfigDataSource
) : ViewModel() {

    private val levelArg: Int = savedStateHandle[Screen.Quiz.ARG_LEVEL] ?: 1
    private val partArg: Int = savedStateHandle[Screen.Quiz.ARG_PART] ?: 1
    private val startWordIdArg: Int = savedStateHandle[Screen.Quiz.ARG_START_WORD_ID] ?: 1
    private val quizTypeArg: String = savedStateHandle[Screen.Quiz.ARG_QUIZ_TYPE] ?: "all"

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var questions: List<QuizQuestion> = emptyList()
    private var currentIndex = 0
    private var correctCount = 0
    private var elapsedSeconds = 0
    private var timerJob: Job? = null
    private val incorrectWords = mutableListOf<Word>()

    init {
        val quizType = QuizType.entries.find { it.key == quizTypeArg } ?: QuizType.MixAll
        _uiState.value = _uiState.value.copy(quizType = quizType, level = levelArg, part = partArg)
        loadQuiz()
        startTimer()
        viewModelScope.launch {
            val langCode = userPreferences.getActiveLanguageCodeOnce()
            val language = Language.fromCode(langCode) ?: Language.English
            _uiState.value = _uiState.value.copy(languageName = language.nameInEnglish)
        }
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            questions = generateQuizUseCase(levelArg, startWordIdArg)
            if (questions.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "Not enough words")
            } else {
                _uiState.value = _uiState.value.copy(totalCount = questions.size)
                showQuestion()
            }
        }
    }

    private fun startTimer() {
        val maxSeconds = remoteConfigDataSource.quizMaxDurationSeconds.toInt()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds = (elapsedSeconds + 1).coerceAtMost(maxSeconds)
                val m = elapsedSeconds / 60
                val s = elapsedSeconds % 60
                _uiState.value = _uiState.value.copy(timerText = "$m:${String.format("%02d", s)}")
            }
        }
    }

    private fun showQuestion() {
        val q = questions[currentIndex]
        _uiState.value = _uiState.value.copy(
            currentIndex = currentIndex,
            progress = (currentIndex.toFloat()) / questions.size,
            question = q,
            answerState = AnswerState.Idle,
            selectedOptionId = null,
            correctOptionId = null
        )
        if (_uiState.value.quizType == QuizType.ListenToGuess ||
            (_uiState.value.quizType == QuizType.MixAll && currentIndex < 3)) {
            ttsManager.speak(q.question.hanzi)
        }
    }

    fun onAnswer(selectedWord: Word) {
        if (_uiState.value.answerState == AnswerState.Answered) return
        val current = questions[currentIndex]
        val isCorrect = selectedWord.id == current.question.id
        if (isCorrect) {
            correctCount++
            soundPlayer.playCorrect()
        } else {
            incorrectWords.add(current.question)
            soundPlayer.playIncorrect()
        }
        _uiState.value = _uiState.value.copy(
            answerState = AnswerState.Answered,
            selectedOptionId = selectedWord.id,
            correctOptionId = current.question.id
        )
        viewModelScope.launch {
            delay(750)
            nextQuestion()
        }
    }

    private fun nextQuestion() {
        if (currentIndex < questions.size - 1) {
            currentIndex++
            showQuestion()
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        timerJob?.cancel()
        val percent = (correctCount * 100) / questions.size
        val rating = Rating.fromPercent(percent)
        _uiState.value = _uiState.value.copy(
            isFinished = true,
            correctCount = correctCount,
            duration = elapsedSeconds,
            incorrectWords = incorrectWords.toList(),
            progress = 1f
        )
        viewModelScope.launch {
            saveQuizResultUseCase(
                QuizResult(
                    quizType = quizTypeArg,
                    level = levelArg,
                    wordPart = partArg,
                    rating = rating.score,
                    duration = elapsedSeconds,
                    correctAnswer = correctCount,
                    createDate = System.currentTimeMillis()
                )
            )
        }
    }

    fun speakCurrentQuestion() {
        _uiState.value.question?.let { ttsManager.speak(it.question.hanzi) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
