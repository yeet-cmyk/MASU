package com.masu.platochess

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings

class ScreenCaptureActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        if (state != null) return
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val request = if (Build.VERSION.SDK_INT >= 34)
            manager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
            else manager.createScreenCaptureIntent()
        @Suppress("DEPRECATION") startActivityForResult(request, 41)
    }
    @Deprecated("Activity result bridge")
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        if (request == 41 && result == RESULT_OK && data != null) {
            if (Settings.canDrawOverlays(this)) startService(Intent(this, OverlayService::class.java))
            val capture = Intent(this, CaptureService::class.java).putExtra("resultCode", result).putExtra("data", data)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(capture) else startService(capture)
        }
        finish()
    }
}
