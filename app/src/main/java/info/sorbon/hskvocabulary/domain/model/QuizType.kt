package info.sorbon.hskvocabulary.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import info.sorbon.hskvocabulary.presentation.theme.Blue500
import info.sorbon.hskvocabulary.presentation.theme.Blue800

enum class QuizType(val key: String) {
    MixAll("all"),
    ListenToGuess("listenToGuess"),
    LangChinese("langChinese"),
    ChineseLang("chineseLang");

    fun displayTitle(languageName: String): String = when (this) {
        MixAll -> "Mix all"
        ListenToGuess -> "Listen to guess"
        LangChinese -> "$languageName → Chinese"
        ChineseLang -> "Chinese → $languageName"
    }

    /** Icon for quiz type selection */
    val icon: ImageVector
        get() = when (this) {
            MixAll -> Icons.Default.Shuffle
            ListenToGuess -> Icons.Default.Hearing
            LangChinese -> Icons.Default.Translate
            ChineseLang -> Icons.Default.SwapHoriz
        }

    /** Color for quiz type card (matches iOS QuizType.color) */
    val color: Color
        get() = when (this) {
            MixAll -> Blue800
            ListenToGuess -> Blue500
            LangChinese -> Blue500
            ChineseLang -> Blue500
        }

    companion object {
        fun fromKey(key: String): QuizType? = entries.find { it.key == key }
    }
}
