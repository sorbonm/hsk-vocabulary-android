package info.sorbon.hskvocabulary.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import info.sorbon.hskvocabulary.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results ORDER BY create_date DESC")
    fun getAllResults(): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE level = :level ORDER BY create_date ASC")
    fun getResultsByLevel(level: Int): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE level = :level ORDER BY create_date ASC")
    suspend fun getResultsByLevelOnce(level: Int): List<QuizResultEntity>

    @Query("SELECT * FROM quiz_results")
    suspend fun getAllResultsOnce(): List<QuizResultEntity>

    @Insert
    suspend fun insert(result: QuizResultEntity)

    @Query("DELETE FROM quiz_results")
    suspend fun deleteAll()
}
