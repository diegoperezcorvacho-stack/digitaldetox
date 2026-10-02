package com.tuusuario.digitaldetox

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.edit

class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("DetoxPrefs", Context.MODE_PRIVATE)

        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                // Se apagó la pantalla: Guardamos la hora actual de inicio
                val startTime = System.currentTimeMillis()
                prefs.edit { putLong("start_time", startTime) }
            }

            Intent.ACTION_USER_PRESENT -> {
                // El usuario desbloqueó el celular: Calculamos la duración
                val startTime = prefs.getLong("start_time", 0L)
                if (startTime != 0L) {
                    val endTime = System.currentTimeMillis()
                    val durationMillis = endTime - startTime

                    // Guardamos la última duración registrada
                    prefs.edit {
                        putLong("last_session_ms", durationMillis)
                        putBoolean("last_session_shown", false)
                        putLong("start_time", 0L) // Reiniciamos la marca de inicio
                    }
                }
            }
        }
    }
}
