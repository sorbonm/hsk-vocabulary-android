package info.sorbon.hskvocabulary.domain.repository

import info.sorbon.hskvocabulary.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    fun getWordsByLevel(level: Int): Flow<List<Word>>
    fun getBookmarkedWords(): Flow<List<Word>>
    fun getBookmarkCount(): Flow<Int>
    suspend fun getWordsForQuiz(level: Int, startId: Int, limit: Int = 10): List<Word>
    suspend fun getWordsByLevelOnce(level: Int): List<Word>
    suspend fun toggleBookmark(wordId: Int, currentState: Boolean)
    suspend fun getWordsByIds(ids: List<Int>): List<Word>
    suspend fun getWordCount(): Int
    suspend fun preloadWords(onProgress: (Int, Int) -> Unit)
}
