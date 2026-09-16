package com.masu.platochess
import android.graphics.Rect
object BoardCalibration {
 fun estimate(w:Int,h:Int):Rect { val l=(w*.025f).toInt(); val r=(w*.975f).toInt(); val t=(h*.27f).toInt(); return Rect(l,t,r,t+r-l) }
 fun square(b:Rect,row:Int,col:Int):Rect { val s=b.width()/8f; return Rect((b.left+col*s).toInt(),(b.top+row*s).toInt(),(b.left+(col+1)*s).toInt(),(b.top+(row+1)*s).toInt()) }
}