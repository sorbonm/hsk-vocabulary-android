package info.sorbon.hskvocabulary.core.util

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.SIMPLIFIED_CHINESE
                isReady = true
            }
        }
    }

    fun speak(text: String, locale: Locale = Locale.SIMPLIFIED_CHINESE) {
        if (isReady) {
            tts?.language = locale
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
        }
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
