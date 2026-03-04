package info.sorbon.hskvocabulary.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import info.sorbon.hskvocabulary.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY id ASC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE level = :level ORDER BY id ASC")
    fun getWordsByLevel(level: Int): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE level = :level AND id >= :startId ORDER BY id ASC LIMIT :limit")
    suspend fun getWordsForQuiz(level: Int, startId: Int, limit: Int = 10): List<WordEntity>

    @Query("SELECT * FROM words WHERE level = :level ORDER BY id ASC")
    suspend fun getWordsByLevelOnce(level: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE is_bookmark = 1 ORDER BY id ASC")
    fun getBookmarkedWords(): Flow<List<WordEntity>>

    @Query("SELECT COUNT(*) FROM words WHERE is_bookmark = 1")
    fun getBookmarkCount(): Flow<Int>

    @Query("UPDATE words SET is_bookmark = :isBookmark WHERE id = :wordId")
    suspend fun updateBookmark(wordId: Int, isBookmark: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("DELETE FROM words")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Query("SELECT * FROM words WHERE id IN (:ids) ORDER BY id ASC")
    suspend fun getWordsByIds(ids: List<Int>): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words WHERE level = :level")
    suspend fun getWordCountForLevel(level: Int): Int

    @RawQuery
    suspend fun execRawQuery(query: SupportSQLiteQuery): Int
}
