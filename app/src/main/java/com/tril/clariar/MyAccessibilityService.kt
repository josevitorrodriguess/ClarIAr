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
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tril.clariar.http.GroqApiRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            //Free up resources such as virtual displays or image readers
            //Log.d("MyAccessibilityService", "MediaProjection has been stopped")
        }
    }

    private lateinit var ttsHandler: TextToSpeechHandler

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        // Initialize the TextToSpeechHandler
        ttsHandler = TextToSpeechHandler(this)

        // Register the receiver
        val filter = IntentFilter("com.tril.clariar.PERMISSION_GRANTED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 or higher
            registerReceiver(
                permissionGrantedReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(permissionGrantedReceiver, filter, RECEIVER_NOT_EXPORTED)
        }

        // Create the notification channel
        createNotificationChannel()
    }

    override fun onDestroy() {
        // Disable TextToSpeechHandler to free up resources
        ttsHandler.shutdown()
        super.onDestroy()
        // Unregister the MediaProjection callback
        mediaProjection?.unregisterCallback(mediaProjectionCallback)
        // Unregister any receiver
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
                    Log.d("MyAccessibilityService", "ImageView or Image clicked.")

                    // Check if TTS is speaking
                    if (ttsHandler.isSpeaking()) {
                        // Interrupts TTS
                        ttsHandler.stop()
                        Log.d("MyAccessibilityService", "TTS interrupted by user.")
                    } else {
                        // Provide immediate auditory feedback
                        ttsHandler.speak("Processando imagem, por favor aguarde.")

                        // Proceed to capture and process the image in the background
                        val rect = Rect()
                        source.getBoundsInScreen(rect)

                        // Start the capture and processing in a coroutine
                        CoroutineScope(Dispatchers.Default).launch {
                            captureAndProcessImage(rect)
                        }
                    }
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
        // register callback
        mediaProjection?.registerCallback(mediaProjectionCallback, Handler(Looper.getMainLooper()))

        try {
            //Log.d("MyAccessibilityService", "Starting image capture and processing.")
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
                //Log.d("MyAccessibilityService", "OnImageAvailableListener chamado")
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

                        // Retrieve the dimensions of the bitmap
                        val bitmapWidth = bitmap.width
                        val bitmapHeight = bitmap.height

                        // Ensure rect.left and rect.top are within the bitmap bounds
                        val x = rect.left.coerceAtLeast(0)
                        val y = rect.top.coerceAtLeast(0)

                        // Calculate the maximum possible width and height starting from (x, y)
                        val maxWidth = bitmapWidth - x
                        val maxHeight = bitmapHeight - y

                        // Adjust rect.width() and rect.height() if they exceed the bitmap bounds
                        val width = rect.width().coerceAtMost(maxWidth)
                        val height = rect.height().coerceAtMost(maxHeight)

                        // Ensure width and height are positive
                        if (width <= 0 || height <= 0) {
                            // Handle the error case
                            //Log.e("BitmapCreation", "Invalid dimensions for bitmap cropping.")
                            return@setOnImageAvailableListener
                        }
                        //Log.d("BitmapParams", "Bitmap dimensions: width=${bitmapWidth}, height=${bitmapHeight}")
                        //Log.d("BitmapParams", "Crop parameters: x=$x, y=$y, width=$width, height=$height")


                        // Crop the bitmap to the bounds of the clicked view
                        val croppedBitmap = Bitmap.createBitmap(
                            bitmap,
                            x,
                            y,
                            width,
                            height
                        )
                        // Store the bitmap in ImageStorage
                        ImageStorage.capturedBitmap = croppedBitmap

                        // Starts a Coroutine to send the image and get the descriptive text
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val apiKey = getString(R.string.api_key)

                                // Creates a GroqApiRequest instance with the API key and the captured image
                                val groqApiRequest = GroqApiRequest(apiKey, croppedBitmap, ttsHandler)

                                // Send the image to the LLM model and get the descriptive text
                                val descriptiveText = groqApiRequest.sendChatRequest()

                                // Checks if descriptiveText is not null
                                if (descriptiveText != null) {
                                    // Registers the descriptive text
                                    val cleanedText = descriptiveText.replace("\\s+".toRegex(), " ").trim()
                                    Log.d("MyAccessibilityService", "Descriptive Text: $cleanedText")


                                    withContext(Dispatchers.Main) {
                                        ttsHandler.speak(cleanedText)
                                    }

                                } else {
                                    // handles the case when descriptiveText is null
                                    Log.e("MyAccessibilityService", "Failed to get descriptive text from LLM model")

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(applicationContext, "Failed to get image description.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Handle exceptions appropriately
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(applicationContext, "An error occurred.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        // After use, clean the bitmap .
                        // ImageStorage.clearCapturedBitmap()

                        //Log.d("MyAccessibilityService", "Bitmap stored in ImageStorage")

                        // Clear the original bitmap
                        bitmap.recycle()

                        // Do not recycle the croppedBitmap here as it needs to be accessed later

                    } catch (e: Exception) {
                        Log.e("MyAccessibilityService", "Error while processing image", e)
                        e.printStackTrace()
                    } finally {
                        image.close()
                        reader.close()
                        imageReader.close()
                        imageReader.setOnImageAvailableListener(null, null)
                        virtualDisplay.release()
                    }

                } else {
                // If there is no image, still release the resources
                imageReader.setOnImageAvailableListener(null, null)
                imageReader.close()
                virtualDisplay.release()
            }
            }, Handler(Looper.getMainLooper()))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}