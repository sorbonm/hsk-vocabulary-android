package info.sorbon.hskvocabulary.data.remote.firebase

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeDatabaseSource @Inject constructor() {
    private val database = Firebase.database.reference

    suspend fun fetchTranslations(langCode: String): Map<Int, String> {
        val snapshot = database.child("translations/$langCode").get().await()
        val result = mutableMapOf<Int, String>()
        snapshot.children.forEach { child ->
            val id = child.child("id").getValue(Int::class.java) ?: return@forEach
            val definition = child.child("definition").getValue(String::class.java) ?: return@forEach
            result[id] = definition
        }
        return result
    }
}
