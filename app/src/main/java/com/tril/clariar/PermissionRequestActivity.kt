package com.tril.clariar

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.PendingIntent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat
import com.tril.clariar.MyAccessibilityService.Companion.CHANNEL_ID
import com.tril.clariar.MyAccessibilityService.Companion.NOTIFICATION_ID

class PermissionRequestActivity : Activity() {

    private val REQUEST_POST_NOTIFICATIONS = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_POST_NOTIFICATIONS)
            } else {
                // Permission already granted
                notifyPermissionGranted()
                finish()
            }
        } else {
            // Permission not required on versions below Android 13
            finish()
        }
    }

    private fun notifyPermissionGranted() {
        TODO("Not yet implemented")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted
                notifyPermissionGranted()
            } else {
                // Permission not granted
                // You may choose to inform the user that permission is required
            }
            finish() // Close the Activity after processing the result
        }
    }

    fun showNotification() {
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
            .setSmallIcon(R.drawable.iconwarning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                with(NotificationManagerCompat.from(this)) {
                    notify(NOTIFICATION_ID, notification)
                }
            } else {
                // Permission not granted
            }
        } else {
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, notification)
            }
        }
    }
}
