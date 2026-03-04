package info.sorbon.hskvocabulary.domain.model

import androidx.compose.ui.graphics.Color
import info.sorbon.hskvocabulary.presentation.theme.Blue300
import info.sorbon.hskvocabulary.presentation.theme.Blue400
import info.sorbon.hskvocabulary.presentation.theme.Blue500
import info.sorbon.hskvocabulary.presentation.theme.Blue600
import info.sorbon.hskvocabulary.presentation.theme.Blue700
import info.sorbon.hskvocabulary.presentation.theme.Blue800

enum class HskLevel(
    val level: Int,
    val wordCount: Int,
    val wordStartId: Int
) {
    HSK1(1, 150, 1),
    HSK2(2, 150, 151),
    HSK3(3, 300, 301),
    HSK4(4, 600, 601),
    HSK5(5, 1300, 1201),
    HSK6(6, 2500, 2501);

    val title: String get() = "HSK $level"
    val maxStarCount: Int get() = wordCount / 3
    val subtitle: String get() = "$wordCount words"

    /** Background color matching iOS HSKEnum.backgroundColor */
    val backgroundColor: Color
        get() = when (this) {
            HSK1 -> Blue300
            HSK2 -> Blue400
            HSK3 -> Blue500
            HSK4 -> Blue600
            HSK5 -> Blue700
            HSK6 -> Blue800
        }

    /** Background image resource name (matching iOS quiz-hsk-level-X-background) */
    val backgroundImageName: String
        get() = "quiz_hsk_level_${level}_background"

    /** iOS online test URL schemes mapped to Android package names */
    val onlineTestPackageName: String
        get() = when (this) {
            HSK1 -> "info.sorbon.hsk_1_online_test"
            HSK2 -> "info.sorbon.hsk_2_online_test"
            HSK3 -> "info.sorbon.hsk_3_online_test"
            HSK4 -> "info.sorbon.hsk_4_online_test"
            HSK5 -> "info.sorbon.hsk_5_online_test"
            HSK6 -> "info.sorbon.hsk_6_online_test"
        }

    fun getLevelWordParts(partSize: Int = 10): Int = wordCount / partSize

    companion object {
        fun fromLevel(level: Int): HskLevel? = entries.find { it.level == level }
    }
}
