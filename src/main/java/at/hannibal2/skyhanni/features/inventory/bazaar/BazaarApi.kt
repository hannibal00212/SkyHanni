package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.OwnInventoryData
import at.hannibal2.skyhanni.data.bazaar.HypixelBazaarFetcher
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.bazaar.BazaarOpenedProductEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
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
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.StringUtils.equalsIgnoreColor
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BazaarApi {

    private var loadedNpcPriceData = false

    val holder = HypixelItemApi()
    var inBazaarInventory = false
    private var currentSearchedItem = ""

    var currentlyOpenedProduct: NeuInternalName? = null
    var orderOptionProduct: NeuInternalName? = null

    private val patternGroup = RepoPattern.group("inventory.bazaar")

    /**
     * REGEX-TEST: [Bazaar] Bought 1x Small Storage for 3,999.5 coins!
     */
    private val resetCurrentSearchPattern by patternGroup.pattern(
        "reset-current-search",
        "\\[Bazaar] (?:Buy Order Setup!|Bought) \\d+x (?<item>.*) for .*",
    )

    /**
     * REGEX-TEST: Bazaar ➜ Coal
     * REGEX-TEST: How many do you want?
     * REGEX-TEST: Confirm Buy Order
     * REGEX-TEST: Confirm Buy Order
     * REGEX-TEST: Order options
     */
    private val inventoryNamePattern by patternGroup.list(
        "inventory-name",
        "Bazaar ➜ .*",
        "How many do you want\\?",
        "How much do you want to pay\\?",
        "Confirm Buy Order",
        "Confirm Instant Buy",
        "At what price are you selling\\?",
        "Confirm Sell Offer",
        "Order options",
    )

    /**
     * REGEX-TEST: Your Bazaar Orders
     * REGEX-TEST: Co-op Bazaar Orders
     */
    private val inventoryBazaarOrdersPattern by patternGroup.list(
        "inventory-bazaar-orders",
        "Your Bazaar Orders",
        "Co-op Bazaar Orders",
    )

    fun NeuInternalName.getBazaarData(): BazaarData? = HypixelBazaarFetcher.latestProductInformation[this]

    fun NeuInternalName.getBazaarDataOrError(): BazaarData = getBazaarData() ?: run {
        ErrorManager.skyHanniError(
            "Can not find bazaar data for $itemName",
            "internal name" to this,
        )
    }

    fun isBazaarItem(stack: ItemStack): Boolean = stack.getInternalName().isBazaarItem()

    fun NeuInternalName.isBazaarItem() = getBazaarData() != null

    fun searchForBazaarItem(internalName: NeuInternalName, amount: Int = -1) {
        searchForBazaarItem(internalName.itemNameWithoutColor, amount)
    }

    fun searchForBazaarItem(displayName: String, amount: Int? = null) {
        if (!LorenzUtils.inSkyBlock) return
        if (NeuItems.neuHasFocus()) return
        if (LorenzUtils.noTradeMode) return
        if (DungeonApi.inDungeon() || LorenzUtils.inKuudraFight) return
        HypixelCommands.bazaar(displayName.removeColor())
        amount?.let { OSUtils.copyToClipboard(it.toString()) }
        currentSearchedItem = displayName.removeColor()
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        inBazaarInventory = checkIfInBazaar(event)
        if (inBazaarInventory) {
            val openedProduct = getOpenedProduct(event.inventoryItems) ?: return
            currentlyOpenedProduct = openedProduct
            BazaarOpenedProductEvent(openedProduct, event).post()
        }
    }

    @HandleEvent
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

    private fun getOpenedProduct(inventoryItems: Map<Int, ItemStack>): NeuInternalName? {
        val buyInstantly = inventoryItems[10] ?: return null

        if (buyInstantly.displayName != "§aBuy Instantly") return null
        val bazaarItem = inventoryItems[13] ?: return null

        return NeuInternalName.fromItemName(bazaarItem.displayName)
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {

        if (!loadedNpcPriceData) {
            loadedNpcPriceData = true
            holder.start()
        }
    }

    // TODO cache
    @HandleEvent(onlyOnSkyblock = true)
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
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

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message.removeColor()
        val item = resetCurrentSearchPattern.matchMatcher(message) {
            group("item")
        } ?: return

        if (currentSearchedItem == item) {
            currentSearchedItem = ""
        }
    }

    private fun checkIfInBazaar(event: InventoryFullyOpenedEvent): Boolean {
        val items = event.inventorySize.let { listOf(it - 5, it - 6) }.mapNotNull { event.inventoryItems[it] }
        if (items.any { it.name.equalsIgnoreColor("Go Back") && it.getLore().firstOrNull() == "§7To Bazaar" }) {
            return true
        }

        // check for Buy Instantly
        event.inventoryItems[16]?.let {
            if (it.name == "§aCustom Amount" && it.getLore().firstOrNull() == "§8Buy Order Quantity") {
                return true
            }
        }

        if (isBazaarOrderInventory(event.inventoryName)) return true
        return inventoryNamePattern.matches(event.inventoryName)
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(25, "bazaar", "inventory.bazaar")
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inBazaarInventory = false
        currentlyOpenedProduct = null
    }

    fun isBazaarOrderInventory(inventoryName: String): Boolean = inventoryBazaarOrdersPattern.matches(inventoryName)
}
