package com.didioverlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    companion object {
        const val CHANNEL_ID = "overlay_channel"
        const val NOTIF_ID = 101

        // Acciones para cambiar el semáforo desde MainActivity (o desde donde quieras)
        const val ACTION_SET_STATUS = "com.didioverlay.ACTION_SET_STATUS"
        const val EXTRA_STATUS = "status"

        // Valores permitidos
        const val STATUS_NO = "NO"          // No conviene
        const val STATUS_OK = "OK"          // Conviene
        const val STATUS_EXCELLENT = "EXCELLENT" // Excelente
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: LinearLayout? = null
    private lateinit var statusDot: TextView
    private lateinit var statusText: TextView

    override fun onCreate() {
        super.onCreate()

        // Si no tiene permiso de superposición, no hacemos nada
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createOverlay()
        setStatus(STATUS_OK) // estado inicial
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SET_STATUS) {
            val status = intent.getStringExtra(EXTRA_STATUS) ?: STATUS_OK
            setStatus(status)
        }
        return START_STICKY
    }

    private fun createOverlay() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 18, 24, 18)
            // Fondo negro semi-transparente
            setBackgroundColor(0xAA000000.toInt())
        }

        statusDot = TextView(this).apply {
            text = "●"
            textSize = 42f
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            text = "Evaluando..."
            gravity = Gravity.CENTER
        }

        layout.addView(statusDot)
        layout.addView(statusText)

        val overlayType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 30
            y = 240
        }

        overlayView = layout
        windowManager.addView(layout, params)
    }

    private fun setStatus(status: String) {
        when (status) {
            STATUS_NO -> {
                statusDot.setTextColor(0xFFFF3B30.toInt()) // rojo
                statusText.text = "NO conviene"
            }
            STATUS_OK -> {
                statusDot.setTextColor(0xFFFFCC00.toInt()) // amarillo
                statusText.text = "Conviene"
            }
            STATUS_EXCELLENT -> {
                statusDot.setTextColor(0xFF34C759.toInt()) // verde
                statusText.text = "Excelente"
            }
            else -> {
                statusDot.setTextColor(0xFFFFCC00.toInt())
                statusText.text = "Conviene"
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Didi Overlay")
            .setContentText("Semáforo activo")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
