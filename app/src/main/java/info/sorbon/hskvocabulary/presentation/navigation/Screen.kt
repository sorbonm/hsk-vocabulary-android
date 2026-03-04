package info.sorbon.hskvocabulary.presentation.navigation

sealed class Screen(val route: String) {
    data object Preload : Screen("preload")
    data object Home : Screen("home")
    data object LevelWords : Screen("level_words/{level}") {
        fun createRoute(level: Int) = "level_words/$level"
        const val ARG_LEVEL = "level"
    }
    data object QuizType : Screen("quiz_type/{level}") {
        fun createRoute(level: Int) = "quiz_type/$level"
        const val ARG_LEVEL = "level"
    }
    data object QuizLevelPart : Screen("quiz_level_part/{level}/{quizType}") {
        fun createRoute(level: Int, quizType: String) = "quiz_level_part/$level/$quizType"
        const val ARG_LEVEL = "level"
        const val ARG_QUIZ_TYPE = "quizType"
    }
    data object Quiz : Screen("quiz/{level}/{part}/{startWordId}/{quizType}") {
        fun createRoute(level: Int, part: Int, startWordId: Int, quizType: String) =
            "quiz/$level/$part/$startWordId/$quizType"
        const val ARG_LEVEL = "level"
        const val ARG_PART = "part"
        const val ARG_START_WORD_ID = "startWordId"
        const val ARG_QUIZ_TYPE = "quizType"
    }
    data object QuizResult : Screen("quiz_result/{level}/{part}/{quizType}/{correct}/{total}/{duration}?incorrectIds={incorrectIds}") {
        fun createRoute(level: Int, part: Int, quizType: String, correct: Int, total: Int, duration: Int, incorrectIds: String = "") =
            "quiz_result/$level/$part/$quizType/$correct/$total/$duration?incorrectIds=$incorrectIds"
        const val ARG_INCORRECT_IDS = "incorrectIds"
    }
    data object QuizHistory : Screen("quiz_history?level={level}&quizType={quizType}") {
        fun createRoute(level: Int? = null, quizType: String? = null): String {
            val params = mutableListOf<String>()
            if (level != null) params.add("level=$level")
            if (quizType != null) params.add("quizType=$quizType")
            return if (params.isEmpty()) "quiz_history" else "quiz_history?${params.joinToString("&")}"
        }
        const val ARG_LEVEL = "level"
        const val ARG_QUIZ_TYPE = "quizType"
    }
    data object FlashcardMode : Screen("flashcard_mode/{level}") {
        fun createRoute(level: Int) = "flashcard_mode/$level"
        const val ARG_LEVEL = "level"
    }
    data object Flashcard : Screen("flashcard/{level}/{mode}") {
        fun createRoute(level: Int, mode: String) = "flashcard/$level/$mode"
        const val ARG_LEVEL = "level"
        const val ARG_MODE = "mode"
    }
    data object Leaderboard : Screen("leaderboard")
    data object Profile : Screen("profile")
    data object LanguageSelection : Screen("language_selection")
    data object PracticeLevel : Screen("practice_level/{type}") {
        fun createRoute(type: String) = "practice_level/$type"
        const val ARG_TYPE = "type"
    }
    data object Bookmarks : Screen("bookmarks")
    data object Contacts : Screen("contacts")
}
