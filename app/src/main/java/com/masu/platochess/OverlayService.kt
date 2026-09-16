package com.masu.platochess

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.TextView

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var view: TextView? = null
    private lateinit var params: WindowManager.LayoutParams
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() { view?.text = OverlayBus.message; handler.postDelayed(this, 250) }
    }
    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val prefs = getSharedPreferences("overlay", MODE_PRIVATE)
        view = TextView(this).apply {
            text = OverlayBus.message; textSize = 16f; maxWidth = (resources.displayMetrics.widthPixels * .85).toInt()
            setPadding(22, 14, 22, 14); setBackgroundColor(0xEE202124.toInt()); setTextColor(0xFFFFFFFF.toInt())
            contentDescription = "اقتراح الشطرنج • اسحب لتحريك الفقاعة"
        }
        @Suppress("DEPRECATION")
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply {
                gravity = Gravity.TOP or Gravity.START
                x = prefs.getInt("x", 8); y = prefs.getInt("y", 30)
            }
        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0
        view!!.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { sx = event.rawX; sy = event.rawY; ox = params.x; oy = params.y; true }
                MotionEvent.ACTION_MOVE -> {
                    val metrics = resources.displayMetrics
                    params.x = (ox + (event.rawX - sx).toInt()).coerceIn(0, (metrics.widthPixels - v.width).coerceAtLeast(0))
                    params.y = (oy + (event.rawY - sy).toInt()).coerceIn(0, (metrics.heightPixels - v.height).coerceAtLeast(0))
                    runCatching { wm.updateViewLayout(v, params) }; true
                }
                MotionEvent.ACTION_UP -> { prefs.edit().putInt("x", params.x).putInt("y", params.y).apply(); v.performClick(); true }
                else -> true
            }
        }
        try { wm.addView(view, params); handler.post(tick) } catch (_: Exception) { view = null; stopSelf() }
    }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        view?.let { runCatching { wm.removeView(it) } }
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
