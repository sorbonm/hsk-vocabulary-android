package info.sorbon.hskvocabulary.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import info.sorbon.hskvocabulary.data.local.db.HskDatabase
import info.sorbon.hskvocabulary.data.local.db.QuizResultDao
import info.sorbon.hskvocabulary.data.local.db.WordDao
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HskDatabase =
        Room.databaseBuilder(
            context,
            HskDatabase::class.java,
            "hsk_vocabulary.db"
        ).build()

    @Provides
    fun provideWordDao(db: HskDatabase): WordDao = db.wordDao()

    @Provides
    fun provideQuizResultDao(db: HskDatabase): QuizResultDao = db.quizResultDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
