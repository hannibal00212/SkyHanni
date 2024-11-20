package at.hannibal2.skyhanni.features.stranded

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HighlightPlaceableNpcs {

    private val config get() = SkyHanniMod.feature.misc.stranded

    private val patternGroup = RepoPattern.group("stranded.highlightplacement")

    // TODO Please add regex tests
    private val locationPattern by patternGroup.pattern(
        "location",
        "§7Location: §f\\[§e\\d+§f, §e\\d+§f, §e\\d+§f]",
    )
    private val clickToSetPattern by RepoPattern.pattern(
        "clicktoset",
        "§7§eClick to set the location of this NPC!",
    )
    private val clickToSpawnPattern by RepoPattern.pattern(
        "clicktospawn",
        "§elocation!",
    )

    private var inInventory = false
    private var highlightedItems = emptyList<Int>()

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inInventory = false
        if (!isEnabled()) return

        if (event.inventoryName != "Island NPCs") return

        val highlightedItems = mutableListOf<Int>()
        for ((slot, stack) in event.inventoryItems) {
            if (isPlaceableNpc(stack.getLore())) {
                highlightedItems.add(slot)
            }
        }
        inInventory = true
        this.highlightedItems = highlightedItems
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        highlightedItems = emptyList()
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled() || !inInventory) return
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex in highlightedItems) {
                slot highlight LorenzColor.GREEN
            }
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(31, "stranded", "misc.stranded")
    }

    private fun isPlaceableNpc(lore: List<String>): Boolean {
        // Checking if NPC & placeable
        if (lore.isEmpty() || !(clickToSetPattern.matches(lore.last()) || clickToSpawnPattern.matches(lore.last()))) {
            return false
        }

        // Checking if is already placed
        return lore.none { locationPattern.matches(it) }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && LorenzUtils.isStrandedProfile && config.highlightPlaceableNpcs
}
