package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.events.DateChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.time.LocalDate

@SkyHanniModule
object DateAPI {
    var date: LocalDate? = null

    @SubscribeEvent
    fun onSecond(event: SecondPassedEvent) {
        val now = LocalDate.now()
        if (now != date) {
            date?.let {
                DateChangeEvent(it, now).post()
            }
            date = now
        }
    }
}
