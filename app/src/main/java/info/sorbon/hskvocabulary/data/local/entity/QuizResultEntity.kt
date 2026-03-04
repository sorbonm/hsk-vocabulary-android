package info.sorbon.hskvocabulary.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "quiz_type") val quizType: String,
    val level: Int,
    @ColumnInfo(name = "word_part") val wordPart: Int,
    val rating: Double,
    val duration: Int,
    @ColumnInfo(name = "correct_answer") val correctAnswer: Int,
    @ColumnInfo(name = "create_date") val createDate: Long
)
