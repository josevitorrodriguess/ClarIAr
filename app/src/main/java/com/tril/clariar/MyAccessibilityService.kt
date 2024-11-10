package com.tril.clariar

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MyAccessibilityService : AccessibilityService() {

    companion object {
        const val CHANNEL_ID = "ScreenCaptureChannel"
        const val NOTIFICATION_ID = 1
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
        val filter = IntentFilter("com.example.clariar.PERMISSION_GRANTED")
        registerReceiver(permissionGrantedReceiver, filter)
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

    private fun requestScreenCapturePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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
            .setContentTitle("Permissão de Captura de Tela Necessária")
            .setContentText("Toque para conceder permissão para captura de tela")
            .setSmallIcon(R.drawable.iconwarning) // Make sure this icon exists
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
    }

    override fun onInterrupt() {
        // Handle service interruption
    }
}
