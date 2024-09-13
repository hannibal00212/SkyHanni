package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.getBazaarDataOrError
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils.getInventoryName
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object BazaarOrderHelper {
    private val patternGroup = RepoPattern.group("bazaar.orderhelper")
    private val bazaarItemNamePattern by patternGroup.pattern(
        "itemname",
        "§.§l(?<type>BUY|SELL) (?<name>.*)",
    )
    private val filledPattern by patternGroup.pattern(
        "filled",
        "§7Filled: §[a6].*§7/.* §a§l100%!",
    )
    private val pricePattern by patternGroup.pattern(
        "price",
        "§7Price per unit: §6(?<number>.*) coins",
    )

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!SkyHanniMod.feature.inventory.bazaar.orderHelper) return
        if (event.gui !is GuiChest) return

        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest
        val inventoryName = chest.getInventoryName()
        if (!BazaarApi.isBazaarOrderInventory(inventoryName)) return

        for ((slot, stack) in chest.getUpperItems()) {
            bazaarItemNamePattern.matchMatcher(stack.name) {
                val buyOrSell = group("type").let { (it == "BUY") to (it == "SELL") }
                if (buyOrSell.let { !it.first && !it.second }) return

                highlightItem(group("name"), slot, buyOrSell)
            }
        }
    }

    private fun highlightItem(itemName: String, slot: Slot, buyOrSell: Pair<Boolean, Boolean>) {
        val data = NEUInternalName.fromItemName(itemName).getBazaarDataOrError()

        val itemLore = slot.stack.getLore()
        for (line in itemLore) {
            filledPattern.matchMatcher(line) {
                slot highlight LorenzColor.GREEN
                return
            }

            pricePattern.matchMatcher(line) {
                val price = group("number").formatDouble()
                if (buyOrSell.first && price < data.instantBuyPrice || buyOrSell.second && price > data.sellOfferPrice) {
                    slot highlight LorenzColor.GOLD
                    return
                }
            }
        }
    }
}
