package info.sorbon.hskvocabulary.domain.model

data class QuizResult(
    val id: Long = 0,
    val quizType: String,
    val level: Int,
    val wordPart: Int,
    val rating: Double,
    val duration: Int,
    val correctAnswer: Int,
    val createDate: Long
)
