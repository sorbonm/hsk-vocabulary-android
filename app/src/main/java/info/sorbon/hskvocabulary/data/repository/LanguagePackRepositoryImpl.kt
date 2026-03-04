package info.sorbon.hskvocabulary.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.data.local.db.WordDao
import info.sorbon.hskvocabulary.data.remote.firebase.RealtimeDatabaseSource
import info.sorbon.hskvocabulary.domain.model.Language
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagePackRepositoryImpl @Inject constructor(
    private val realtimeDatabaseSource: RealtimeDatabaseSource,
    private val wordDao: WordDao,
    private val userPreferences: UserPreferences
) {
    suspend fun downloadLanguagePack(langCode: String, onProgress: ((Int, Int) -> Unit)? = null) {
        val translations = realtimeDatabaseSource.fetchTranslations(langCode)
        val total = translations.size
        var count = 0
        val columnName = "definition_$langCode"
        translations.entries.chunked(500).forEach { batch ->
            batch.forEach { (id, definition) ->
                val query = SimpleSQLiteQuery(
                    "UPDATE words SET `$columnName` = ? WHERE id = ?",
                    arrayOf(definition, id)
                )
                wordDao.execRawQuery(query)
                count++
            }
            onProgress?.invoke(count, total)
        }
        userPreferences.addDownloadedLanguage(langCode)
    }
}
