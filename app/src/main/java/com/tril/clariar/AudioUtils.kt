package com.tril.clariar

import android.media.AudioManager
import android.media.ToneGenerator

class AudioUtils {
    fun bipSong(){
        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)

        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 200)

        toneGenerator.release()
    }
}