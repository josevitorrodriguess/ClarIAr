package com.tril.clariar

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MyAccessibilityService : AccessibilityService() {

    companion object {
        const val CHANNEL_ID = "ScreenCaptureChannel"
        const val NOTIFICATION_ID = 1

        var mediaProjection: MediaProjection? = null
    }

    // BroadcastReceiver to listen when the permission is granted
    private val permissionGrantedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Permission granted; show the notification
            showNotification()
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Register the receiver
        val filter = IntentFilter("com.tril.clariar.PERMISSION_GRANTED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 or higher
            registerReceiver(
                permissionGrantedReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(permissionGrantedReceiver, filter)
        }

        // Create the notification channel
        createNotificationChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver
        unregisterReceiver(permissionGrantedReceiver)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Start the permission request process
        requestScreenCapturePermission()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d("MyAccessibilityService", "Click event detected.")
            val source: AccessibilityNodeInfo? = event.source
            if (source != null) {
                val className = source.className?.toString()
                Log.d("MyAccessibilityService", "Clicked view class: $className")
                if (className == "android.widget.ImageView" || className == "android.widget.Image") {
                    Log.d("MyAccessibilityService", "ImageView ou Image clicked.")
                    val rect = Rect()
                    source.getBoundsInScreen(rect)
                    // Proceed to capture the screen and crop the image
                    captureAndProcessImage(rect)
                } else {
                    Log.d("MyAccessibilityService", "Event is not from an ImageView or Image")
                }
            } else {
                Log.d("MyAccessibilityService", "Event has no source.")
            }
        }
    }

    override fun onInterrupt() {
        // Handle service interruption
    }

    // Create the notification channel for Android O and above
    private fun createNotificationChannel() {
        // Create the notification channel only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Screen Capture"
            val descriptionText = "Notifications for screen capture permission"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Request screen capture permission
    private fun requestScreenCapturePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission
                val permissionIntent = Intent(this, PermissionRequestActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(permissionIntent)
            } else {
                // Permission granted; show the notification
                showNotification()
            }
        } else {
            // Versions below Android 13; permission not required
            showNotification()
        }
    }

    // Show the notification prompting for screen capture permission
    private fun showNotification() {
        val notificationIntent = Intent(this, PermissionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Capture Permission Required")
            .setContentText("Tap to grant permission for screen capture")
            .setSmallIcon(R.drawable.iconwarning) // Ensure this icon exists
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check permission before notifying
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted; handle accordingly
                return
            }
        }

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // Capture and process the image of the clicked view
    private fun captureAndProcessImage(rect: Rect) {
        if (mediaProjection == null) {
            Log.e("MyAccessibilityService", "mediaProjection is null.")
            showNotification()
            Toast.makeText(this, "Screen capture permission not granted.", Toast.LENGTH_SHORT).show()
            return
        } else {
            Log.d("MyAccessibilityService", "mediaProjection initialized.")
        }

        try {
            Log.d("MyAccessibilityService", "Starting image capture and processing.")
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels
            val screenDensity = metrics.densityDpi

            val imageReader = ImageReader.newInstance(
                screenWidth,
                screenHeight,
                PixelFormat.RGBA_8888,
                2
            )

            val virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "ScreenCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,
                null,
                null
            )

            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                if (image != null) {
                    try {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * screenWidth

                        val bitmap = Bitmap.createBitmap(
                            screenWidth + rowPadding / pixelStride,
                            screenHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(buffer)

                        // Crop the bitmap to the bounds of the clicked view
                        val croppedBitmap = Bitmap.createBitmap(
                            bitmap,
                            rect.left,
                            rect.top,
                            rect.width(),
                            rect.height()
                        )

                        // Store the bitmap in ImageStorage
                        ImageStorage.capturedBitmap = croppedBitmap

                        // Iniciar uma corrotina para processar a imagem e obter o texto descritivo
//                        CoroutineScope(Dispatchers.IO).launch {
//                            try {
//                                // Converter o bitmap para um formato adequado (por exemplo, array de bytes)
//                                val imageData = convertBitmapToByteArray(croppedBitmap)
//
//                                // Enviar a imagem para o modelo LLM e obter o texto descritivo
//                                val descriptiveText = sendImageToLLMModel(imageData)
//
//                                // Passar o texto para o mecanismo TTS na thread principal
//                                withContext(Dispatchers.Main) {
//                                    speakText(descriptiveText)
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                                // Lidar com erros apropriadamente
//                            }
//                        }

                        // After use, clear the bitmap
                        //ImageStorage.clearCapturedBitmap()

                        Log.d("MyAccessibilityService", "Bitmap stored in ImageStorage")

                        // Clear the original bitmap
                        bitmap.recycle()

                        // Do not recycle the croppedBitmap here as it needs to be accessed later

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        image.close()
                        imageReader.setOnImageAvailableListener(null, null)
                        virtualDisplay.release()
                        mediaProjection?.stop()
                    }
                }
            }, null)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}