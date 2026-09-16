package com.masu.platochess

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import java.io.File

class MainActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 64, 32, 32) }
        box.addView(TextView(this).apply { text = "Plato Chess Assistant"; textSize = 24f })
        box.addView(TextView(this).apply {
            text = "افتح مباراة جديدة قبل أول نقلة ثم وافق على مشاركة الشاشة كاملة. يُحدد لونك تلقائيًا من القطع أسفل الرقعة.\n\nأبقِ الرقعة كاملة ظاهرة والفقاعة خارجها. عند تغيير حجم الشاشة أو بدء مباراة أخرى أوقف التحليل وابدأه مجددًا.\n\nاستخدم المساعدة فقط حيث يُسمح بها. قراءة Plato تجريبية ولم تُختبر على جهاز حقيقي."
            textSize = 16f
        })
        box.addView(Button(this).apply {
            text = "بدء التحليل والفقاعة"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    Toast.makeText(this@MainActivity, "اسمح بالظهور فوق التطبيقات ثم اضغط بدء مجددًا", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    stopService(Intent(this@MainActivity, CaptureService::class.java))
                    startActivity(Intent(this@MainActivity, ScreenCaptureActivity::class.java))
                }
            }
        })
        box.addView(Button(this).apply {
            text = "إيقاف"
            setOnClickListener {
                stopService(Intent(this@MainActivity, CaptureService::class.java))
                stopService(Intent(this@MainActivity, OverlayService::class.java))
                OverlayBus.message = "متوقف"
            }
        })
        box.addView(Button(this).apply {
            text = "تاريخ النقلات"
            setOnClickListener {
                val latest = File(filesDir, "games").listFiles()?.maxByOrNull { it.lastModified() }
                android.app.AlertDialog.Builder(this@MainActivity).setTitle("آخر مباراة")
                    .setMessage(latest?.readText() ?: "لم تُسجل نقلات بعد").setPositiveButton("إغلاق", null).show()
            }
        })
        box.addView(TextView(this).apply { text = "Stockfish 11 • GPLv3 • يعمل محليًا\nالتطبيق لا يضغط ولا يحرك القطع." })
        setContentView(ScrollView(this).apply { addView(box) })
    }
}
