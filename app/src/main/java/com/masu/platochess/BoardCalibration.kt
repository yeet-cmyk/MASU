package com.masu.platochess
import android.graphics.Rect
object BoardCalibration{fun estimate(w:Int,h:Int):Rect{val l=(w*.025f).toInt();val r=(w*.975f).toInt();val t=(h*.27f).toInt();val size=r-l;return Rect(l,t,r,minOf(h,t+size))}fun square(b:Rect,row:Int,col:Int):Rect{val sx=b.width()/8f;val sy=b.height()/8f;return Rect((b.left+col*sx).toInt(),(b.top+row*sy).toInt(),(b.left+(col+1)*sx).toInt(),(b.top+(row+1)*sy).toInt())}}
