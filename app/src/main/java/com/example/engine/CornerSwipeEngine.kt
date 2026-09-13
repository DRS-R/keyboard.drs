package com.example.engine

import kotlin.math.hypot

enum class KeySwipeDirection {
    NONE,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    UP,
    DOWN
}

object CornerSwipeEngine {
    private const val DISTANCE_THRESHOLD_PX = 32f

    fun detectDirection(startX: Float, startY: Float, endX: Float, endY: Float): KeySwipeDirection {
        val dx = endX - startX
        val dy = endY - startY
        val distance = hypot(dx, dy)

        if (distance < DISTANCE_THRESHOLD_PX) {
            return KeySwipeDirection.NONE
        }

        return when {
            dx < 0 && dy < 0 -> KeySwipeDirection.TOP_LEFT
            dx >= 0 && dy < 0 -> KeySwipeDirection.TOP_RIGHT
            dx < 0 && dy >= 0 -> KeySwipeDirection.BOTTOM_LEFT
            else -> KeySwipeDirection.BOTTOM_RIGHT
        }
    }
}
