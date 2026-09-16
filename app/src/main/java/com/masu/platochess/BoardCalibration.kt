package com.masu.platochess

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import kotlin.math.abs

object BoardCalibration {
    fun estimate(w: Int, h: Int): Rect {
        val l = (w * .025f).toInt(); val size = (w * .95f).toInt()
        val t = (h * .27f).toInt().coerceAtMost((h - size).coerceAtLeast(0))
        return Rect(l, t, l + size, t + size)
    }
    fun square(b: Rect, row: Int, col: Int) = Rect(
        b.left + col * b.width() / 8, b.top + row * b.height() / 8,
        b.left + (col + 1) * b.width() / 8, b.top + (row + 1) * b.height() / 8)

    /** Find the empty middle four ranks of a NEW game, never infer a board from screen size alone. */
    fun detect(frame: Bitmap): Rect? {
        var best: Rect? = null; var score = Double.MAX_VALUE
        val step = maxOf(3, frame.width / 100)
        for (percent in 76..100 step 2) {
            val size = frame.width * percent / 100
            if (size > frame.height) continue
            for (left in 0..(frame.width - size) step step) {
                for (top in (frame.height / 12)..(frame.height - size) step step) {
                    val rect = Rect(left, top, left + size, top + size)
                    val v = gridScore(frame, rect)
                    if (v < score) { score = v; best = rect }
                }
            }
        }
        val coarse = best ?: return null
        // Refine all three parameters at pixel resolution near the best coarse candidate.
        for (size in (coarse.width() - step)..(coarse.width() + step) step 2)
            for (left in (coarse.left - step)..(coarse.left + step) step 2)
                for (top in (coarse.top - step)..(coarse.top + step) step 2) {
                    if (left < 0 || top < 0 || left + size > frame.width || top + size > frame.height) continue
                    val rect = Rect(left, top, left + size, top + size)
                    val v = gridScore(frame, rect)
                    if (v < score) { best = rect; score = v }
                }
        return best?.takeIf { score < 16.0 }
    }
    private fun rgb(p: Int) = intArrayOf(Color.red(p), Color.green(p), Color.blue(p))
    private fun gridScore(frame: Bitmap, b: Rect): Double {
        val sums = Array(2) { DoubleArray(3) }
        val samples = Array(2) { mutableListOf<IntArray>() }
        for (r in 2..5) for (c in 0..7) {
            val parity = (r + c) % 2
            // Wide within-cell samples penalize wrong grid size and offset.
            for (dy in listOf(.12, .5, .88)) for (dx in listOf(.12, .5, .88)) {
                val p = rgb(frame.getPixel((b.left + (c + dx) * b.width() / 8).toInt(), (b.top + (r + dy) * b.height() / 8).toInt()))
                samples[parity].add(p)
                for (k in 0..2) sums[parity][k] += p[k]
            }
        }
        for (a in 0..1) for (k in 0..2) sums[a][k] /= samples[a].size
        val contrast = (0..2).sumOf { abs(sums[0][it] - sums[1][it]) } / 3
        if (contrast < 18) return Double.MAX_VALUE
        var error = 0.0
        for (a in 0..1) for (p in samples[a]) for (k in 0..2) error += abs(p[k] - sums[a][k])
        return error / (samples.sumOf { it.size } * 3) + 60.0 / contrast
    }
}
