package com.masu.platochess

import android.graphics.Rect

/** Initial calibration for the supplied portrait Plato screenshots. Values are normalized so other resolutions scale. */
object BoardCalibration {
    fun estimate(width: Int, height: Int): Rect {
        val left = (width * 0.025f).toInt()
        val right = (width * 0.975f).toInt()
        val top = (height * 0.27f).toInt()
        val size = right - left
        return Rect(left, top, right, top + size)
    }

    fun square(board: Rect, row: Int, col: Int): Rect {
        val s = board.width() / 8f
        return Rect((board.left + col*s).toInt(), (board.top + row*s).toInt(), (board.left + (col+1)*s).toInt(), (board.top + (row+1)*s).toInt())
    }
}