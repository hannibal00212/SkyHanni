package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzUtils
import kotlin.concurrent.fixedRateTimer

@SkyHanniModule
object FixedRateTimerManager {
    private var totalSeconds = 0

    init {
        fixedRateTimer(name = "skyhanni-fixed-rate-timer-manager", period = 1000L) {
            DelayedRun.onThread.execute {
                if (!LorenzUtils.onHypixel) return@execute
                SecondPassedEvent(totalSeconds).postAndCatch()
                totalSeconds++
            }
        }
    }
}
