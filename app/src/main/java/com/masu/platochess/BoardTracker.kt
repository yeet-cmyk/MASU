package com.masu.platochess
/** Stabilizes noisy screen observations before they are sent to an engine. */
class BoardTracker(private val required:Int=3){private var last:String?=null;private var count=0
 fun accept(key:String):Boolean{if(key==last)count++ else{last=key;count=1};return count>=required}
 fun reset(){last=null;count=0}}
