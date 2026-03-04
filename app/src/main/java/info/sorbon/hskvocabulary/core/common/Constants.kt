package info.sorbon.hskvocabulary.core.common

object Constants {
    const val QUIZ_QUESTION_COUNT = 10
    const val QUIZ_OPTIONS_COUNT = 4
    const val LANGUAGE_PACK_BATCH_SIZE = 500

    object AdMob {
        const val BANNER_HOME = "ca-app-pub-4956851260856444/XXXXXXXXXX" // TODO: Replace with actual ad unit ID
        const val INTERSTITIAL_QUIZ = "ca-app-pub-4956851260856444/XXXXXXXXXX" // TODO: Replace with actual ad unit ID
    }

    object Firebase {
        const val WORDS_PATH = "words"
        const val TRANSLATIONS_PATH = "translations"
        const val USERS_COLLECTION = "users"
        const val QUIZ_AGGREGATES_COLLECTION = "quizLevelAggregates"
        const val USER_COUNTER_DOCUMENT = "counters/userIDCounter"
    }

    object IAP {
        const val REMOVE_ADS_PRODUCT_ID = "hsk_vocabulary.remove_ads"
    }
}
