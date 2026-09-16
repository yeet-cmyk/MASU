package com.masu.platochess
/** Engine boundary. A UCI engine implementation can be plugged in without changing capture/overlay code. */
interface MoveAdvisor{fun bestMove(fen:String):String?}
class PlaceholderAdvisor:MoveAdvisor{override fun bestMove(fen:String):String?=null}
