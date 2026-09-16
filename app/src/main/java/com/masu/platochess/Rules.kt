package com.masu.platochess

import kotlin.math.abs

data class ChessMove(val from: Int, val to: Int, val promotion: Char? = null) {
    fun uci() = Rules.squareName(from) + Rules.squareName(to) + (promotion?.toString() ?: "")
}

/** Legal moves, including king safety, castling, en passant and underpromotion. */
object Rules {
    fun opposite(s: PlayerSide) = if (s == PlayerSide.WHITE) PlayerSide.BLACK else PlayerSide.WHITE
    fun side(p: Char) = if (p.isUpperCase()) PlayerSide.WHITE else PlayerSide.BLACK
    fun squareName(i: Int) = "${('a'.code + i % 8).toChar()}${8 - i / 8}"
    fun squareIndex(s: String) = (8 - s[1].digitToInt()) * 8 + (s[0] - 'a')
    private val knight = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
    private val diagonal = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
    private val straight = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    private fun at(r: Int, c: Int): Int? = if (r in 0..7 && c in 0..7) r * 8 + c else null

    fun attacked(b: BoardState, target: Int, by: PlayerSide): Boolean {
        for (i in 0..63) {
            val p = b.squares[i] ?: continue
            if (side(p) != by) continue
            val dr = target / 8 - i / 8
            val dc = target % 8 - i % 8
            when (p.lowercaseChar()) {
                'p' -> if (dr == (if (by == PlayerSide.WHITE) -1 else 1) && abs(dc) == 1) return true
                'n' -> if ((abs(dr) == 2 && abs(dc) == 1) || (abs(dr) == 1 && abs(dc) == 2)) return true
                'k' -> if (maxOf(abs(dr), abs(dc)) == 1) return true
                else -> {
                    val aligned = when (p.lowercaseChar()) {
                        'b' -> abs(dr) == abs(dc)
                        'r' -> dr == 0 || dc == 0
                        'q' -> dr == 0 || dc == 0 || abs(dr) == abs(dc)
                        else -> false
                    }
                    if (!aligned || (dr == 0 && dc == 0)) continue
                    val sr = dr.compareTo(0); val sc = dc.compareTo(0)
                    var r = i / 8 + sr; var c = i % 8 + sc; var blocked = false
                    while (r * 8 + c != target) {
                        if (b.squares[r * 8 + c] != null) { blocked = true; break }
                        r += sr; c += sc
                    }
                    if (!blocked) return true
                }
            }
        }
        return false
    }
    fun inCheck(b: BoardState, s: PlayerSide): Boolean {
        val king = b.squares.indexOf(if (s == PlayerSide.WHITE) 'K' else 'k')
        return king < 0 || attacked(b, king, opposite(s))
    }
    fun legalMoves(b: BoardState): List<ChessMove> {
        val moves = mutableListOf<ChessMove>()
        fun add(from: Int, to: Int) {
            val p = b.squares[from]!!
            if (b.squares[to]?.lowercaseChar() == 'k') return
            if (p.lowercaseChar() == 'p' && to / 8 in listOf(0, 7)) {
                "qrbn".forEach { moves.add(ChessMove(from, to, it)) }
            } else moves.add(ChessMove(from, to))
        }
        for (i in 0..63) {
            val p = b.squares[i] ?: continue
            if (side(p) != b.sideToMove) continue
            val r = i / 8; val c = i % 8
            if (p.lowercaseChar() == 'p') {
                val d = if (b.sideToMove == PlayerSide.WHITE) -1 else 1
                val one = at(r + d, c)
                if (one != null && b.squares[one] == null) {
                    add(i, one)
                    if (r == (if (d == -1) 6 else 1)) {
                        val two = (r + 2 * d) * 8 + c
                        if (b.squares[two] == null) add(i, two)
                    }
                }
                for (dc in listOf(-1, 1)) {
                    val t = at(r + d, c + dc) ?: continue
                    val captured = b.squares[t]
                    val epPawn = b.squares[r * 8 + c + dc]
                    if ((captured != null && side(captured) != b.sideToMove) ||
                        (t == b.enPassant && captured == null && epPawn == if (d == -1) 'p' else 'P')) add(i, t)
                }
            } else {
                val kind = p.lowercaseChar()
                val dirs = when (kind) { 'n' -> knight; 'b' -> diagonal; 'r' -> straight; else -> diagonal + straight }
                for ((dr, dc) in dirs) {
                    var rr = r + dr; var cc = c + dc
                    while (true) {
                        val t = at(rr, cc) ?: break
                        val occupant = b.squares[t]
                        if (occupant != null && side(occupant) == b.sideToMove) break
                        add(i, t)
                        if (occupant != null || kind == 'n' || kind == 'k') break
                        rr += dr; cc += dc
                    }
                }
                if (kind == 'k') {
                    val home = if (b.sideToMove == PlayerSide.WHITE) 60 else 4
                    val rook = if (b.sideToMove == PlayerSide.WHITE) 'R' else 'r'
                    if (i == home && !inCheck(b, b.sideToMove)) {
                        for (kingSide in listOf(true, false)) {
                            val right = if (kingSide) 'K' else 'Q'
                            val key = if (b.sideToMove == PlayerSide.WHITE) right else right.lowercaseChar()
                            val path = if (kingSide) listOf(home + 1, home + 2) else listOf(home - 1, home - 2, home - 3)
                            val rookIndex = if (kingSide) home + 3 else home - 4
                            if (key in b.castling && b.squares[rookIndex] == rook && path.all { b.squares[it] == null } &&
                                path.take(2).all { !attacked(b, it, opposite(b.sideToMove)) }) add(i, path[1])
                        }
                    }
                }
            }
        }
        return moves.filter { !inCheck(apply(b, it), b.sideToMove) }
    }
    /** Caller must supply a legal move; deliberately also usable by the legal move generator. */
    fun apply(b: BoardState, m: ChessMove): BoardState {
        val cells = b.squares.toMutableList()
        val p = cells[m.from]!!; val captured = cells[m.to]
        cells[m.from] = null
        if (p.lowercaseChar() == 'p' && m.to == b.enPassant && captured == null && m.from % 8 != m.to % 8)
            cells[m.from / 8 * 8 + m.to % 8] = null
        cells[m.to] = m.promotion?.let { if (side(p) == PlayerSide.WHITE) it.uppercaseChar() else it } ?: p
        if (p.lowercaseChar() == 'k' && abs(m.to - m.from) == 2) {
            val rf = if (m.to > m.from) m.from + 3 else m.from - 4
            val rt = if (m.to > m.from) m.from + 1 else m.from - 1
            cells[rt] = cells[rf]; cells[rf] = null
        }
        var rights = b.castling
        if (p == 'K') rights = rights.replace("K", "").replace("Q", "")
        if (p == 'k') rights = rights.replace("k", "").replace("q", "")
        for ((sq, right) in listOf(0 to 'q', 7 to 'k', 56 to 'Q', 63 to 'K'))
            if (m.from == sq || m.to == sq) rights = rights.replace(right.toString(), "")
        return BoardState(cells, opposite(b.sideToMove), rights,
            if (p.lowercaseChar() == 'p' && abs(m.to - m.from) == 16) (m.from + m.to) / 2 else null,
            if (p.lowercaseChar() == 'p' || captured != null) 0 else b.halfmove + 1,
            b.fullmove + if (b.sideToMove == PlayerSide.BLACK) 1 else 0)
    }
}
