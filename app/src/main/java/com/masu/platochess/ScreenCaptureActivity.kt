package com.masu.platochess
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
class ScreenCaptureActivity:Activity(){private val req=41;override fun onCreate(b:Bundle?){super.onCreate(b);val m=getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager;startActivityForResult(m.createScreenCaptureIntent(),req)}
 override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(r==req&&c==RESULT_OK&&d!=null){ScreenCaptureStore.resultCode=c;ScreenCaptureStore.data=d;if(android.os.Build.VERSION.SDK_INT>=26)startForegroundService(Intent(this,CaptureService::class.java)) else startService(Intent(this,CaptureService::class.java))};finish()}}
object ScreenCaptureStore{var resultCode:Int=0;var data:Intent?=null}
