package info.sorbon.hskvocabulary.presentation.navigation

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import info.sorbon.hskvocabulary.core.ads.InterstitialAdManager
import info.sorbon.hskvocabulary.core.ads.RewardedAdManager
import info.sorbon.hskvocabulary.presentation.bookmarks.BookmarksScreen
import info.sorbon.hskvocabulary.presentation.contacts.ContactsScreen
import info.sorbon.hskvocabulary.presentation.flashcard.FlashcardModeScreen
import info.sorbon.hskvocabulary.presentation.flashcard.FlashcardScreen
import info.sorbon.hskvocabulary.presentation.home.HomeScreen
import info.sorbon.hskvocabulary.presentation.language.LanguageSelectionScreen
import info.sorbon.hskvocabulary.presentation.leaderboard.LeaderboardScreen
import info.sorbon.hskvocabulary.presentation.levelwords.LevelWordsScreen
import info.sorbon.hskvocabulary.presentation.practicelevel.PracticeLevelScreen
import info.sorbon.hskvocabulary.presentation.preload.PreloadScreen
import info.sorbon.hskvocabulary.presentation.profile.ProfileScreen
import info.sorbon.hskvocabulary.presentation.quiz.history.QuizHistoryScreen
import info.sorbon.hskvocabulary.presentation.quiz.quiz.QuizScreen
import info.sorbon.hskvocabulary.presentation.quiz.quizlevelpart.QuizLevelPartScreen
import info.sorbon.hskvocabulary.presentation.quiz.result.QuizResultScreen

@Composable
fun HskNavHost(
    navController: NavHostController,
    startDestination: String,
    interstitialAdManager: InterstitialAdManager,
    rewardedAdManager: RewardedAdManager
) {
    val activity = LocalContext.current as Activity

    var showRewardDialog by remember { mutableStateOf(false) }
    var onRewardConfirm by remember { mutableStateOf<(() -> Unit)?>(null) }

    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            title = { Text("Watch a short video") },
            text = { Text("To retake this part, please watch a short video.") },
            confirmButton = {
                TextButton(onClick = {
                    showRewardDialog = false
                    onRewardConfirm?.let { action ->
                        rewardedAdManager.show(activity) { action() }
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRewardDialog = false }) { Text("Cancel") }
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ── Preload ──
        composable(Screen.Preload.route) {
            PreloadScreen(
                onPreloadComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Preload.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLevel = { level ->
                    navController.navigate(Screen.LevelWords.createRoute(level))
                },
                onNavigateToPracticeLevel = { type ->
                    navController.navigate(Screen.PracticeLevel.createRoute(type))
                },
                onNavigateToBookmarks = {
                    navController.navigate(Screen.Bookmarks.route)
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToLanguage = {
                    navController.navigate(Screen.LanguageSelection.route)
                },
                onNavigateToContacts = {
                    navController.navigate(Screen.Contacts.route)
                }
            )
        }

        // ── Practice Level Selection ──
        composable(
            route = Screen.PracticeLevel.route,
            arguments = listOf(navArgument(Screen.PracticeLevel.ARG_TYPE) { type = NavType.StringType })
        ) {
            PracticeLevelScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuizLevelPart = { level, quizType ->
                    navController.navigate(Screen.QuizLevelPart.createRoute(level, quizType))
                },
                onNavigateToFlashcard = { level, mode ->
                    navController.navigate(Screen.Flashcard.createRoute(level, mode))
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onNavigateToQuizHistory = {
                    navController.navigate(Screen.QuizHistory.createRoute())
                }
            )
        }

        // ── Level Words ──
        composable(
            route = Screen.LevelWords.route,
            arguments = listOf(navArgument(Screen.LevelWords.ARG_LEVEL) { type = NavType.IntType })
        ) {
            LevelWordsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Bookmarks ──
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Quiz Level Part (select a part to start) ──
        composable(
            route = Screen.QuizLevelPart.route,
            arguments = listOf(
                navArgument(Screen.QuizLevelPart.ARG_LEVEL) { type = NavType.IntType },
                navArgument(Screen.QuizLevelPart.ARG_QUIZ_TYPE) { type = NavType.StringType }
            )
        ) {
            QuizLevelPartScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartQuiz = { level, part, startWordId, quizType, isRepeat ->
                    val navigate = {
                        navController.navigate(Screen.Quiz.createRoute(level, part, startWordId, quizType))
                    }
                    if (isRepeat) {
                        onRewardConfirm = { navigate() }
                        showRewardDialog = true
                    } else {
                        navigate()
                    }
                },
                onNavigateToHistory = { level ->
                    navController.navigate(Screen.QuizHistory.createRoute(level))
                }
            )
        }

        // ── Quiz (gameplay) ──
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument(Screen.Quiz.ARG_LEVEL) { type = NavType.IntType },
                navArgument(Screen.Quiz.ARG_PART) { type = NavType.IntType },
                navArgument(Screen.Quiz.ARG_START_WORD_ID) { type = NavType.IntType },
                navArgument(Screen.Quiz.ARG_QUIZ_TYPE) { type = NavType.StringType }
            )
        ) {
            QuizScreen(
                onFinish = { level, part, quizType, correct, total, duration, incorrectIds ->
                    val navigateToResult = {
                        navController.navigate(
                            Screen.QuizResult.createRoute(level, part, quizType, correct, total, duration, incorrectIds)
                        ) {
                            popUpTo(Screen.Quiz.route) { inclusive = true }
                        }
                    }
                    interstitialAdManager.show(activity) { navigateToResult() }
                },
                onClose = { navController.popBackStack() }
            )
        }

        // ── Quiz Result ──
        composable(
            route = Screen.QuizResult.route,
            arguments = listOf(
                navArgument("level") { type = NavType.IntType },
                navArgument("part") { type = NavType.IntType },
                navArgument("quizType") { type = NavType.StringType },
                navArgument("correct") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("duration") { type = NavType.IntType },
                navArgument(Screen.QuizResult.ARG_INCORRECT_IDS) {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt("level") ?: 1
            val part = backStackEntry.arguments?.getInt("part") ?: 1
            val quizType = backStackEntry.arguments?.getString("quizType") ?: "all"
            val correct = backStackEntry.arguments?.getInt("correct") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 10
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0

            QuizResultScreen(
                level = level,
                part = part,
                quizType = quizType,
                correct = correct,
                total = total,
                duration = duration,
                onRepeat = {
                    val hskLevel = info.sorbon.hskvocabulary.domain.model.HskLevel.fromLevel(level)
                    val startWordId = (hskLevel?.wordStartId ?: 1) + (part - 1) * 10
                    onRewardConfirm = {
                        navController.popBackStack()
                        navController.navigate(Screen.Quiz.createRoute(level, part, startWordId, quizType))
                    }
                    showRewardDialog = true
                },
                onComplete = {
                    // Pop QuizResult → back to QuizLevelPart
                    navController.popBackStack()
                },
                onNext = {
                    val hskLevel = info.sorbon.hskvocabulary.domain.model.HskLevel.fromLevel(level)
                    val maxParts = hskLevel?.getLevelWordParts() ?: 1
                    val nextPart = if (part < maxParts) part + 1 else 1
                    val nextStartWordId = (hskLevel?.wordStartId ?: 1) + (nextPart - 1) * 10
                    navController.popBackStack()
                    navController.navigate(Screen.Quiz.createRoute(level, nextPart, nextStartWordId, quizType))
                }
            )
        }

        // ── Quiz History ──
        composable(
            route = Screen.QuizHistory.route,
            arguments = listOf(
                navArgument(Screen.QuizHistory.ARG_LEVEL) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(Screen.QuizHistory.ARG_QUIZ_TYPE) {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            QuizHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Flashcard Mode ──
        composable(
            route = Screen.FlashcardMode.route,
            arguments = listOf(navArgument(Screen.FlashcardMode.ARG_LEVEL) { type = NavType.IntType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getInt(Screen.FlashcardMode.ARG_LEVEL) ?: 1
            FlashcardModeScreen(
                level = level,
                onSelectMode = { mode ->
                    navController.navigate(Screen.Flashcard.createRoute(level, mode))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Flashcard ──
        composable(
            route = Screen.Flashcard.route,
            arguments = listOf(
                navArgument(Screen.Flashcard.ARG_LEVEL) { type = NavType.IntType },
                navArgument(Screen.Flashcard.ARG_MODE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString(Screen.Flashcard.ARG_MODE) ?: "normal"
            FlashcardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNextLevel = { nextLevel ->
                    navController.popBackStack()
                    navController.navigate(Screen.Flashcard.createRoute(nextLevel, mode))
                }
            )
        }

        // ── Leaderboard ──
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Language Selection ──
        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Profile ──
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuizHistory = { level ->
                    navController.navigate(Screen.QuizHistory.createRoute(level = level))
                }
            )
        }

        // ── Contacts ──
        composable(Screen.Contacts.route) {
            ContactsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
