package com.example.digitaldetox

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvDuration: TextView
    private lateinit var btnStartService: Button

    // Registro para solicitar permiso de notificaciones (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            iniciarServicio()
        } else {
            Toast.makeText(this, "Se necesita permiso de notificaciones para el servicio", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Asegúrate de que estos IDs existan en tu res/layout/activity_main.xml
        tvDuration = findViewById(R.id.tvDuration)
        btnStartService = findViewById(R.id.btnStartService)

        btnStartService.setOnClickListener {
            checkPermissionsAndStart()
        }
    }

    private fun checkPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                iniciarServicio()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            iniciarServicio()
        }
    }

    private fun iniciarServicio() {
        // Asegúrate de que DetoxService.kt esté en el mismo paquete (com.example.digitaldetox)
        val serviceIntent = Intent(this, DetoxService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Toast.makeText(this, "Modo Detox Iniciado", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val prefs = getSharedPreferences("DetoxPrefs", Context.MODE_PRIVATE)
        val durationMs = prefs.getLong("last_session_ms", 0L)

        if (durationMs > 0) {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = (durationMs / (1000 * 60 * 60))

            tvDuration.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            tvDuration.text = "Sin registros aún"
        }
    }
}