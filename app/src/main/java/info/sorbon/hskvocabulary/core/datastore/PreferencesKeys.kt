package info.sorbon.hskvocabulary.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object PreferencesKeys {
    val CURRENT_LANGUAGE = stringPreferencesKey("current_language")
    val HSK_WORD_PRELOADED = booleanPreferencesKey("hsk_word_preloaded")
    val REMOVED_ADS = booleanPreferencesKey("removed_ads")
    val USER_SIGN_IN = booleanPreferencesKey("user_sign_in")
    val USER_NICKNAME = stringPreferencesKey("user_nickname")
    val USER_COUNTRY = stringPreferencesKey("user_country")
    val USER_IS_PUBLIC = booleanPreferencesKey("user_is_public")
    val LEADERBOARD_PROFILE_SET = booleanPreferencesKey("leaderboard_profile_set")
    val LAST_QUIZ_TYPE = stringPreferencesKey("last_quiz_type")
    val TARGET_LEVEL = intPreferencesKey("target_hsk_level")
    val LANGUAGE_SUGGESTION_SHOWN = booleanPreferencesKey("language_suggestion_shown")
    val DOWNLOADED_LANGUAGES = stringSetPreferencesKey("downloaded_languages")
    val CACHED_FCM_TOKEN = stringPreferencesKey("cached_fcm_token")
    val DATA_VERSION = intPreferencesKey("data_version")
    val FLASHCARD_SHOW_PINYIN = booleanPreferencesKey("flashcard_show_pinyin")
    val FLASHCARD_TIMER_SECONDS = intPreferencesKey("flashcard_timer_seconds")
    fun flashcardIndexKey(level: Int) = intPreferencesKey("flashcard_level_${level}_index")
}
