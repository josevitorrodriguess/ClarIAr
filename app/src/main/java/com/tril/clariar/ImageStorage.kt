package com.tril.clariar

import android.graphics.Bitmap

object ImageStorage {
    // Variable to store the bitmap
    @Volatile
    var capturedBitmap: Bitmap? = null
        get() = synchronized(this) { field }
        set(value) {
            synchronized(this) { field = value }
        }

    // // Function to clear the bitmap from memory
    fun clearCapturedBitmap() {
        synchronized(this) {
            capturedBitmap?.recycle()
            capturedBitmap = null
        }
    }
}


