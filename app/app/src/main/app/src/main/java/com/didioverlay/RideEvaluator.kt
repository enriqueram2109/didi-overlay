package com.didioverlay

object RideEvaluator {

    enum class Decision {
        GREEN, YELLOW, RED
    }

    fun evaluate(amount: Int, minutes: Int): Decision {
        return when {
            amount >= 90 && minutes <= 30 -> Decision.GREEN
            amount >= 70 && minutes <= 40 -> Decision.YELLOW
            else -> Decision.RED
        }
    }
}
