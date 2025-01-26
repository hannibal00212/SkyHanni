package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryApi.profileStorage
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo

@SkyHanniModule
object ChocolateFactoryTooltip {

    private val config get() = ChocolateFactoryApi.config

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onTooltip(event: ToolTipEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory) return
        if (!config.extraTooltipStats) return

        val slotIndex = event.slot.slotNumber
        if (slotIndex !in ChocolateFactoryApi.otherUpgradeSlots && slotIndex !in ChocolateFactoryApi.rabbitSlots) return

        val upgradeInfo = ChocolateFactoryApi.factoryUpgrades.find { it.slotIndex == slotIndex } ?: return

        if (slotIndex == ChocolateFactoryApi.timeTowerIndex && upgradeInfo.isMaxed) {
            event.toolTip.add("§8§m-----------------")
            event.toolTip.add("§7One charge will give: §6${chocPerTimeTower().addSeparators()}")
        }

        if (upgradeInfo.isMaxed) return

        event.toolTip.add("§8§m-----------------")
        event.toolTip.add("§7Time until upgrade: §e${upgradeInfo.formattedTimeUntilGoal()}")

        if (upgradeInfo.effectiveCost == null) return

        event.toolTip.add("§7Extra: §6${upgradeInfo.extraPerSecond?.roundTo(2) ?: "N/A"} §7choc/s")
        event.toolTip.add("§7Effective Cost: §6${upgradeInfo.effectiveCost.addSeparators()}")

        if (slotIndex == ChocolateFactoryApi.timeTowerIndex) {
            event.toolTip.add("§7One charge will give: §6${chocPerTimeTower().addSeparators()}")
        }
    }

    private fun chocPerTimeTower(): Int {
        val profileStorage = profileStorage ?: return 0
        val amountPerSecond = profileStorage.rawChocPerSecond * ChocolateFactoryApi.timeTowerMultiplier()
        val amountPerHour = amountPerSecond * 60 * 60
        return amountPerHour.toInt()
    }
}
