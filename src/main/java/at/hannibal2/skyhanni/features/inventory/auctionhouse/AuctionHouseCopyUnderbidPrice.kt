package at.hannibal2.skyhanni.features.inventory.auctionhouse

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object AuctionHouseCopyUnderbidPrice {

    private val config get() = SkyHanniMod.feature.inventory.auctions

    private val patternGroup = RepoPattern.group("auctions.underbid")
    private val auctionPricePattern by patternGroup.pattern(
        "price",
        "§7(?:Buy it now|Starting bid|Top bid): §6(?<coins>[0-9,]+) coins",
    )
    private val allowedInventoriesPattern by patternGroup.pattern(
        "allowedinventories",
        "Auctions Browser|Manage Auctions|Auctions: \".*\"?",
    )

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!LorenzUtils.inSkyBlock) return
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

    @SubscribeEvent
    fun onKeybind(event: GuiKeyPressEvent) {
        if (!config.copyUnderbidKeybind.isKeyHeld()) return
        if (!LorenzUtils.inSkyBlock) return
        if (!allowedInventoriesPattern.matches(InventoryUtils.openInventoryName())) return
        val stack = event.guiContainer.slotUnderMouse?.stack ?: return

        auctionPricePattern.firstMatcher(stack.getLore()) {
            val underbid = group("coins").formatLong() - 1
            OSUtils.copyToClipboard("$underbid")
            ChatUtils.chat("Copied ${underbid.addSeparators()} to clipboard.")
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(25, "inventory.copyUnderbidPrice", "inventory.auctions.autoCopyUnderbidPrice")
    }
}
