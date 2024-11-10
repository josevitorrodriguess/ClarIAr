package com.tril.clariar

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MyApplication : Application() {

    companion object {
        const val CHANNEL_ID = "ScreenCaptureChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Cria o NotificationChannel apenas no API 26+ (Android O e superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Permissão de Captura de Tela"
            val descriptionText = "Solicita permissão para captura de tela"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}