package com.masu.platochess

object InitialPosition {
    val whiteView = listOf(
        "rnbqkbnr",
        "pppppppp",
        "........",
        "........",
        "........",
        "........",
        "PPPPPPPP",
        "RNBQKBNR"
    )

    val blackView = whiteView.reversed().map { it.reversed() }

    fun asSquares(side: PlayerSide): List<Char?> {
        val rows = if (side == PlayerSide.WHITE) whiteView else blackView
        return rows.flatMap { row ->
            row.map { piece -> if (piece == '.') null else piece }
        }
    }
}
