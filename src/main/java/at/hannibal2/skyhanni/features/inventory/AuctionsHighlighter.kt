package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUItems.getPriceOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
    private val soldPattern by patternGroup.pattern(
        "sold",
        "§7Status: §aSold!"
    )
    private val expiredPattern by patternGroup.pattern(
        "expired",
        "§7Status: §cExpired!"
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
            if (lore.any { soldPattern.matches(it) }) {
                slot highlight LorenzColor.GREEN
                continue
            }

            if (lore.any { expiredPattern.matches(it) }) {
                slot highlight LorenzColor.RED
                continue
            }
            if (config.highlightAuctionsUnderbid) {
                lore.matchFirst(buyItNowPattern) {
                    val coins = group("coins").formatLong()
                    stack.getInternalNameOrNull()?.getPriceOrNull()?.let {
                        if (coins > it) {
                            slot highlight LorenzColor.GOLD
                        }
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
