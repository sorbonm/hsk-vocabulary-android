package info.sorbon.hskvocabulary.domain.model

data class QuizQuestion(
    val question: Word,
    val options: List<Word>,
    val langCode: String,
    var answered: Word? = null
) {
    val isCorrect: Boolean
        get() = answered?.id == question.id
}
