package com.tril.clariar

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TextToSpeechHandler(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    init {
        // Inicializa o TextToSpeech com o contexto e o listener
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Define o idioma para português do Brasil
            val result = tts?.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Linguagem não suportada")
            }
        } else {
            Log.e("TTS", "Inicialização falhou")
        }
    }

    /**
     * Fala o texto fornecido.
     *
     * @param text Texto a ser falado.
     */
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    /**
     * Libera os recursos do TextToSpeech.
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

/*Como usar:
inicializar a classe em um contexto:
    val ttsHandler = TextToSpeechHandler(this)

chamar o método speak
    ttsHandler.speak("Olá, mundo!")

liberar os recursos do TextToSpeech no final do ciclo

ex:
    override fun onDestroy() {
        ttsHandler.shutdown()
        super.onDestroy()
    }

*/