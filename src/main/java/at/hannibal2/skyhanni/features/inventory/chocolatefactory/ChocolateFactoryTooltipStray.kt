package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ChocolateFactoryTooltipStray {
    private val config get() = ChocolateFactoryAPI.config

    /**
     * REGEX-TEST: §5§o§7You gained §6+2,465,018 Chocolate§7!
     * REGEX-TEST: §5§o§7gained §6+30,292 Chocolate§7!
     * REGEX-TEST: §5§o§7§6+36,330 Chocolate§7!
     * REGEX-TEST: §5§o§9Rabbit§7, so you received §655,935,257
     */
    private val chocolateGainedPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.stray",
        "(?:§.)*(?:Rabbit§7, so )?(?:[Yy]ou )?(?:gained |received )?§6\\+?(?<amount>[\\d,]+)(?: Chocolate§7!)?"
    )

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        if (!config.showStrayTime) return
        if (event.slot.slotNumber > 26 || event.slot.slotNumber == ChocolateFactoryAPI.infoIndex) return

        val tooltip = event.toolTip
        tooltip.matchFirst(chocolateGainedPattern) {
            val amount = group("amount").formatLong()
            val format = ChocolateFactoryAPI.timeUntilNeed(amount + 1).format(maxUnits = 2)
            tooltip[tooltip.lastIndex] += " §7(§a+§b$format §aof production§7)"
        }
    }
}
