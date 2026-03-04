package info.sorbon.hskvocabulary.domain.repository

import info.sorbon.hskvocabulary.domain.model.QuizResult
import kotlinx.coroutines.flow.Flow

interface QuizResultRepository {
    fun getAllResults(): Flow<List<QuizResult>>
    fun getResultsByLevel(level: Int): Flow<List<QuizResult>>
    suspend fun getResultsByLevelOnce(level: Int): List<QuizResult>
    suspend fun getAllResultsOnce(): List<QuizResult>
    suspend fun insert(result: QuizResult)
}
