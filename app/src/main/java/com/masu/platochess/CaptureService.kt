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
 override fun onStartCommand(i:Intent?,f:Int,id:Int):Int{val d=ScreenCaptureStore.data?:return START_NOT_STICKY;val m=getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager;projection=m.getMediaProjection(ScreenCaptureStore.resultCode,d);val dm=resources.displayMetrics;reader=ImageReader.newInstance(dm.widthPixels,dm.heightPixels,PixelFormat.RGBA_8888,2);projection?.createVirtualDisplay("PlatoBoard",dm.widthPixels,dm.heightPixels,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader!!.surface,null,null);reader?.setOnImageAvailableListener({r->val image=r.acquireLatestImage()?:return@setOnImageAvailableListener;try{val p=image.planes[0];val pixelStride=p.pixelStride;val rowStride=p.rowStride;val rowPadding=rowStride-pixelStride*image.width;val bmp=Bitmap.createBitmap(image.width+rowPadding/pixelStride,image.height,Bitmap.Config.ARGB_8888);bmp.copyPixelsFromBuffer(p.buffer);val cropped=Bitmap.createBitmap(bmp,0,0,image.width,image.height);BoardRecognizer().features(cropped);bmp.recycle();cropped.recycle()}finally{image.close()}},null);return START_STICKY}
 override fun onDestroy(){reader?.close();projection?.stop();super.onDestroy()};override fun onBind(i:Intent?):IBinder?=null}
