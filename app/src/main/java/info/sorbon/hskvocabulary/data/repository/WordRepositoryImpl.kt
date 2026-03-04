package info.sorbon.hskvocabulary.data.repository

import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.data.local.db.WordDao
import info.sorbon.hskvocabulary.data.local.entity.WordEntity
import info.sorbon.hskvocabulary.data.local.json.BundledWordParser
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val bundledWordParser: BundledWordParser,
    private val userPreferences: UserPreferences
) : WordRepository {

    override fun getWordsByLevel(level: Int): Flow<List<Word>> {
        return wordDao.getWordsByLevel(level).map { entities ->
            val langCode = userPreferences.getActiveLanguageCodeOnce()
            entities.map { it.toDomain(langCode) }
        }
    }

    override fun getBookmarkedWords(): Flow<List<Word>> {
        return wordDao.getBookmarkedWords().map { entities ->
            val langCode = userPreferences.getActiveLanguageCodeOnce()
            entities.map { it.toDomain(langCode) }
        }
    }

    override fun getBookmarkCount(): Flow<Int> = wordDao.getBookmarkCount()

    override suspend fun getWordsForQuiz(level: Int, startId: Int, limit: Int): List<Word> {
        val langCode = userPreferences.getActiveLanguageCodeOnce()
        return wordDao.getWordsForQuiz(level, startId, limit).map { it.toDomain(langCode) }
    }

    override suspend fun getWordsByLevelOnce(level: Int): List<Word> {
        val langCode = userPreferences.getActiveLanguageCodeOnce()
        return wordDao.getWordsByLevelOnce(level).map { it.toDomain(langCode) }
    }

    override suspend fun getWordsByIds(ids: List<Int>): List<Word> {
        if (ids.isEmpty()) return emptyList()
        val langCode = userPreferences.getActiveLanguageCodeOnce()
        return wordDao.getWordsByIds(ids).map { it.toDomain(langCode) }
    }

    override suspend fun toggleBookmark(wordId: Int, currentState: Boolean) {
        wordDao.updateBookmark(wordId, !currentState)
    }

    override suspend fun getWordCount(): Int = wordDao.getWordCount()

    override suspend fun preloadWords(onProgress: (Int, Int) -> Unit) {
        withContext(Dispatchers.IO) {
            val words = bundledWordParser.parse()
            val total = words.size
            val batchSize = 100
            words.chunked(batchSize).forEachIndexed { index, batch ->
                wordDao.insertAll(batch)
                onProgress((index + 1) * batchSize.coerceAtMost(total), total)
            }
        }
    }
}

fun WordEntity.toDomain(langCode: String): Word {
    val localizedDef = getDefinition(langCode)
    return Word(
        id = id,
        level = level,
        hanzi = hanzi,
        tradHanzi = tradHanzi,
        pinyin = pinyin,
        definition = definition,
        localizedDefinition = localizedDef,
        cl = cl,
        example = example,
        sectionTitle = sectionTitle,
        isBookmark = isBookmark
    )
}

fun WordEntity.getDefinition(langCode: String): String {
    if (langCode == "en") return definition
    val translated = when (langCode) {
        "ru" -> definitionRu
        "tj" -> definitionTj
        "de" -> definitionDe
        "fr" -> definitionFr
        "jp" -> definitionJp
        "ar" -> definitionAr
        "es" -> definitionEs
        "it" -> definitionIt
        "km" -> definitionKm
        "ko" -> definitionKo
        "pt" -> definitionPt
        "th" -> definitionTh
        "vi" -> definitionVi
        else -> null
    }
    return if (!translated.isNullOrBlank() && translated != "-") translated else definition
}
