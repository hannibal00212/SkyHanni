package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.DisplayTableEntry
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.UtilsPatterns
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object MaskShopPrice {
    private val config get() = SkyHanniMod.feature.event.carnival.maskShopPrice

    private var display = emptyList<Renderable>()
    private var products = emptyList<Product>()

    private var inInventory = false
    private var inventoryItems = emptyMap<Int, ItemStack>()

    val patternGroup = RepoPattern.group("carnival.shop")
    private val tokenAmountPattern by patternGroup.pattern(
        "tokencost",
        "(?<amount>[\\d,]+) Carnival Tokens",
    )
    private val inventoryPattern by patternGroup.pattern(
        "mask.inventoryname",
        "Carnival Masks"
    )

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (inInventory) {
            update()
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (!inventoryPattern.matches(event.inventoryName)) return
        inInventory = true
        inventoryItems = event.inventoryItems
        update()
    }

    private fun updateProducts() {
        val newProducts = mutableListOf<Product>()
        for ((slot, item) in inventoryItems) {
            val lore = item.getLore()

            val carnivalTokens = getTokenBuyCost(lore) ?: continue
            val internalName = item.getInternalName()
            val itemPrice = internalName.getPriceOrNull() ?: continue

            newProducts.add(Product(slot, item.itemName, internalName, carnivalTokens, itemPrice))
        }
        products = newProducts
    }

    private fun update() {
        updateProducts()

        val multiplier = 100
        // TODO merge core with SkyMartCopperPrice into a utils
        val table = mutableListOf<DisplayTableEntry>()

        for (product in products) {
            val factor = (product.itemPrice / product.carnivalTokens) * multiplier
            val perFormat = factor.shortFormat()

            val hover = buildList {
                add(product.name)
                add("")
                add("§7Item price: §6${product.itemPrice.shortFormat()} ")
                add("§7Carnival Token cost: §c${product.carnivalTokens.shortFormat()} ")
                add("§7Profit per 100 Carnival Tokens: §6$perFormat ")
            }
            table.add(
                DisplayTableEntry(
                    "${product.name}§f:",
                    "§6§l$perFormat",
                    factor,
                    product.item,
                    hover,
                    highlightsOnHoverSlots = product.slot?.let { listOf(it) } ?: emptyList(),
                ),
            )
        }

        display = buildList {
            add(Renderable.string("§e§lCoins per 100 Carnival Tokens§f:"))
            add(LorenzUtils.fillTable(table, padding = 5, itemScale = config.itemScale))
        }
    }

    private fun getTokenBuyCost(lore: List<String>): Long? {
        val nextLine = lore.nextAfter({ UtilsPatterns.costLinePattern.matches(it) }) ?: return null
        return tokenAmountPattern.matchMatcher(nextLine.removeColor()) {
            group("amount").formatLong()
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (inInventory) {
            config.position.renderRenderables(
                display,
                extraSpace = 5,
                posLabel = "Mask Shop Price",
            )
        }
    }

    private fun isEnabled() = LorenzUtils.skyBlockArea == "Carnival" && config.enabled

    private data class Product(
        var slot: Int?,
        val name: String,
        val item: NEUInternalName,
        val carnivalTokens: Long,
        val itemPrice: Double,
    )
}
