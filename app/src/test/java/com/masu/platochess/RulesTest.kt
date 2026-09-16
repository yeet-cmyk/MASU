package com.masu.platochess

import org.junit.Assert.*
import org.junit.Test

class RulesTest {
    private fun perft(b: BoardState, depth: Int): Long = if (depth == 0) 1 else Rules.legalMoves(b).sumOf { perft(Rules.apply(b, it), depth - 1) }
    private fun play(b: BoardState, move: String) = Rules.apply(b, Rules.legalMoves(b).single { it.uci() == move })
    @Test fun initialPerft() {
        val b = BoardState.initial()
        assertEquals(20, Rules.legalMoves(b).size)
        assertEquals(400L, perft(b, 2))
        assertEquals(8902L, perft(b, 3))
        assertEquals(197281L, perft(b, 4))
    }
    @Test fun kiwipeteCastlingAndPins() {
        val b = BoardState.fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
        assertEquals(48L, perft(b, 1))
        assertEquals(2039L, perft(b, 2))
        assertEquals(97862L, perft(b, 3))
        val next = play(b, "e1g1")
        assertEquals('R', next.squares[61]); assertNull(next.squares[63])
        assertEquals("kq", next.castling)
    }
    @Test fun enPassantAndDiscoveredCheck() {
        var b = BoardState.initial()
        for (m in listOf("e2e4", "a7a6", "e4e5", "d7d5", "e5d6")) b = play(b, m)
        assertNull(b.squares[Rules.squareIndex("d5")])
        assertEquals('P', b.squares[Rules.squareIndex("d6")])
        val pin = BoardState.fromFen("k3r3/8/8/3pP3/8/8/8/4K3 w - d6 0 1")
        assertFalse(Rules.legalMoves(pin).any { it.uci() == "e5d6" })
    }
    @Test fun promotionsAndRights() {
        val b = BoardState.fromFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
        assertEquals(setOf("a7a8q", "a7a8r", "a7a8b", "a7a8n"), Rules.legalMoves(b).filter { it.from == 8 }.map { it.uci() }.toSet())
        assertEquals('N', play(b, "a7a8n").squares[0])
    }
    @Test fun trackerStabilityAndMissedReply() {
        val t = GameTracker()
        val b = play(play(t.board, "e2e4"), "e7e5")
        assertFalse(t.observe(b.squares)); assertFalse(t.observe(b.squares)); assertTrue(t.observe(b.squares))
        assertEquals(listOf("e2e4", "e7e5"), t.history)
        assertEquals(b, t.board)
        val invalid = b.squares.toMutableList().apply { this[0] = null }
        repeat(3) { assertFalse(t.observe(invalid)) }
        assertEquals(2, t.history.size)
    }
    @Test fun orientation() {
        assertEquals("a8", ChessRules.algebraic(ScreenSquare(0,0), PlayerSide.WHITE))
        assertEquals("h1", ChessRules.algebraic(ScreenSquare(0,0), PlayerSide.BLACK))
        assertEquals(InitialPosition.asSquares(PlayerSide.WHITE).reversed(), InitialPosition.asSquares(PlayerSide.BLACK))
    }
}
