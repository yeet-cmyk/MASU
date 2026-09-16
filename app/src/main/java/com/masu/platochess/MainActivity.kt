package com.masu.platochess
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
class MainActivity:Activity(){ override fun onCreate(b:Bundle?){super.onCreate(b); val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(48,80,48,48)}; box.addView(TextView(this).apply{text="Plato Chess Assistant\n\nمساعد تحليل الشطرنج";textSize=24f}); box.addView(Button(this).apply{text="1) السماح بقراءة الشاشة";setOnClickListener{startActivity(Intent(this@MainActivity,ScreenCaptureActivity::class.java))}}); box.addView(Button(this).apply{text="2) تشغيل الفقاعة";setOnClickListener{startOverlay()}}); box.addView(TextView(this).apply{text="يحدد اتجاه الرقعة على أساس أن قطعك في الأسفل. لا يحرك القطع تلقائيًا.";textSize=16f}); setContentView(box)}
 private fun startOverlay(){if(!Settings.canDrawOverlays(this)){startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")));return}; startService(Intent(this,OverlayService::class.java))}}
