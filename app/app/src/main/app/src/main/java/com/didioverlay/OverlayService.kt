package com.superposiciondidi

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import android.graphics.Color

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: TextView

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = TextView(this).apply {
            text = "⏳ Evaluando viaje..."
            textSize = 16f
            setPadding(24, 16, 24, 16)
            setBackgroundColor(Color.DKGRAY)
            setTextColor(Color.WHITE)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.END
        params.x = 40
        params.y = 120

        windowManager.addView(overlayView, params)

        // Simulación inicial (luego lo conectamos a DiDi real)
        val evaluator = RideEvaluator()
        val decision = evaluator.evaluate(
            price = 120,
            minutes = 25,
            distanceKm = 7.0
        )

        when (decision) {
            RideDecision.GOOD -> {
                overlayView.text = "🟢 CONVIENE"
                overlayView.setBackgroundColor(Color.parseColor("#2E7D32"))
            }
            RideDecision.MAYBE -> {
                overlayView.text = "🟡 DUDOSO"
                overlayView.setBackgroundColor(Color.parseColor("#F9A825"))
            }
            RideDecision.BAD -> {
                overlayView.text = "🔴 NO CONVIENE"
                overlayView.setBackgroundColor(Color.parseColor("#C62828"))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
