package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI.partyModeReplace
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.getOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryTooltipCompact {
    private val config get() = ChocolateFactoryAPI.config

    private var lastClick = SimpleTimeMark.farPast()
    private var lastHover = SimpleTimeMark.farPast()
    private var tooltipToHover = listOf<String>()

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return

        if (config.tooltipMove) {
            if (event.slot.slotNumber <= 44) {
                lastHover = SimpleTimeMark.now()
                tooltipToHover = event.toolTip.toList().map { it.partyModeReplace() }
                event.cancel()
            } else {
                lastHover = SimpleTimeMark.farPast()
            }
            return
        }

        onCompactClick(event)
    }

    @HandleEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!ChocolateFactoryAPI.inChocolateFactory) return
        if (config.tooltipMove) {
            if (lastHover.passedSince() < 1.seconds) {
                config.tooltipMovePosition.renderStrings(tooltipToHover, posLabel = "Tooltip Move")
            }
        }
    }

    private fun onCompactClick(event: LorenzToolTipEvent) {
        if (!config.compactOnClick) return

        val itemStack = event.itemStack
        val lore = itemStack.getLore()
        if (!lore.any { it == "§7§eClick to uncover the meaning of life!" }) return
        if (lastClick.passedSince() >= 1.seconds && !config.compactOnClickAlways) return
        val list = mutableListOf<String>()
        list.add(itemStack.name)
        lore.getOrNull(5)?.let {
            list.add(it)
        }
        event.toolTip = list
        return
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {

        if (ChocolateFactoryAPI.inChocolateFactory) {
            if (event.slotId == 13) {
                lastClick = SimpleTimeMark.now()
            }
        }
    }
}
