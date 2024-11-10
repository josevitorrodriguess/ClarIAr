package com.tril.clariar

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var imageReader: ImageReader

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED)
        val data: Intent? = intent?.getParcelableExtra("data")

        if (resultCode == Activity.RESULT_OK && data != null) {
            val mediaProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            startScreenCapture()
        } else {
            // Permissão não foi concedida; pare o serviço
            stopSelf()
        }

        return START_STICKY
    }

    private fun startScreenCapture() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                // Processar a imagem (por exemplo, converter para Bitmap, analisar, etc.)
                image.close()
            }
        }, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        imageReader.close()
        virtualDisplay.release()
        mediaProjection?.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}