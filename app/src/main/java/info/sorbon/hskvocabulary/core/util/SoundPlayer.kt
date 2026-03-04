package info.sorbon.hskvocabulary.core.util

import android.content.Context
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import info.sorbon.hskvocabulary.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun playCorrect() {
        play(R.raw.correct_answer)
    }

    fun playIncorrect() {
        play(R.raw.incorrect_answer)
    }

    private fun play(resId: Int) {
        try {
            MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (_: Exception) {
            // Silently ignore playback errors
        }
    }
}
