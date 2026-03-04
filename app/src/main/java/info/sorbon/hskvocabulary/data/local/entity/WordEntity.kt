package info.sorbon.hskvocabulary.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: Int,
    val level: Int,
    val hanzi: String,
    @ColumnInfo(name = "trad_hanzi") val tradHanzi: String,
    val pinyin: String,
    val definition: String,
    val cl: String,
    val example: String,
    @ColumnInfo(name = "section_title") val sectionTitle: String,
    @ColumnInfo(name = "is_bookmark") val isBookmark: Boolean = false,
    @ColumnInfo(name = "definition_ru") val definitionRu: String? = null,
    @ColumnInfo(name = "definition_tj") val definitionTj: String? = null,
    @ColumnInfo(name = "definition_de") val definitionDe: String? = null,
    @ColumnInfo(name = "definition_fr") val definitionFr: String? = null,
    @ColumnInfo(name = "definition_jp") val definitionJp: String? = null,
    @ColumnInfo(name = "definition_ar") val definitionAr: String? = null,
    @ColumnInfo(name = "definition_es") val definitionEs: String? = null,
    @ColumnInfo(name = "definition_it") val definitionIt: String? = null,
    @ColumnInfo(name = "definition_km") val definitionKm: String? = null,
    @ColumnInfo(name = "definition_ko") val definitionKo: String? = null,
    @ColumnInfo(name = "definition_pt") val definitionPt: String? = null,
    @ColumnInfo(name = "definition_th") val definitionTh: String? = null,
    @ColumnInfo(name = "definition_vi") val definitionVi: String? = null
)
