package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.OwnInventoryData
import at.hannibal2.skyhanni.data.bazaar.HypixelBazaarFetcher
import at.hannibal2.skyhanni.events.BazaarOpenedProductEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.InventoryUtils.getUpperItems
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.itemNameWithoutColor
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.equalsIgnoreColor
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BazaarApi {

    private var loadedNpcPriceData = false

    val holder = BazaarDataHolder()
    var inBazaarInventory = false
    private var currentSearchedItem = ""

    var currentlyOpenedProduct: NEUInternalName? = null
    var orderOptionProduct: NEUInternalName? = null

    fun NEUInternalName.getBazaarData(): BazaarData? = HypixelBazaarFetcher.latestProductInformation[this]

    fun NEUInternalName.getBazaarDataOrError(): BazaarData = getBazaarData() ?: run {
        ErrorManager.skyHanniError(
            "Can not find bazaar data for $itemName",
            "internal name" to this,
        )
    }

    fun isBazaarItem(stack: ItemStack): Boolean = stack.getInternalName().isBazaarItem()

    fun NEUInternalName.isBazaarItem() = getBazaarData() != null

    fun searchForBazaarItem(internalName: NEUInternalName, amount: Int = -1) {
        searchForBazaarItem(internalName.itemNameWithoutColor, amount)
    }

    fun searchForBazaarItem(displayName: String, amount: Int? = null) {
        if (!LorenzUtils.inSkyBlock) return
        if (NEUItems.neuHasFocus()) return
        if (LorenzUtils.noTradeMode) return
        if (DungeonAPI.inDungeon() || LorenzUtils.inKuudraFight) return
        HypixelCommands.bazaar(displayName.removeColor())
        amount?.let { OSUtils.copyToClipboard(it.toString()) }
        currentSearchedItem = displayName.removeColor()
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inBazaarInventory = checkIfInBazaar(event)
        if (inBazaarInventory) {
            val openedProduct = getOpenedProduct(event.inventoryItems) ?: return
            currentlyOpenedProduct = openedProduct
            BazaarOpenedProductEvent(openedProduct, event).postAndCatch()
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        val item = event.item ?: return
        val itemName = item.name
        if (isBazaarOrderInventory(InventoryUtils.openInventoryName())) {
            val internalName = item.getInternalNameOrNull() ?: return
            if (itemName.contains("SELL")) {
                orderOptionProduct = internalName
            } else if (itemName.contains("BUY")) {
                // pickup items from bazaar order
                OwnInventoryData.ignoreItem(1.seconds) { it == internalName }
            }
        }
        if (InventoryUtils.openInventoryName() == "Order options" && itemName == "§cCancel Order") {
            // pickup items from own bazaar order
            OwnInventoryData.ignoreItem(1.seconds) { it == orderOptionProduct }
        }
    }

    private fun getOpenedProduct(inventoryItems: Map<Int, ItemStack>): NEUInternalName? {
        val buyInstantly = inventoryItems[10] ?: return null

        if (buyInstantly.displayName != "§aBuy Instantly") return null
        val bazaarItem = inventoryItems[13] ?: return null

        return NEUInternalName.fromItemName(bazaarItem.displayName)
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {

        if (!loadedNpcPriceData) {
            loadedNpcPriceData = true
            holder.start()
        }
    }

    // TODO cache
    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!inBazaarInventory) return
        if (!SkyHanniMod.feature.inventory.bazaar.purchaseHelper) return
        if (currentSearchedItem == "") return

        if (event.gui !is GuiChest) return
        val guiChest = event.gui
        val chest = guiChest.inventorySlots as ContainerChest

        for ((slot, stack) in chest.getUpperItems()) {
            if (chest.inventorySlots.indexOf(slot) !in 9..44) {
                continue
            }

            if (stack.displayName.removeColor() == currentSearchedItem) {
                slot highlight LorenzColor.GREEN
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!inBazaarInventory) return
        // TODO USE SH-REPO
        // TODO remove dynamic pattern
        "\\[Bazaar] (Buy Order Setup!|Bought).*$currentSearchedItem.*".toPattern()
            .matchMatcher(event.message.removeColor()) { currentSearchedItem = "" }
    }

    private fun checkIfInBazaar(event: InventoryFullyOpenedEvent): Boolean {
        val items = event.inventorySize.let { listOf(it - 5, it - 6) }.mapNotNull { event.inventoryItems[it] }
        if (items.any { it.name.equalsIgnoreColor("Go Back") && it.getLore().firstOrNull() == "§7To Bazaar" }) {
            return true
        }

        if (event.inventoryName.startsWith("Bazaar ➜ ")) return true
        return when (event.inventoryName) {
            "How many do you want?" -> true
            "How much do you want to pay?" -> true
            "Confirm Buy Order" -> true
            "Confirm Instant Buy" -> true
            "At what price are you selling?" -> true
            "Confirm Sell Offer" -> true
            "Order options" -> true

            else -> false
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(25, "bazaar", "inventory.bazaar")
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inBazaarInventory = false
        currentlyOpenedProduct = null
    }

    fun isBazaarOrderInventory(inventoryName: String): Boolean = when (inventoryName) {
        "Your Bazaar Orders" -> true
        "Co-op Bazaar Orders" -> true
        else -> false
    }
}
