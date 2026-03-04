package info.sorbon.hskvocabulary.domain.usecase

import info.sorbon.hskvocabulary.domain.repository.QuizResultRepository
import javax.inject.Inject

class GetLearnedWordsPerLevelUseCase @Inject constructor(
    private val quizResultRepository: QuizResultRepository
) {
    suspend operator fun invoke(): Map<Int, Int> {
        val allResults = quizResultRepository.getAllResultsOnce()
        return allResults.groupBy { it.level }
            .mapValues { (_, results) ->
                results.groupBy { it.wordPart }
                    .values
                    .sumOf { partResults ->
                        partResults.maxByOrNull { it.createDate }?.correctAnswer ?: 0
                    }
            }
    }
}
