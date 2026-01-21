package com.didioverlay

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun sendStatus(status: String) {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_SET_STATUS
            putExtra(OverlayService.EXTRA_STATUS, status)
        }
        startService(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI simple con botones para probar
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 80, 40, 40)
        }

        val btnPermission = Button(this).apply { text = "1) Dar permiso de superposición" }
        val btnStart = Button(this).apply { text = "2) Iniciar overlay" }
        val btnNo = Button(this).apply { text = "Semáforo: NO conviene" }
        val btnOk = Button(this).apply { text = "Semáforo: Conviene" }
        val btnExcellent = Button(this).apply { text = "Semáforo: Excelente" }

        layout.addView(btnPermission)
        layout.addView(btnStart)
        layout.addView(btnNo)
        layout.addView(btnOk)
        layout.addView(btnExcellent)

        setContentView(layout)

        btnPermission.setOnClickListener {
            requestOverlayPermission()
        }

        btnStart.setOnClickListener {
            if (!hasOverlayPermission()) {
                requestOverlayPermission()
            } else {
                startOverlayService()
            }
        }

        btnNo.setOnClickListener { sendStatus(OverlayService.STATUS_NO) }
        btnOk.setOnClickListener { sendStatus(OverlayService.STATUS_OK) }
        btnExcellent.setOnClickListener { sendStatus(OverlayService.STATUS_EXCELLENT) }
    }
}
