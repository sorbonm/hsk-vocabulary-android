package info.sorbon.hskvocabulary.domain.model

data class Word(
    val id: Int,
    val level: Int,
    val hanzi: String,
    val tradHanzi: String,
    val pinyin: String,
    val definition: String,
    val localizedDefinition: String,
    val cl: String,
    val example: String,
    val sectionTitle: String,
    val isBookmark: Boolean
)
