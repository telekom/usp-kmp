package de.telekom.usp

import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Implementation of the retry timings for web sockets, sessions etc.
 *
 * @property name a descriptive name for this interval, used only for debugging purposes
 * @param minimumWaitInterval the minimum wait interval to use for retry duration computation
 * @param intervalMultiplier the interval multiplier to use for retry duration computation
 */
class RetryInterval(
    val name: String,
    minimumWaitInterval: Int = 5,
    intervalMultiplier: Int = 2000
) {

    private val m = minimumWaitInterval

    private val k = intervalMultiplier

    private var count = 1

    fun next(): Duration {
        val retry = count++

        return if (retry < 10) {
            val min = if (retry == 1) m else (m * (k / 1000F).pow(retry - 1)).toInt()
            val max = (m * (k / 1000F).pow(retry)).toInt()
            Random.nextInt(min, max).seconds
        } else {
            (m * (k / 1000F).pow(10)).toInt().seconds
        }
    }

    fun reset() {
        count = 1
    }

    override fun toString(): String {
        return "RetryInterval[name='$name', count=$count]"
    }
}