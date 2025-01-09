package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object EventCounter {

    private val config get() = SkyHanniMod.feature.dev.debug

    private val map = mutableMapOf<String, Int>()
    private var lastUpdate = SimpleTimeMark.farPast()

    private var enabled = false

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        enabled = LorenzUtils.onHypixel && config.eventCounter
    }

    fun count(eventName: String) {
        if (!enabled) return

        map.addOrPut(eventName, 1)

        if (lastUpdate == SimpleTimeMark.farPast()) {
            lastUpdate = SimpleTimeMark.now()
        }

        if (lastUpdate.passedSince() > 1.seconds) {
            lastUpdate = SimpleTimeMark.now()

            print(map)

            map.clear()
        }
    }

    private fun print(map: MutableMap<String, Int>) {
        println("")
        var total = 0
        for ((name, amount) in map.entries.sortedBy { it.value }) {
            println("$name (${amount.addSeparators()} times)")
            total += amount
        }
        println("")
        println("total: ${total.addSeparators()}")
    }
}
