package info.sorbon.hskvocabulary.domain.model

data class LeaderboardEntry(
    val userId: String,
    val nickname: String = "",
    val country: String = "",
    val platform: String = "",
    val totalCorrect: Int = 0,
    val totalDuration: Int = 0,
    val partsCount: Int = 0,
    val rank: Int = 0
)
