package com.masu.platochess

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import kotlin.math.abs

/** Session templates learned only from a stable initial setup. Unknown observations fail closed. */
class BoardRecognizer {
    data class Shape(val mask: BooleanArray, val light: DoubleArray, val density: Double, val luma: Double)
    var bounds: Rect? = null; private set
    var playerSide: PlayerSide? = null; private set
    private val templates = mutableMapOf<Char, MutableList<Shape>>()
    private var candidate: List<Shape>? = null
    private var candidateBounds: Rect? = null
    private var stable = 0

    fun initialize(frame: Bitmap): Boolean {
        val rect = candidateBounds ?: BoardCalibration.detect(frame) ?: return false
        val cells = extract(frame, rect)
        val occupied = (0..15) + (48..63)
        if (occupied.any { cells[it].density !in .09.. .80 } || (16..47).any { cells[it].density > .065 }) {
            candidate = null; candidateBounds = null; stable = 0; return false
        }
        // Reject menus and later positions: two rows of mutually matching pawns are required.
        for (row in listOf(1,6)) {
            val pawns = cells.subList(row * 8, row * 8 + 8)
            if (pawns.count { distance(it, pawns[0]) < .32 } < 7) {
                candidateBounds = null; candidate = null; stable = 0; return false
            }
        }
        val old = candidate
        stable = if (old != null && cells.indices.all { distance(cells[it], old[it]) < .12 }) stable + 1 else 1
        candidate = cells; candidateBounds = rect
        if (stable < 3) return false
        val top = (0..15).map { cells[it].luma }.average()
        val bottom = (48..63).map { cells[it].luma }.average()
        if (abs(top - bottom) < .16) return false
        val side = if (bottom > top) PlayerSide.WHITE else PlayerSide.BLACK
        val setup = InitialPosition.asSquares(side)
        // Repeated pieces must agree; different king/queen silhouettes must remain distinguishable.
        val groups = occupied.groupBy { setup[it]!! }
        if (groups.any { (_, indices) -> indices.any { distance(cells[it], cells[indices[0]]) > .36 } }) return false
        for (king in listOf('K','k')) {
            val queen = if (king == 'K') 'Q' else 'q'
            if (distance(cells[setup.indexOf(king)], cells[setup.indexOf(queen)]) < .065) return false
        }
        groups.forEach { (p, indices) -> templates[p] = indices.map { cells[it] }.toMutableList() }
        bounds = rect; playerSide = side
        return true
    }
    fun read(frame: Bitmap): List<Char?>? {
        val b = bounds ?: return null
        if (b.right > frame.width || b.bottom > frame.height) return null
        val shapes = extract(frame, b)
        val observed = mutableListOf<Char?>()
        for (shape in shapes) {
            if (shape.density < .055) { observed.add(null); continue }
            val scores = templates.map { (piece, examples) -> piece to examples.minOf { distance(shape, it) } }.sortedBy { it.second }
            val best = scores.first()
            if (best.second > .30 || scores[1].second - best.second < .025) return null
            observed.add(best.first)
        }
        return if (playerSide == PlayerSide.BLACK) observed.reversed() else observed
    }
    private fun extract(frame: Bitmap, b: Rect) = (0..63).map { shape(frame, BoardCalibration.square(b, it / 8, it % 8)) }
    private fun shape(frame: Bitmap, q: Rect): Shape {
        // Median corner colour suppresses move-highlight backgrounds without removing piece edges.
        val corners = listOf(.06 to .06, .94 to .06, .06 to .94, .94 to .94).map { (x,y) ->
            frame.getPixel((q.left + q.width()*x).toInt(), (q.top + q.height()*y).toInt())
        }
        fun median(channel: (Int) -> Int) = corners.map(channel).sorted().let { (it[1]+it[2])/2 }
        val br = median(Color::red); val bg = median(Color::green); val bb = median(Color::blue)
        val mask = BooleanArray(24*24); val light = DoubleArray(24*24)
        var count = 0; var sum = 0.0
        for (r in 0..23) for (c in 0..23) {
            val p = frame.getPixel((q.left + q.width()*(.08 + .84*(c+.5)/24)).toInt(), (q.top + q.height()*(.08 + .84*(r+.5)/24)).toInt())
            val red = Color.red(p); val green = Color.green(p); val blue = Color.blue(p)
            val i = r*24+c
            mask[i] = maxOf(abs(red-br), abs(green-bg), abs(blue-bb)) > 30
            light[i] = (red + green + blue)/765.0
            if (mask[i]) { count++; sum += light[i] }
        }
        return Shape(mask, light, count/576.0, if (count == 0) 0.0 else sum/count)
    }
    private fun distance(a: Shape, b: Shape): Double {
        var union = 0; var mismatch = 0; var light = 0.0; var common = 0
        for (i in a.mask.indices) {
            if (a.mask[i] || b.mask[i]) union++
            if (a.mask[i] != b.mask[i]) mismatch++
            if (a.mask[i] && b.mask[i]) { common++; light += abs(a.light[i]-b.light[i]) }
        }
        if (union == 0) return 0.0
        return mismatch.toDouble()/union + .65 * if (common == 0) 0.0 else light/common
    }
}
