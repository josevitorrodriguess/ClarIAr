package com.tril.clariar

import android.app.Activity
import android.app.PendingIntent
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
import androidx.core.app.NotificationCompat
import android.app.Notification

class ScreenCaptureService : Service() {

    companion object {
        const val CHANNEL_ID = "ScreenCaptureChannel"
        const val NOTIFICATION_ID = 2
    }

    private var mediaProjection: MediaProjection? = null
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var imageReader: ImageReader

    private fun startForegroundServiceWithNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Clariar está capturando sua tela")
            .setSmallIcon(R.drawable.iconwarning) //notification icon
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service in the foreground with a notification
        startForegroundServiceWithNotification()

        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED)
        val data: Intent? = intent?.getParcelableExtra("data")

        if (resultCode == Activity.RESULT_OK && data != null) {
            val mediaProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

            // Optional: Assign mediaProjection to MyAccessibilityService if needed.o
            MyAccessibilityService.mediaProjection = mediaProjection

            startScreenCapture()
        } else {
            // Permission not granted; terminate service
            stopForeground(true)
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
                // Process the image (e.g. convert to Bitmap, analyze, etc.)
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