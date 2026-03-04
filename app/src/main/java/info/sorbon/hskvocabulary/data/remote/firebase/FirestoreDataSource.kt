package info.sorbon.hskvocabulary.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import info.sorbon.hskvocabulary.data.local.db.QuizResultDao
import info.sorbon.hskvocabulary.domain.model.LeaderboardEntry
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val quizResultDao: QuizResultDao
) {
    private val db = Firebase.firestore

    // ---- Nickname Generation (matching iOS DJB2 hash) ----

    fun generateNickname(uid: String): String {
        var hash: ULong = 5381u
        for (byte in uid.encodeToByteArray()) {
            hash = ((hash shl 5) + hash) + byte.toUByte().toULong()
        }
        val sixDigits = (hash % 900_000u).toInt() + 100_000
        return "User#$sixDigits"
    }

    // ---- Leaderboard ----

    /**
     * Recalculates quizLevelAggregates/{uid}_{level} from local Room data.
     * Called after every quiz. Uses Room as source of truth for best results per part.
     * Cost: 0 reads + 1 write (only when aggregate changes).
     * Matches iOS FirestoreManager.saveBestResult logic.
     */
    suspend fun saveBestResult(userId: String, level: Int) {
        val allResults = quizResultDao.getResultsByLevelOnce(level)

        // Group by part, find best per part
        val bestByPart = mutableMapOf<Int, Pair<Int, Int>>() // part -> (correct, duration)
        for (result in allResults) {
            val part = result.wordPart
            val correct = result.correctAnswer
            val duration = result.duration
            val existing = bestByPart[part]
            if (existing == null ||
                correct > existing.first ||
                (correct == existing.first && duration < existing.second)
            ) {
                bestByPart[part] = Pair(correct, duration)
            }
        }

        if (bestByPart.isEmpty()) return

        var totalCorrect = 0
        var totalDuration = 0
        for ((_, best) in bestByPart) {
            totalCorrect += best.first
            totalDuration += best.second
        }

        val docId = "${userId}_$level"
        db.collection("quizLevelAggregates").document(docId).set(
            mapOf(
                "userId" to userId,
                "level" to level,
                "totalCorrect" to totalCorrect,
                "totalDuration" to totalDuration,
                "partsCount" to bestByPart.size,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    /**
     * Reads pre-aggregated leaderboard from quizLevelAggregates collection.
     * Cost: 1 query (~100 docs max).
     */
    suspend fun fetchLevelLeaderboard(level: Int, limit: Long = 100): List<LeaderboardEntry> {
        val snapshot = db.collection("quizLevelAggregates")
            .whereEqualTo("level", level)
            .orderBy("totalCorrect", Query.Direction.DESCENDING)
            .orderBy("totalDuration", Query.Direction.ASCENDING)
            .limit(limit)
            .get().await()

        return snapshot.documents.mapIndexed { index, doc ->
            LeaderboardEntry(
                userId = doc.getString("userId") ?: "",
                nickname = "",
                country = "",
                totalCorrect = doc.getLong("totalCorrect")?.toInt() ?: 0,
                totalDuration = doc.getLong("totalDuration")?.toInt() ?: 0,
                partsCount = doc.getLong("partsCount")?.toInt() ?: 0,
                rank = index + 1
            )
        }
    }

    /**
     * Fetches user's quizLevelAggregates (1 query, max 6 docs).
     * Used in ProfileScreen.
     */
    suspend fun fetchUserLevelAggregates(userId: String): List<Map<String, Any>> {
        val snapshot = db.collection("quizLevelAggregates")
            .whereEqualTo("userId", userId)
            .get().await()
        return snapshot.documents.mapNotNull { it.data }
    }

    // ---- User Profile ----

    /**
     * Creates initial user profile on first sign-in.
     * Skips if profile already exists (1 read, 0 writes for existing users).
     * Matches iOS FirestoreManager.createInitialUserProfile.
     */
    suspend fun createInitialUserProfile(userId: String, nickname: String, country: String) {
        val ref = db.collection("users").document(userId)
        val snapshot = ref.get().await()
        if (snapshot.exists() && snapshot.getString("nickname") != null) return

        val data = mutableMapOf<String, Any>(
            "nickname" to nickname,
            "country" to country,
            "platform" to "android",
            "isPublic" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        // Include FCM token if already available
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            data["fcmToken"] = token
        } catch (_: Exception) { }

        ref.set(data).await()
    }

    suspend fun saveUserProfile(userId: String, nickname: String, country: String) {
        db.collection("users").document(userId).set(
            mapOf(
                "nickname" to nickname,
                "country" to country,
                "platform" to "android",
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    data class UserProfile(val nickname: String, val country: String, val platform: String)

    suspend fun fetchUserProfiles(userIds: List<String>): Map<String, UserProfile> {
        if (userIds.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, UserProfile>()
        userIds.distinct().forEach { uid ->
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    result[uid] = UserProfile(
                        nickname = doc.getString("nickname") ?: "User",
                        country = doc.getString("country") ?: "",
                        platform = doc.getString("platform") ?: ""
                    )
                }
            } catch (_: Exception) { }
        }
        return result
    }

    suspend fun updateUserField(userId: String, field: String, value: Any) {
        db.collection("users").document(userId).update(
            field, value,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun saveFcmToken(userId: String, token: String) {
        db.collection("users").document(userId).set(
            mapOf(
                "fcmToken" to token,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }
}
