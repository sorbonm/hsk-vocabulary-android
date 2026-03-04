package info.sorbon.hskvocabulary.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val activeLanguageCode: Flow<String> = dataStore.data.map {
        it[PreferencesKeys.CURRENT_LANGUAGE] ?: "en"
    }

    val isWordsPreloaded: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKeys.HSK_WORD_PRELOADED] ?: false
    }

    val adsRemoved: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKeys.REMOVED_ADS] ?: false
    }

    val downloadedLanguages: Flow<Set<String>> = dataStore.data.map {
        it[PreferencesKeys.DOWNLOADED_LANGUAGES] ?: setOf("en")
    }

    val lastQuizType: Flow<String?> = dataStore.data.map {
        it[PreferencesKeys.LAST_QUIZ_TYPE]
    }

    suspend fun getActiveLanguageCodeOnce(): String {
        return dataStore.data.first()[PreferencesKeys.CURRENT_LANGUAGE] ?: "en"
    }

    suspend fun setActiveLanguage(code: String) {
        dataStore.edit { it[PreferencesKeys.CURRENT_LANGUAGE] = code }
    }

    suspend fun setWordsPreloaded(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.HSK_WORD_PRELOADED] = value }
    }

    suspend fun setAdsRemoved(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.REMOVED_ADS] = value }
    }

    suspend fun addDownloadedLanguage(code: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.DOWNLOADED_LANGUAGES] ?: setOf("en")
            prefs[PreferencesKeys.DOWNLOADED_LANGUAGES] = current + code
        }
    }

    suspend fun setLastQuizType(type: String) {
        dataStore.edit { it[PreferencesKeys.LAST_QUIZ_TYPE] = type }
    }

    suspend fun setLanguageSuggestionShown(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.LANGUAGE_SUGGESTION_SHOWN] = value }
    }

    suspend fun isLanguageSuggestionShown(): Boolean {
        return dataStore.data.first()[PreferencesKeys.LANGUAGE_SUGGESTION_SHOWN] ?: false
    }

    // ── Profile / Leaderboard preferences ──

    val userIsPublic: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKeys.USER_IS_PUBLIC] ?: false
    }

    suspend fun setUserIsPublic(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.USER_IS_PUBLIC] = value }
    }

    val targetHskLevel: Flow<Int> = dataStore.data.map {
        it[PreferencesKeys.TARGET_LEVEL] ?: 0
    }

    suspend fun setTargetHskLevel(level: Int) {
        dataStore.edit { it[PreferencesKeys.TARGET_LEVEL] = level }
    }

    // ── Flashcard preferences ──

    val flashcardShowPinyin: Flow<Boolean> = dataStore.data.map {
        it[PreferencesKeys.FLASHCARD_SHOW_PINYIN] ?: true
    }

    suspend fun setFlashcardShowPinyin(value: Boolean) {
        dataStore.edit { it[PreferencesKeys.FLASHCARD_SHOW_PINYIN] = value }
    }

    suspend fun getFlashcardTimerSeconds(): Int {
        return dataStore.data.first()[PreferencesKeys.FLASHCARD_TIMER_SECONDS] ?: 5
    }

    suspend fun setFlashcardTimerSeconds(value: Int) {
        dataStore.edit { it[PreferencesKeys.FLASHCARD_TIMER_SECONDS] = value }
    }

    suspend fun getFlashcardIndex(level: Int): Int {
        return dataStore.data.first()[PreferencesKeys.flashcardIndexKey(level)] ?: 0
    }

    suspend fun setFlashcardIndex(level: Int, index: Int) {
        dataStore.edit { it[PreferencesKeys.flashcardIndexKey(level)] = index }
    }
}
