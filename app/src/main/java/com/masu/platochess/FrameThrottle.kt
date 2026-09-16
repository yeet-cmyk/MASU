package com.masu.platochess
class FrameThrottle(private val intervalMs:Long=650){private var last=0L;fun allow(now:Long=android.os.SystemClock.elapsedRealtime()):Boolean{if(now-last<intervalMs)return false;last=now;return true}}
