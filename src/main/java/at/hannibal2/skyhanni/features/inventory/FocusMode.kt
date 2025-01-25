package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.sublistAfter
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.isTopInventory
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName

@SkyHanniModule
object FocusMode {

    private val config get() = SkyHanniMod.feature.inventory.focusMode

    private var active = false
    private var inAuctionHouse = false

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onTooltip(event: ToolTipEvent) {
        if (!isEnabled()) return
        if (event.toolTip.isEmpty()) return
        if (config.hideMenuItems) {
            event.itemStack.getInternalNameOrNull().let {
                if (it == null || it == "SKYBLOCK_MENU".toInternalName()) return
            }
            val inBazaar = BazaarApi.inBazaarInventory && event.slot.isTopInventory()
            if (inBazaar) return
        }

        val keyName = KeyboardManager.getKeyName(config.toggleKey)

        val hint = !config.disableHint && !config.alwaysEnabled && keyName != "NONE"
        if (active || config.alwaysEnabled) {
            event.toolTip = buildList {
                add(event.toolTip.first())
                if (hint) {
                    add("§7Focus Mode from SkyHanni active!")
                    add("Press $keyName to disable!")
                }
                val separator = "§5§o§8§m-----------------"
                if (inAuctionHouse && event.toolTip.contains(separator)) {
                    val ahLore = event.toolTip.sublistAfter(separator, amount = 20)
                    add(separator)
                    addAll(ahLore)
                }
            }.toMutableList()
        } else {
            if (hint) {
                event.toolTip.add(1, "§7Press $keyName to enable Focus Mode from SkyHanni!")
            }
        }
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return
        if (config.alwaysEnabled) return
        if (!config.toggleKey.isKeyClicked()) return
        active = !active
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        inAuctionHouse = event.inventoryName.startsWith("Auctions")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && InventoryUtils.inContainer() && config.enabled
}
