package com.tril.clariar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi

class PermissionActivity : Activity() {

    private val REQUEST_CODE = 1001
    private lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(permissionIntent, REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                // Usuário concedeu permissão; iniciar o serviço de captura de tela
                val intent = Intent(this, ScreenCaptureService::class.java)
                intent.putExtra("resultCode", resultCode)
                intent.putExtra("data", data)
                startForegroundService(intent)
            } else {
                // Permissão negada; lidar com isso aqui
            }
            finish() // Fechar a activity
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}