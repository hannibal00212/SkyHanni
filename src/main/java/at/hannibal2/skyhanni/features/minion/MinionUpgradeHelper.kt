package at.hannibal2.skyhanni.features.minion

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackAPI
import at.hannibal2.skyhanni.data.SackAPI.getAmountInSacksOrNull
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.MinionCloseEvent
import at.hannibal2.skyhanni.events.MinionOpenEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.setLore
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object MinionUpgradeHelper {
    private val config get() = SkyHanniMod.feature.misc.minions

    private var displayItem: ItemStack? = null
    private var itemsNeeded: Int = 0
    private var itemName: String = ""
    private var itemsInSacks: Int = 0

    private val requiredItemsPattern by RepoPattern.pattern(
        "minion.items.upgrade",
        "§7§cYou need §6(?<amount>\\d+) §cmore (?<itemName>.+)\\."
    )

    @SubscribeEvent
    fun onMinionOpen(event: MinionOpenEvent) {
        if (!config.minionConfigHelper) return
        event.inventoryItems[50]?.getLore()?.joinToString(" ")?.let { lore ->
            requiredItemsPattern.findMatcher(lore) {
                itemName = group("itemName").removeColor()
                itemsNeeded = group("amount")?.toInt() ?: 0
            } ?: resetItems()

            if (itemsInSacks > 0) {
                add("§7In sacks: §a$itemsInSacks§7x §b$itemName")
            }
        }
    }

    @SubscribeEvent
    fun onMinionClose(event: MinionCloseEvent) {
        if (!config.minionConfigHelper) return
        resetItems()
    }

    private fun resetItems() {
        itemName = ""
        itemsNeeded = 0
        itemsInSacks = 0
        displayItem = null
    }

    private fun createDisplayItem(): ItemStack {
        val itemPrice = itemName.asInternalName().getPriceOrNull() ?: 0.0

        val displayName = "§bGet required items"

        val lore = buildList {
            val itemsRemaining = itemsNeeded - itemsInSacks
            val totalCost = itemsNeeded * itemPrice
            val remainingCost = itemsRemaining * itemPrice

            add("")

            if (itemsInSacks > 0) {
                add("§7In sacks: §a$itemsInSacks§7x §b$itemName")
            }

            if (itemsRemaining > 0) {
                add("§7From Bazaar: §a$itemsRemaining§7x §b$itemName")
                add("§7Cost: §6${remainingCost.shortFormat()} §7coins")
            } else {
                add("§7All items in sacks")
            }

            add("")
            add("§7Total: §6${totalCost.shortFormat()} §7coins")

            add("")
            add(if (itemsRemaining > 0) "§e§oClick to open Bazaar" else "§e§oClick to retrieve items from the sacks")
        }

        val diamondBlockItemStack = ItemStack(Item.getItemFromBlock(Blocks.diamond_block), 1)
        return diamondBlockItemStack.setStackDisplayName(displayName).setLore(lore)
    }

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!config.minionConfigHelper) return
        if (event.inventory !is InventoryPlayer && event.slot == 51) {
            displayItem?.let { event.replace(it) }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!config.minionConfigHelper || displayItem == null || event.slotId != 51) return
        event.cancel()
        if (itemName.isNotEmpty()) {
            val remainingItems = itemsNeeded - itemsInSacks
            if (remainingItems > 0) {
                BazaarApi.searchForBazaarItem(itemName, remainingItems)
            } else {
                GetFromSackAPI.getFromSack(itemName.asInternalName(), itemsNeeded)
            }
        }
    }
}
