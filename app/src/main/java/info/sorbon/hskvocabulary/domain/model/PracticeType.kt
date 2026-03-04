package info.sorbon.hskvocabulary.domain.model

import androidx.annotation.DrawableRes
import info.sorbon.hskvocabulary.R

enum class PracticeType(
    val title: String,
    val subtitle: String,
    @DrawableRes val backgroundRes: Int
) {
    Quiz("Quiz", "Test yourself\nfor knowledge", R.drawable.ic_practice_flashcard),
    Flashcard("Flashcard", "Repeat the words for\nbetter memorization", R.drawable.ic_practice_quiz),
    OnlineTest("Online test", "Come tests and find\nout your chances", R.drawable.ic_practice_online_test)
}
