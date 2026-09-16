package com.masu.platochess

/** Screen-board orientation helper. Plato keeps the local player at the bottom. */
enum class PlayerSide { WHITE, BLACK }

data class ScreenSquare(val row: Int, val col: Int)

object ChessRules {
    fun algebraic(square: ScreenSquare, side: PlayerSide): String {
        require(square.row in 0..7 && square.col in 0..7)
        return if (side == PlayerSide.WHITE) {
            "${('a'.code + square.col).toChar()}${8 - square.row}"
        } else {
            "${('h'.code - square.col).toChar()}${1 + square.row}"
        }
    }

    fun prettyMove(uci: String): String = if (uci.length >= 4) "${uci.substring(0,2)} → ${uci.substring(2,4)}" else uci
}