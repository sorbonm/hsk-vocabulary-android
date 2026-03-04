package info.sorbon.hskvocabulary.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import info.sorbon.hskvocabulary.data.local.entity.QuizResultEntity
import info.sorbon.hskvocabulary.data.local.entity.WordEntity

@Database(
    entities = [WordEntity::class, QuizResultEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HskDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun quizResultDao(): QuizResultDao
}
