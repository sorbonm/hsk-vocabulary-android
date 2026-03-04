package info.sorbon.hskvocabulary.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.sorbon.hskvocabulary.data.repository.QuizResultRepositoryImpl
import info.sorbon.hskvocabulary.data.repository.WordRepositoryImpl
import info.sorbon.hskvocabulary.domain.repository.QuizResultRepository
import info.sorbon.hskvocabulary.domain.repository.WordRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWordRepository(impl: WordRepositoryImpl): WordRepository

    @Binds
    @Singleton
    abstract fun bindQuizResultRepository(impl: QuizResultRepositoryImpl): QuizResultRepository
}
