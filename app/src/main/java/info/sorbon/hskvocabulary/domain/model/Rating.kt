package info.sorbon.hskvocabulary.domain.model

enum class Rating(val score: Double, val description: String) {
    None(0.0, "Don't despair! Everything will work out!"),
    OneStar(1.0, "Not bad! More is better!"),
    TwoStar(2.0, "Good! Keep it up!"),
    ThreeStar(3.0, "Excellent! One step closer to your dream!");

    companion object {
        fun fromPercent(percent: Int): Rating = when {
            percent < 20 -> None
            percent in 20..40 -> OneStar
            percent in 41..80 -> TwoStar
            else -> ThreeStar
        }
    }
}
