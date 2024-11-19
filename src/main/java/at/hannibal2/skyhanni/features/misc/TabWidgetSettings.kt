package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object TabWidgetSettings {
    private val patternGroup = RepoPattern.group("tab.widget.setting")

    /**
     * REGEX-TEST: Widgets on Private Island
     * REGEX-TEST: Widgets in Crystal Hollows
     */
    private val mainPageSettingPattern by patternGroup.pattern(
        "gui",
        "(Widgets in.*|Widgets on.*)",
    )

    /**
     * REGEX-TEST: §7Currently: §aALWAYS ENABLED
     * REGEX-TEST: §7Currently: §cDISABLED
     */
    private val mainPageWidgetPattern by patternGroup.pattern(
        "main",
        "§7Currently:.*",
    )

    /**
     * REGEX-TEST: §eClick to disable!
     * REGEX-TEST: §eClick to edit!
     */
    private val subPageWidgetPattern by patternGroup.pattern(
        "sub",
        "§eClick to .*",
    )

    /**
     * REGEX-TEST: Profile Widget Settings
     */
    private val shownSettingPattern by patternGroup.pattern(
        "show",
        "Shown .* Setting.*|.*Widget Settings",
    )

    /**
     * REGEX-TEST: §eClick to disable!
     */
    private val clickToDisablePattern by patternGroup.pattern(
        "click.disable",
        ".*(disable!)",
    )

    /**
     * REGEX-TEST: §7Currently: §aENABLED
     */
    private val enabledPattern by patternGroup.pattern(
        "is.enabled",
        ".*ENABLED",
    )

    var inInventory = false
    var highlights = mutableMapOf<Int, LorenzColor>()

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        highlights.clear()

        val inventoryName = event.inventoryName
        if (mainPageSettingPattern.matches(inventoryName)) {
            inInventory = true
            val items = event.inventoryItems.filter { mainPageWidgetPattern.anyMatches(it.value.getLore()) }
            for ((slot, stack) in items) {
                highlights[slot] = if (enabledPattern.anyMatches(stack.getLore())) {
                    LorenzColor.GREEN
                } else {
                    LorenzColor.RED
                }
            }
        }

        if (shownSettingPattern.matches(inventoryName)) {
            inInventory = true
            val items = event.inventoryItems.filter {
                subPageWidgetPattern.matches(it.value.getLore().lastOrNull())
            }

            for ((slot, stack) in items) {
                highlights[slot] = if (clickToDisablePattern.anyMatches(stack.getLore())) {
                    LorenzColor.GREEN
                } else {
                    LorenzColor.RED
                }
            }
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        highlights.clear()
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        event.gui.inventorySlots.inventorySlots
            .associateWith { highlights[it.slotNumber] }
            .forEach { (slot, color) ->
                color?.let { slot.highlight(it) }
            }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && SkyHanniMod.feature.inventory.highlightWidgets
}
