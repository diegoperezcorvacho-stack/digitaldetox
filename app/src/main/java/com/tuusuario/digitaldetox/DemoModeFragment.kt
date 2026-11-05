package com.tuusuario.digitaldetox

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.util.Locale

class DemoModeFragment : Fragment() {

    private lateinit var cardDemo: MaterialCardView
    private lateinit var tvPhaseStatus: TextView
    private lateinit var demoProgressIndicator: CircularProgressIndicator
    private lateinit var tvDemoDuration: TextView
    private lateinit var btnStartDemo: Button
    private lateinit var btnResetDemo: Button

    private lateinit var cardDemoSuccess: MaterialCardView
    private lateinit var tvDemoSuccessDetails: TextView

    private lateinit var btnClearDb: Button
    private lateinit var tvDbEmpty: TextView
    private lateinit var rvSessions: RecyclerView

    private lateinit var dbHelper: DetoxDatabaseHelper
    private lateinit var sessionAdapter: SessionAdapter

    private var animator: ValueAnimator? = null
    private val argbEvaluator = ArgbEvaluator()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_demo_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DetoxDatabaseHelper(requireContext())

        cardDemo = view.findViewById(R.id.cardDemo)
        tvPhaseStatus = view.findViewById(R.id.tvPhaseStatus)
        demoProgressIndicator = view.findViewById(R.id.demoProgressIndicator)
        tvDemoDuration = view.findViewById(R.id.tvDemoDuration)
        btnStartDemo = view.findViewById(R.id.btnStartDemo)
        btnResetDemo = view.findViewById(R.id.btnResetDemo)

        cardDemoSuccess = view.findViewById(R.id.cardDemoSuccess)
        tvDemoSuccessDetails = view.findViewById(R.id.tvDemoSuccessDetails)

        btnClearDb = view.findViewById(R.id.btnClearDb)
        tvDbEmpty = view.findViewById(R.id.tvDbEmpty)
        rvSessions = view.findViewById(R.id.rvSessions)

        sessionAdapter = SessionAdapter(emptyList())
        rvSessions.layoutManager = LinearLayoutManager(requireContext())
        rvSessions.adapter = sessionAdapter

        btnStartDemo.setOnClickListener {
            startDemoSimulation()
        }

        btnResetDemo.setOnClickListener {
            resetDemoSimulation()
        }

        btnClearDb.setOnClickListener {
            dbHelper.clearAllSessions()
            loadDatabaseRecords()
            Toast.makeText(requireContext(), "Base de datos reiniciada", Toast.LENGTH_SHORT).show()
        }

        loadDatabaseRecords()
    }

    override fun onResume() {
        super.onResume()
        loadDatabaseRecords()
    }

    override fun onPause() {
        super.onPause()
        animator?.cancel()
    }

    private fun startDemoSimulation() {
        if (animator?.isRunning == true) return

        btnStartDemo.isEnabled = false
        btnResetDemo.isEnabled = false
        cardDemoSuccess.visibility = View.GONE

        val colorNavy = "#1E293B".toColorInt()
        val colorIndigo = "#4F46E5".toColorInt()
        val colorAmber = "#F59E0B".toColorInt()
        val colorEmerald = "#10B981".toColorInt()

        val totalSimulatedMs = 28800000L // 8 horas = 28,800,000 ms

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 10000L // 10 segundos reales
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float

                // 1. Cálculo automático: de 00:00:00 a 08:00:00 en 10s
                val currentSimulatedMs = (fraction * totalSimulatedMs).toLong()
                val seconds = (currentSimulatedMs / 1000) % 60
                val minutes = (currentSimulatedMs / (1000 * 60)) % 60
                val hours = (currentSimulatedMs / (1000 * 60 * 60))

                tvDemoDuration.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                demoProgressIndicator.setProgress((fraction * 100).toInt(), true)

                // 2. Transición suave de color
                val currentColor = when {
                    fraction < 0.33f -> {
                        val subFraction = fraction / 0.33f
                        argbEvaluator.evaluate(subFraction, colorNavy, colorIndigo) as Int
                    }
                    fraction < 0.66f -> {
                        val subFraction = (fraction - 0.33f) / 0.33f
                        argbEvaluator.evaluate(subFraction, colorIndigo, colorAmber) as Int
                    }
                    else -> {
                        val subFraction = (fraction - 0.66f) / 0.34f
                        argbEvaluator.evaluate(subFraction, colorAmber, colorEmerald) as Int
                    }
                }

                demoProgressIndicator.setIndicatorColor(currentColor)
                tvDemoDuration.setTextColor(currentColor)
                tvPhaseStatus.setTextColor(currentColor)
                cardDemo.setStrokeColor(currentColor)

                // Actualización de Fase
                tvPhaseStatus.text = when {
                    fraction < 0.25f -> "🌙 Fase 1: Inicio de descanso (0h - 2h)"
                    fraction < 0.50f -> "💤 Fase 2: Sueño Profundo (2h - 4h)"
                    fraction < 0.75f -> "🌅 Fase 3: Modo Detox Activo (4h - 6h)"
                    else -> "☀️ Fase 4: Meta Alcanzada (6h - 8h)"
                }
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!isAdded) return
                    btnStartDemo.isEnabled = true
                    btnResetDemo.isEnabled = true

                    // 3. Guardado en SQLite en tiempo real al finalizar los 10 segundos
                    val insertedId = dbHelper.insertSession(totalSimulatedMs, isDemo = true)

                    showSuccessCard(insertedId)
                    loadDatabaseRecords()
                }
            })

            start()
        }
    }

    private fun resetDemoSimulation() {
        animator?.cancel()
        animator = null

        btnStartDemo.isEnabled = true
        btnResetDemo.isEnabled = true

        demoProgressIndicator.setProgress(0, false)
        tvDemoDuration.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", 0, 0, 0)

        val defaultColor = "#1E293B".toColorInt()
        demoProgressIndicator.setIndicatorColor(defaultColor)
        tvDemoDuration.setTextColor(defaultColor)
        tvPhaseStatus.setTextColor(defaultColor)
        tvPhaseStatus.text = "🌙 Fase 1: Inicio de descanso (0h - 2h)"
        cardDemo.setStrokeColor(defaultColor)

        cardDemoSuccess.visibility = View.GONE
    }

    private fun showSuccessCard(insertedId: Long) {
        tvDemoSuccessDetails.text = String.format(
            Locale.getDefault(),
            "Sesión de 8h (28,800s) guardada en SQLite. Registro #%d",
            insertedId
        )

        cardDemoSuccess.visibility = View.VISIBLE
        cardDemoSuccess.alpha = 0f
        cardDemoSuccess.scaleX = 0.9f
        cardDemoSuccess.scaleY = 0.9f
        cardDemoSuccess.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .start()
    }

    private fun loadDatabaseRecords() {
        val sessions = dbHelper.getAllSessions()
        if (sessions.isEmpty()) {
            tvDbEmpty.visibility = View.VISIBLE
            rvSessions.visibility = View.GONE
        } else {
            tvDbEmpty.visibility = View.GONE
            rvSessions.visibility = View.VISIBLE
            sessionAdapter.updateData(sessions)
        }
    }
}
