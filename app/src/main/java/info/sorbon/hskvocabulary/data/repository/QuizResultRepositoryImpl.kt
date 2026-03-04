package info.sorbon.hskvocabulary.data.repository

import info.sorbon.hskvocabulary.data.local.db.QuizResultDao
import info.sorbon.hskvocabulary.data.local.entity.QuizResultEntity
import info.sorbon.hskvocabulary.domain.model.QuizResult
import info.sorbon.hskvocabulary.domain.repository.QuizResultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizResultRepositoryImpl @Inject constructor(
    private val quizResultDao: QuizResultDao
) : QuizResultRepository {

    override fun getAllResults(): Flow<List<QuizResult>> =
        quizResultDao.getAllResults().map { it.map { e -> e.toDomain() } }

    override fun getResultsByLevel(level: Int): Flow<List<QuizResult>> =
        quizResultDao.getResultsByLevel(level).map { it.map { e -> e.toDomain() } }

    override suspend fun getResultsByLevelOnce(level: Int): List<QuizResult> =
        quizResultDao.getResultsByLevelOnce(level).map { it.toDomain() }

    override suspend fun getAllResultsOnce(): List<QuizResult> =
        quizResultDao.getAllResultsOnce().map { it.toDomain() }

    override suspend fun insert(result: QuizResult) {
        quizResultDao.insert(result.toEntity())
    }
}

fun QuizResultEntity.toDomain() = QuizResult(
    id = id,
    quizType = quizType,
    level = level,
    wordPart = wordPart,
    rating = rating,
    duration = duration,
    correctAnswer = correctAnswer,
    createDate = createDate
)

fun QuizResult.toEntity() = QuizResultEntity(
    id = id,
    quizType = quizType,
    level = level,
    wordPart = wordPart,
    rating = rating,
    duration = duration,
    correctAnswer = correctAnswer,
    createDate = createDate
)
