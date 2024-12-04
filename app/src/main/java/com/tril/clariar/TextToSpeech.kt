package com.tril.clariar

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TextToSpeechHandler(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    init {
        // Initialize TextToSpeech with context and listener
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set the language to Brazilian Portuguese
            val result = tts?.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Linguagem não suportada")
            }else {
                tts?.setSpeechRate(1.2f)
            }

        } else {
            Log.e("TTS", "Inicialização falhou")
        }
    }

    /**
     * Speak the text provided.
     *
     * @param text Text to be spoken.
     */
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    /**
     * Check if TTS is speaking.
     *
     * @return True if TTS is speaking, false otherwise.
     */
    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }


    /**
     * Unlock TextToSpeech resources.
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }

    //Stops the current speech, keeping the initialized TTS for later use
    fun stop() {
        tts?.stop()
    }
}

/* How to use:

initialize the class in a context
    val ttsHandler = TextToSpeechHandler(this)

    call the speak method
    ttsHandler.speak("Hello, world!")

    release the TextToSpeech resources at the end of the loop

ex:
    override fun onDestroy() {
    ttsHandler.shutdown()
    super.onDestroy()
}

*/