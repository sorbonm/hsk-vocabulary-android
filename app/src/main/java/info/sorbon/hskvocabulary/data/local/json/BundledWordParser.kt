package info.sorbon.hskvocabulary.data.local.json

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import info.sorbon.hskvocabulary.data.local.entity.WordEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BundledWord(
    val id: Int,
    val level: Int,
    val pinyin: String,
    val hanzi: String,
    @SerialName("trad_hanzi") val tradHanzi: String,
    val definition: String,
    val example: String = "",
    val cl: String = ""
)

@Singleton
class BundledWordParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(): List<WordEntity> {
        val jsonString = context.assets.open("preload-hsk-words.json")
            .bufferedReader()
            .use { it.readText() }

        val words = json.decodeFromString<List<BundledWord>>(jsonString)

        return words.map { w ->
            WordEntity(
                id = w.id,
                level = w.level,
                hanzi = w.hanzi,
                tradHanzi = w.tradHanzi,
                pinyin = w.pinyin,
                definition = w.definition,
                cl = w.cl,
                example = w.example,
                sectionTitle = sectionTitle(w.pinyin)
            )
        }
    }

    private fun sectionTitle(pinyin: String): String {
        val first = pinyin.firstOrNull() ?: return "#"
        return if (first.isLetter()) first.uppercaseChar().toString() else "#"
    }
}
