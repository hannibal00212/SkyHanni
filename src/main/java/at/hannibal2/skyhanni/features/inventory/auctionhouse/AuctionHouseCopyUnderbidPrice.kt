package at.hannibal2.skyhanni.features.inventory.auctionhouse

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object AuctionHouseCopyUnderbidPrice {

    private val config get() = SkyHanniMod.feature.inventory.auctions

    private val patternGroup = RepoPattern.group("auctions.underbid")

    /**
     * REGEX-TEST: §7Buy it now: §61,000,000,000 coins
     * REGEX-TEST: §7Starting bid: §6200,000,000 coins
     * REGEX-TEST: §7Top bid: §6220,000 coins
     */
    private val auctionPricePattern by patternGroup.pattern(
        "price",
        "§7(?:Buy it now|Starting bid|Top bid): §6(?<coins>[0-9,]+) coins",
    )

    /**
     * REGEX-TEST: Auctions Browser
     * REGEX-TEST: Manage Auctions
     * REGEX-TEST: Auctions: "aaa"
     */
    private val allowedInventoriesPattern by patternGroup.pattern(
        "allowedinventories",
        "Auctions Browser|Manage Auctions|Auctions: \".*\"?",
    )

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!config.autoCopyUnderbidPrice) return
        if (!event.fullyOpenedOnce) return
        if (event.inventoryName != "Create BIN Auction") return
        val item = event.inventoryItems[13] ?: return

        val internalName = item.getInternalName()
        if (internalName == NEUInternalName.NONE) return

        val price = internalName.getPrice().toLong()
        if (price <= 0) {
            OSUtils.copyToClipboard("")
            return
        }
        val newPrice = price * item.stackSize - 1
        OSUtils.copyToClipboard("$newPrice")
        ChatUtils.chat("Copied ${newPrice.addSeparators()} to clipboard. (Copy Underbid Price)")
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onKeybind(event: GuiKeyPressEvent) {
        if (!config.copyUnderbidKeybind.isKeyHeld()) return
        if (!allowedInventoriesPattern.matches(InventoryUtils.openInventoryName())) return
        val stack = event.guiContainer.slotUnderMouse?.stack ?: return

        auctionPricePattern.firstMatcher(stack.getLore()) {
            val underbid = group("coins").formatLong() - 1
            OSUtils.copyToClipboard("$underbid")
            ChatUtils.chat("Copied ${underbid.addSeparators()} to clipboard.")
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(25, "inventory.copyUnderbidPrice", "inventory.auctions.autoCopyUnderbidPrice")
    }
}
