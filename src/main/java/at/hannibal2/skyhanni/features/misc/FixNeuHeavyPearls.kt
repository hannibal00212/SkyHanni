package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import io.github.moulberry.notenoughupdates.NotEnoughUpdates
import kotlin.time.Duration.Companion.hours

@SkyHanniModule(neuRequired = true)
object FixNeuHeavyPearls {

    private val config get() = SkyHanniMod.feature.misc
    private val heavyPearl = "HEAVY_PEARL".toInternalName()

    @HandleEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!isEnabled()) return

        if (event.internalName == heavyPearl && event.amount == 3) {
            val specific = NotEnoughUpdates.INSTANCE.config.getProfileSpecific()
            if (System.currentTimeMillis() > specific.dailyHeavyPearlCompleted + 1.hours.inWholeMilliseconds) {
                ChatUtils.chat("Mark NEU Heavy Pearls as done.")
                specific.dailyHeavyPearlCompleted = System.currentTimeMillis()
            }
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.fixNeuHeavyPearls
}
