package com.masu.platochess
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
class CaptureService:Service(){private var projection:MediaProjection?=null;private var reader:ImageReader?=null
 override fun onCreate(){super.onCreate();val id="capture";val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager;if(android.os.Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(NotificationChannel(id,"Screen analysis",NotificationManager.IMPORTANCE_LOW));val n=if(android.os.Build.VERSION.SDK_INT>=26)Notification.Builder(this,id) else Notification.Builder(this);startForeground(7,n.setContentTitle("Plato Chess Assistant").setContentText("تحليل الشاشة يعمل").setSmallIcon(android.R.drawable.ic_menu_view).build())}
 override fun onStartCommand(i:Intent?,f:Int,id:Int):Int{val d=ScreenCaptureStore.data?:return START_NOT_STICKY;val m=getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager;projection=m.getMediaProjection(ScreenCaptureStore.resultCode,d);val dm=resources.displayMetrics;reader=ImageReader.newInstance(dm.widthPixels,dm.heightPixels,PixelFormat.RGBA_8888,2);projection?.createVirtualDisplay("PlatoBoard",dm.widthPixels,dm.heightPixels,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader!!.surface,null,null);reader?.setOnImageAvailableListener({r->val image=r.acquireLatestImage()?:return@setOnImageAvailableListener;try{val p=image.planes[0];val ps=p.pixelStride;val rs=p.rowStride;val pad=rs-ps*image.width;val bmp=Bitmap.createBitmap(image.width+pad/ps,image.height,Bitmap.Config.ARGB_8888);bmp.copyPixelsFromBuffer(p.buffer);val crop=Bitmap.createBitmap(bmp,0,0,image.width,image.height);BoardRecognizer().features(crop);bmp.recycle();crop.recycle()}finally{image.close()}},null);return START_STICKY}
 override fun onDestroy(){reader?.close();projection?.stop();super.onDestroy()};override fun onBind(i:Intent?):IBinder?=null}
