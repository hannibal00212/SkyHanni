package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrInsert
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getSecondsHeld
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HeldTimeInLore {
    private val config get() = SkyHanniMod.feature.inventory

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.timeHeldInLore) return

        val seconds = event.itemStack.getSecondsHeld() ?: return
        val formatted = seconds.seconds.format(maxUnits = 2)

        event.toolTip.addOrInsert(10, "§7Time Held: §b$formatted")
    }
}
