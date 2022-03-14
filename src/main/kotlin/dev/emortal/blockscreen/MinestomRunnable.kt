package dev.emortal.blockscreen

import java.time.Duration
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

abstract class MinestomRunnable(val delay: Duration = Duration.ZERO, val repeat: Duration = Duration.ZERO, var iterations: Int = -1, timer: Timer = defaultTimer) {

    companion object {
        val defaultTimer = Timer()
    }

    private var task: TimerTask? = null

    init {
        task = timer.scheduleAtFixedRate(delay.toMillis(), repeat.toMillis()) {
            if (iterations != -1 && currentIteration >= iterations) {
                this@MinestomRunnable.cancel()
                cancelled()
                return@scheduleAtFixedRate
            }

            this@MinestomRunnable.run()
            currentIteration++
        }
    }
    var currentIteration = 0

    abstract fun run()
    open fun cancelled() {}

    fun cancel() {
        task?.cancel()
    }
}