package com.masu.platochess
import android.graphics.Bitmap
import android.graphics.Color
class BoardRecognizer {
 data class CellFeature(val meanR:Int,val meanG:Int,val meanB:Int,val variance:Double,val centerLuma:Int)
 fun features(frame:Bitmap):List<CellFeature>{val b=BoardCalibration.estimate(frame.width,frame.height);return (0 until 64).map{i->val q=BoardCalibration.square(b,i/8,i%8);var rr=0L;var gg=0L;var bb=0L;var n=0L;val vals=ArrayList<Int>();val step=maxOf(2,q.width()/18);var y=q.top+step;while(y<q.bottom-step){var x=q.left+step;while(x<q.right-step){val p=frame.getPixel(x,y);rr+=Color.red(p);gg+=Color.green(p);bb+=Color.blue(p);vals.add((Color.red(p)+Color.green(p)+Color.blue(p))/3);n++;x+=step};y+=step};val mean=if(n==0L)0.0 else vals.sum().toDouble()/n;val v=if(vals.isEmpty())0.0 else vals.sumOf{(it-mean)*(it-mean)}/vals.size;val cp=frame.getPixel(q.centerX(),q.centerY());CellFeature((rr/maxOf(1,n)).toInt(),(gg/maxOf(1,n)).toInt(),(bb/maxOf(1,n)).toInt(),v,(Color.red(cp)+Color.green(cp)+Color.blue(cp))/3)}}
 fun inferInitialSide(f:List<CellFeature>):PlayerSide?{if(f.size!=64)return null;val top=(0..15).map{f[it].centerLuma}.average();val bottom=(48..63).map{f[it].centerLuma}.average();if(kotlin.math.abs(top-bottom)<8)return null;return if(bottom>top)PlayerSide.WHITE else PlayerSide.BLACK}
}
