package com.masu.platochess

/** Canonical a8..h1 order, independent of the screen orientation. */
data class BoardState(
    val squares: List<Char?>,
    val sideToMove: PlayerSide,
    val castling: String = "",
    val enPassant: Int? = null,
    val halfmove: Int = 0,
    val fullmove: Int = 1
) {
    init { require(squares.size == 64) }
    fun fenPlacement(): String = squares.chunked(8).joinToString("/") { row ->
        buildString {
            var empty = 0
            for (p in row) {
                if (p == null) empty++ else {
                    if (empty > 0) append(empty)
                    empty = 0
                    append(p)
                }
            }
            if (empty > 0) append(empty)
        }
    }
    fun fen(): String = "${fenPlacement()} ${if (sideToMove == PlayerSide.WHITE) "w" else "b"} ${castling.ifEmpty { "-" }} ${enPassant?.let { Rules.squareName(it) } ?: "-"} $halfmove $fullmove"
    companion object {
        fun initial() = BoardState(InitialPosition.asSquares(PlayerSide.WHITE), PlayerSide.WHITE, "KQkq")
        fun fromFen(fen: String): BoardState {
            val f = fen.split(' ')
            val cells = f[0].filter { it != '/' }.flatMap { p ->
                if (p.isDigit()) List<Char?>(p.digitToInt()) { null } else listOf(p)
            }
            return BoardState(cells, if (f[1] == "w") PlayerSide.WHITE else PlayerSide.BLACK,
                f[2].replace("-", ""), if (f[3] == "-") null else Rules.squareIndex(f[3]), f[4].toInt(), f[5].toInt())
        }
    }
}
