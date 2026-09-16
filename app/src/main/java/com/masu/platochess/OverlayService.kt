package com.masu.platochess
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
class OverlayService:Service(){private lateinit var wm:WindowManager;private var view:TextView?=null;private val h=Handler(Looper.getMainLooper());private val tick=object:Runnable{override fun run(){view?.text=OverlayBus.message;h.postDelayed(this,350)}}
 override fun onCreate(){super.onCreate();wm=getSystemService(WINDOW_SERVICE) as WindowManager;view=TextView(this).apply{text=OverlayBus.message;textSize=18f;setPadding(28,18,28,18);setBackgroundColor(0xDD202124.toInt());setTextColor(0xFFFFFFFF.toInt())};val p=WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT).apply{gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL;y=180};wm.addView(view,p);h.post(tick)}
 override fun onDestroy(){h.removeCallbacks(tick);view?.let{wm.removeView(it)};super.onDestroy()};override fun onBind(i:Intent?):IBinder?=null}
