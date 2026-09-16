package com.masu.platochess

import android.content.Intent
import android.graphics.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RuntimeTest {
    @Test fun bundledEngineReturnsLegalMove() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        StockfishAdvisor(File(context.applicationInfo.nativeLibraryDir, "libstockfish.so")).use { engine ->
            val b = BoardState.initial()
            val move = engine.bestMove(b.fen())
            assertTrue("Engine returned $move", Rules.legalMoves(b).any { it.uci() == move })
            val black = Rules.apply(b, Rules.legalMoves(b).single { it.uci() == "e2e4" })
            val reply = engine.bestMove(black.fen())
            assertTrue(Rules.legalMoves(black).any { it.uci() == reply })
        }
    }
    @Test fun activityOpens() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val activity = instrumentation.startActivitySync(Intent(instrumentation.targetContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        assertNotNull(activity)
        instrumentation.runOnMainSync { activity.finish() }
    }
    /** Synthetic fixture, deliberately NOT evidence of accuracy on Plato assets. */
    private fun fixture(board: BoardState, side: PlayerSide): Bitmap {
        val bitmap = Bitmap.createBitmap(640, 1080, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap); canvas.drawColor(Color.rgb(35,40,48))
        val paint = Paint().apply { isAntiAlias = false }
        val cells = if (side == PlayerSide.WHITE) board.squares else board.squares.reversed()
        for (i in 0..63) {
            val x = 32f + (i%8)*72; val y = 250f + (i/8)*72
            paint.color = if ((i/8+i%8)%2 == 0) Color.rgb(181,184,160) else Color.rgb(103,132,94)
            canvas.drawRect(x,y,x+72,y+72,paint)
            val p = cells[i] ?: continue
            paint.color = if (p.isUpperCase()) Color.rgb(245,245,245) else Color.rgb(18,18,18)
            // Connected geometric silhouettes, with different heights and crowns for each type.
            val kind = "pnbrqk".indexOf(p.lowercaseChar())
            canvas.drawRect(x+20,y+48,x+52,y+59,paint)
            canvas.drawRect(x+28,y+24,x+44,y+49,paint)
            when (kind) {
                0 -> canvas.drawCircle(x+36,y+25,10f,paint)
                1 -> canvas.drawRect(x+17,y+15,x+41,y+29,paint)
                2 -> { canvas.drawCircle(x+36,y+23,14f,paint); canvas.drawRect(x+32,y+9,x+40,y+19,paint) }
                3 -> { canvas.drawRect(x+17,y+14,x+55,y+28,paint); canvas.drawRect(x+17,y+9,x+24,y+17,paint); canvas.drawRect(x+48,y+9,x+55,y+17,paint) }
                4 -> { canvas.drawRect(x+16,y+17,x+56,y+30,paint); canvas.drawCircle(x+20,y+13,5f,paint); canvas.drawCircle(x+36,y+11,5f,paint); canvas.drawCircle(x+52,y+13,5f,paint) }
                5 -> { canvas.drawRect(x+31,y+5,x+41,y+30,paint); canvas.drawRect(x+23,y+12,x+49,y+20,paint) }
            }
        }
        return bitmap
    }
    @Test fun syntheticBothOrientationsAndMove() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (side in PlayerSide.values()) {
            val reader = BoardRecognizer()
            val initial = fixture(BoardState.initial(), side)
            File(context.getExternalFilesDir(null), "synthetic-${side}.png").outputStream().use { initial.compress(Bitmap.CompressFormat.PNG,100,it) }
            assertFalse(reader.initialize(initial)); assertFalse(reader.initialize(initial))
            assertTrue("Could not initialize synthetic $side", reader.initialize(initial))
            assertEquals(side, reader.playerSide)
            assertEquals(BoardState.initial().squares, reader.read(initial))
            val b = Rules.apply(BoardState.initial(), Rules.legalMoves(BoardState.initial()).single { it.uci() == "e2e4" })
            val moved = fixture(b, side)
            assertEquals("Move recognition $side", b.squares, reader.read(moved))
            moved.recycle(); initial.recycle()
        }
    }
    @Test fun nonBoardRejected() {
        val empty = Bitmap.createBitmap(640,1080,Bitmap.Config.ARGB_8888)
        empty.eraseColor(Color.GRAY)
        assertFalse(BoardRecognizer().initialize(empty))
        empty.recycle()
    }
}
