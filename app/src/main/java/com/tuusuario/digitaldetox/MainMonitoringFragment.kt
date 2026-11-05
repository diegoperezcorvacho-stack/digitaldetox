package com.tuusuario.digitaldetox

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.util.Locale

class MainMonitoringFragment : Fragment() {

    private lateinit var tvDuration: TextView
    private lateinit var btnStartService: Button
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var ivIllustration: ImageView
    private lateinit var cardMessage: MaterialCardView
    private lateinit var tvMessageDetail: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            iniciarServicio()
        } else {
            Toast.makeText(requireContext(), getString(R.string.notification_permission_needed), Toast.LENGTH_SHORT).show()
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateLiveUI()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_monitoring, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDuration = view.findViewById(R.id.tvDuration)
        btnStartService = view.findViewById(R.id.btnStartService)
        progressIndicator = view.findViewById(R.id.progressIndicator)
        ivIllustration = view.findViewById(R.id.ivIllustration)
        cardMessage = view.findViewById(R.id.cardMessage)
        tvMessageDetail = view.findViewById(R.id.tvMessageDetail)

        btnStartService.setOnClickListener {
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                checkPermissionsAndStart()
            }.start()
        }
    }

    private fun checkPermissionsAndStart() {
        val context = requireContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
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
        val context = requireContext()
        val serviceIntent = Intent(context, DetoxService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
        Toast.makeText(context, getString(R.string.detox_started), Toast.LENGTH_SHORT).show()
        updateUIState(isMonitoring = true)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
        checkLastSession()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateLiveUI() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("DetoxPrefs", Context.MODE_PRIVATE)
        val startTime = prefs.getLong("start_time", 0L)

        if (startTime != 0L) {
            val elapsed = System.currentTimeMillis() - startTime
            updateTimerText(elapsed)
            val progress = ((elapsed % 3600000) / 36000f).toInt()
            progressIndicator.setProgress(progress, true)
            updateUIState(isMonitoring = true)
        } else {
            updateUIState(isMonitoring = false)
            progressIndicator.setProgress(0, false)
        }
    }

    private fun checkLastSession() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("DetoxPrefs", Context.MODE_PRIVATE)
        val lastSession = prefs.getLong("last_session_ms", 0L)
        val sessionShown = prefs.getBoolean("last_session_shown", true)

        if (lastSession > 0 && !sessionShown) {
            showSuccessCard(lastSession)
            prefs.edit { putBoolean("last_session_shown", true) }
        }
    }

    private fun showSuccessCard(durationMs: Long) {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))

        val timeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        tvMessageDetail.text = getString(R.string.session_detail, timeStr)

        cardMessage.visibility = View.VISIBLE
        cardMessage.alpha = 0f
        cardMessage.animate().alpha(1f).setDuration(600).start()
    }

    private fun updateTimerText(durationMs: Long) {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        tvDuration.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun updateUIState(isMonitoring: Boolean) {
        if (!isAdded) return
        if (isMonitoring) {
            ivIllustration.setImageResource(R.drawable.ic_relax)
            btnStartService.text = getString(R.string.btn_monitoring)
            btnStartService.isEnabled = false
        } else {
            ivIllustration.setImageResource(R.drawable.ic_moon)
            btnStartService.text = getString(R.string.btn_start_monitoring)
            btnStartService.isEnabled = true
        }
    }
}
