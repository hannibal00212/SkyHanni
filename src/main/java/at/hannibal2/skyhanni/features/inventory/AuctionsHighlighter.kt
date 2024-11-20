package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValueCalculator
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object AuctionsHighlighter {

    private val config get() = SkyHanniMod.feature.inventory.auctions

    private val patternGroup = RepoPattern.group("auctions.highlight")
    val buyItNowPattern by patternGroup.pattern(
        "buyitnow",
        "§7Buy it now: §6(?<coins>.*) coins"
    )
    val auctionPattern by patternGroup.pattern(
        "auction",
        "§7(?:Starting bid|Top bid): §6(?<coins>.*) coins"
    )

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.highlightAuctions) return
        if (event.gui !is GuiChest) return

        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest
        if (chest.getInventoryName() != "Manage Auctions") return

        for ((slot, stack) in chest.getUpperItems()) {
            val lore = stack.getLore()
            if (lore.any { it == "§7Status: §aSold!" }) {
                slot highlight LorenzColor.GREEN
                continue
            }
            if (lore.any { it == "§7Status: §cExpired!" }) {
                slot highlight LorenzColor.RED
                continue
            }
            if (config.highlightAuctionsUnderbid) {
                buyItNowPattern.firstMatcher(lore) {
                    val coins = group("coins").formatLong()
                    val totalPrice = EstimatedItemValueCalculator.getTotalPrice(stack)
                    if (coins > totalPrice) {
                        slot highlight LorenzColor.GOLD
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(25, "inventory.highlightAuctions", "inventory.auctions.highlightAuctions")
        event.move(25, "inventory.highlightAuctionsUnderbid", "inventory.auctions.highlightAuctionsUnderbid")
    }
}
