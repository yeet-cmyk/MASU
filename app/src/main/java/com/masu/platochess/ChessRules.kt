package com.masu.platochess

enum class PlayerSide { WHITE, BLACK }
data class ScreenSquare(val row: Int, val col: Int)
object ChessRules {
 fun algebraic(s: ScreenSquare, side: PlayerSide): String { require(s.row in 0..7 && s.col in 0..7); return if(side==PlayerSide.WHITE) "${('a'.code+s.col).toChar()}${8-s.row}" else "${('h'.code-s.col).toChar()}${1+s.row}" }
 fun prettyMove(uci:String)=if(uci.length>=4) "${uci.substring(0,2)} → ${uci.substring(2,4)}${if(uci.length == 5) " = " + uci[4].uppercaseChar() else ""}" else uci
}