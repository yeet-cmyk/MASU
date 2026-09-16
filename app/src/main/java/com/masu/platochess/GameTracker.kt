package com.masu.platochess

/** Only commits exact, stable observations reachable by legal moves. */
class GameTracker {
    var board = BoardState.initial(); private set
    val history = mutableListOf<String>()
    private val stable = BoardTracker(3)
    fun observe(cells: List<Char?>): Boolean {
        if (cells.size != 64) return false
        if (!stable.accept(cells.joinToString("") { it?.toString() ?: "." })) return false
        if (cells == board.squares) return true
        val next = Rules.legalMoves(board).map { it to Rules.apply(board, it) }
        val single = next.filter { it.second.squares == cells }
        if (single.size == 1) {
            history.add(single[0].first.uci()); board = single[0].second; return true
        }
        // Recover one missed pair (our move followed by a fast reply); ambiguity never commits.
        val pairs = mutableListOf<Triple<ChessMove, ChessMove, BoardState>>()
        for ((first, intermediate) in next) for (second in Rules.legalMoves(intermediate)) {
            val result = Rules.apply(intermediate, second)
            if (result.squares == cells) pairs.add(Triple(first, second, result))
        }
        if (pairs.size != 1) return false
        val (a, b, result) = pairs.single()
        history.add(a.uci()); history.add(b.uci()); board = result
        return true
    }
    fun uncertain() = stable.reset()
}
