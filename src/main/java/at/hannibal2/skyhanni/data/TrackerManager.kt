package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatIntOrUserError
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TrackerManager {

    private var hasChanged = false
    var dirty = false
    var commandEditTrackerSuccess = false

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val config = SkyHanniMod.feature.misc.tracker.hideCheapItems
        ConditionalUtils.onToggle(config.alwaysShowBest, config.minPrice, config.enabled) {
            hasChanged = true
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderOverlayFirst(event: GuiRenderEvent) {
        if (hasChanged) {
            dirty = true
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onRenderOverlayLast(event: GuiRenderEvent) {
        if (hasChanged) {
            dirty = false
            hasChanged = false
        }
    }

    fun commandEditTracker(args: Array<String>) {
        if (args.size < 2) {
            ChatUtils.userError("Usage: /shedittracker <item name> <amount>")
            return
        }

        val amount = args.last().formatIntOrUserError() ?: return
        if (amount == 0) {
            ChatUtils.userError("Amount can not be zero!")
            return
        }

        val rawName = args.dropLast(1).joinToString(" ")
        val internalName = NEUInternalName.fromItemNameOrInternalName(rawName)
        if (!internalName.isKnownItem()) {
            ChatUtils.chat("No item found for '$rawName'!")
            return
        }

        commandEditTrackerSuccess = false
        ItemAddEvent(internalName, amount, ItemAddManager.Source.COMMAND).postAndCatch()
        if (!commandEditTrackerSuccess) {
            ChatUtils.userError("Could not edit the Item Tracker! Does this item belong to this tracker? Is the tracker active right now?")
        }
    }
}
