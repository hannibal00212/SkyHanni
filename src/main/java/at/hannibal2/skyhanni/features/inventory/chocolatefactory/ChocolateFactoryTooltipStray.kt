package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.TimeUtils.format

@SkyHanniModule
object ChocolateFactoryTooltipStray {
    private val config get() = ChocolateFactoryApi.config

    /**
     * REGEX-TEST: §5§o§7You gained §6+2,465,018 Chocolate§7!
     * REGEX-TEST: §5§o§7gained §6+30,292 Chocolate§7!
     * REGEX-TEST: §5§o§7§6+36,330 Chocolate§7!
     * REGEX-TEST: §5§o§9Rabbit§7, so you received §655,935,257
     */
    private val chocolateGainedPattern by ChocolateFactoryApi.patternGroup.pattern(
        "rabbit.stray",
        "(?:§.)*(?:Rabbit§7, so )?(?:[Yy]ou )?(?:gained |received )?§6\\+?(?<amount>[\\d,]+)(?: Chocolate§7!)?",
    )

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onTooltip(event: ToolTipEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory) return
        if (!config.showStrayTime) return
        if (event.slot.slotNumber > 26 || event.slot.slotNumber == ChocolateFactoryApi.infoIndex) return

        val tooltip = event.toolTip
        chocolateGainedPattern.firstMatcher(tooltip) {
            val amount = group("amount").formatLong()
            val format = ChocolateFactoryApi.timeUntilNeed(amount + 1).format(maxUnits = 2)
            tooltip[tooltip.lastIndex] += " §7(§a+§b$format §aof production§7)"
        }
    }
}
