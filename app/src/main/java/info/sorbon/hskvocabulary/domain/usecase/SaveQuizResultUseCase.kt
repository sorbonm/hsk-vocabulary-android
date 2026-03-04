package info.sorbon.hskvocabulary.domain.usecase

import info.sorbon.hskvocabulary.data.remote.firebase.AuthDataSource
import info.sorbon.hskvocabulary.data.remote.firebase.FirestoreDataSource
import info.sorbon.hskvocabulary.domain.model.QuizResult
import info.sorbon.hskvocabulary.domain.repository.QuizResultRepository
import javax.inject.Inject

class SaveQuizResultUseCase @Inject constructor(
    private val quizResultRepository: QuizResultRepository,
    private val firestoreDataSource: FirestoreDataSource,
    private val authDataSource: AuthDataSource
) {
    suspend operator fun invoke(result: QuizResult) {
        // Save locally first
        quizResultRepository.insert(result)

        // Sync aggregated best result to Firestore (matches iOS saveBestResult pattern)
        val uid = authDataSource.currentUserId ?: return
        try {
            firestoreDataSource.saveBestResult(userId = uid, level = result.level)
        } catch (_: Exception) {
            // Non-critical: will sync next time
        }
    }
}
