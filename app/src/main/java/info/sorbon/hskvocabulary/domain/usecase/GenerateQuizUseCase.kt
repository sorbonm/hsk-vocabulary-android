package info.sorbon.hskvocabulary.domain.usecase

import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.domain.model.QuizQuestion
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import javax.inject.Inject

class GenerateQuizUseCase @Inject constructor(
    private val wordRepository: WordRepository,
    private val userPreferences: UserPreferences
) {
    suspend operator fun invoke(
        level: Int,
        startWordId: Int,
        questionCount: Int = 10
    ): List<QuizQuestion> {
        val words = wordRepository.getWordsForQuiz(level, startWordId, questionCount)
        if (words.size < 4) return emptyList()
        val langCode = userPreferences.getActiveLanguageCodeOnce()
        return words.map { questionWord ->
            val otherWords = words.filter { it.id != questionWord.id }.shuffled().take(3)
            val options = (listOf(questionWord) + otherWords).shuffled()
            QuizQuestion(
                question = questionWord,
                options = options,
                langCode = langCode,
                answered = null
            )
        }
    }
}
