package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.RenderUtils.highlight

@SkyHanniModule
object ChocolateFactoryInventory {

    private val config get() = ChocolateFactoryApi.config

    /**
     * REGEX-TEST: §7§aYou have 1 unclaimed reward!
     * REGEX-TEST: §7§aYou have 2 unclaimed rewards!
     */
    private val unclaimedRewardsPattern by ChocolateFactoryApi.patternGroup.pattern(
        "unclaimedrewards",
        "§7§aYou have \\d+ unclaimed rewards?!",
    )

    @HandleEvent
    fun onForegroundDrawn(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory) return
        if (!config.highlightUpgrades) return


        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.stack == null) continue
            val slotIndex = slot.slotNumber

            if (slotIndex == ChocolateFactoryApi.bestPossibleSlot) {
                event.drawSlotText(slot.xDisplayPosition + 18, slot.yDisplayPosition, "§6✦", 1f)
            }
        }
    }

    @HandleEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory) return
        if (!config.highlightUpgrades) return

        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.stack == null) continue
            val slotIndex = slot.slotNumber

            val currentUpdates = ChocolateFactoryApi.factoryUpgrades
            currentUpdates.find { it.slotIndex == slotIndex }?.let { upgrade ->
                if (upgrade.canAfford()) {
                    slot highlight LorenzColor.GREEN.addOpacity(75)
                }
            }
            if (slotIndex == ChocolateFactoryApi.bestAffordableSlot) {
                slot highlight LorenzColor.GREEN.addOpacity(200)
            }

            if (slotIndex == ChocolateFactoryApi.barnIndex && ChocolateFactoryBarnManager.barnFull) {
                slot highlight LorenzColor.RED
            }
            if (slotIndex == ChocolateFactoryApi.milestoneIndex) {
                unclaimedRewardsPattern.firstMatcher(slot.stack?.getLore().orEmpty()) {
                    slot highlight LorenzColor.RED
                }
            }
            if (slotIndex == ChocolateFactoryApi.timeTowerIndex) {
                if (ChocolateFactoryTimeTowerManager.timeTowerActive()) {
                    slot highlight LorenzColor.LIGHT_PURPLE.addOpacity(200)
                }
                if (ChocolateFactoryTimeTowerManager.timeTowerFull()) {
                    slot highlight LorenzColor.RED
                }
            }
        }
    }

    @HandleEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory) return
        if (!config.showStackSizes) return

        val upgradeInfo = ChocolateFactoryApi.factoryUpgrades.find { it.slotIndex == event.slot.slotNumber } ?: return
        event.stackTip = upgradeInfo.stackTip()
    }

    @HandleEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!ChocolateFactoryApi.inChocolateFactory) return
        val slot = event.slot ?: return
        val slotNumber = slot.slotNumber
        if (!config.useMiddleClick) return
        if (slotNumber in ChocolateFactoryApi.noPickblockSlots &&
            (slotNumber != ChocolateFactoryApi.timeTowerIndex || event.clickedButton == 1)
        ) return

        // this would break ChocolateFactoryKeybinds otherwise
        if (event.clickType == GuiContainerEvent.ClickType.HOTBAR) return

        // if the user is holding shift, we don't want to pickblock, handled by hypixel as +10 levels for rabbits
        if (KeyboardManager.isShiftKeyDown() && slotNumber in ChocolateFactoryApi.rabbitSlots.keys) return

        event.makePickblock()
    }
}
