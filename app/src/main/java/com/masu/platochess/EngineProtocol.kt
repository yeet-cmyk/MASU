package com.masu.platochess
object EngineProtocol {fun position(fen:String)="position fen $fen";fun go(depth:Int=14)="go depth $depth";fun parseBestMove(line:String):String?{if(!line.startsWith("bestmove "))return null;return line.split(' ').getOrNull(1)?.takeIf{it!="(none)"}}}
