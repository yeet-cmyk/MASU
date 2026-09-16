package com.masu.platochess
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
class OverlayService:Service(){private lateinit var wm:WindowManager;private var view:TextView?=null;private lateinit var params:WindowManager.LayoutParams;private val h=Handler(Looper.getMainLooper());private val tick=object:Runnable{override fun run(){view?.text=OverlayBus.message;h.postDelayed(this,350)}}
 override fun onCreate(){super.onCreate();wm=getSystemService(WINDOW_SERVICE) as WindowManager;view=TextView(this).apply{text=OverlayBus.message;textSize=18f;setPadding(28,18,28,18);setBackgroundColor(0xDD202124.toInt());setTextColor(0xFFFFFFFF.toInt())};params=WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT).apply{gravity=Gravity.TOP or Gravity.START;x=80;y=180};var sx=0f;var sy=0f;var ox=0;var oy=0;view!!.setOnTouchListener{_,e->when(e.action){MotionEvent.ACTION_DOWN->{sx=e.rawX;sy=e.rawY;ox=params.x;oy=params.y;true};MotionEvent.ACTION_MOVE->{params.x=ox+(e.rawX-sx).toInt();params.y=oy+(e.rawY-sy).toInt();wm.updateViewLayout(view,params);true};else->false}};wm.addView(view,params);h.post(tick)}
 override fun onDestroy(){h.removeCallbacks(tick);view?.let{wm.removeView(it)};super.onDestroy()};override fun onBind(i:Intent?):IBinder?=null}
