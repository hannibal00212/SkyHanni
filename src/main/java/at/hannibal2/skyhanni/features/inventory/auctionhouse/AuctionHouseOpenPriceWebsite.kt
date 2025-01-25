package at.hannibal2.skyhanni.features.inventory.auctionhouse

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.ItemStack
import java.net.URLEncoder
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AuctionHouseOpenPriceWebsite {

    private val config get() = SkyHanniMod.feature.inventory.auctions
    private var lastClick = SimpleTimeMark.farPast()

    private val patternGroup = RepoPattern.group("inventory.auctionhouse")

    /**
     * REGEX-TEST: Auctions: "hyperion"
     */
    private val ahSearchPattern by patternGroup.pattern(
        "title.search",
        "Auctions: \"(?<searchTerm>.*)\"?"
    )

    private var searchTerm = ""
    private var displayItem: ItemStack? = null

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        // TODO get search from search sign (slot 48) since it can be cut off in title
        ahSearchPattern.matchMatcher(event.inventoryName) {
            searchTerm = URLEncoder.encode(group("searchTerm").removeSuffix("\""), "UTF-8").replace("+", "%20")
            displayItem = createDisplayItem()
        }
    }

    private fun createDisplayItem() = ItemUtils.createItemStack(
        "PAPER".toInternalName().getItemStack().item,
        "§bPrice History",
        "§8(From SkyHanni)",
        "",
        "§7Click here to open",
        "§7the price history",
        "§7of §e$searchTerm",
        "§7on §csky.coflnet.com"
    )

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        displayItem = null
    }

    @HandleEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!isEnabled()) return
        if (event.inventory is InventoryPlayer) return

        if (event.slot == 8) {
            displayItem?.let {
                event.replace(it)
            }
        }
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        displayItem ?: return
        if (event.slotId != 8) return
        event.cancel()
        if (lastClick.passedSince() > 0.3.seconds) {
            val url = "https://sky.coflnet.com/api/mod/open/$searchTerm"
            OSUtils.openBrowser(url)
            lastClick = SimpleTimeMark.now()
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.openPriceWebsite
}
