package com.example.digitaldetox

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class DetoxService : Service() {

    private val receiver = ScreenStateReceiver()

    override fun onCreate() {
        super.onCreate()

        // Registrar el Receiver para eventos de pantalla
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(receiver, filter)

        // Iniciar notificación obligatoria para Foreground Service
        startForeground(1, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "detox_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monitor de Tiempo sin Celular",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Detox Digital Activo")
            .setContentText("Midiendo tu tiempo de inactividad...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
    }
}