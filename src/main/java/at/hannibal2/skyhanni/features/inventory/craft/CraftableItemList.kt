package at.hannibal2.skyhanni.features.inventory.craft

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.SackAPI.getAmountInSacks
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.sortedDesc
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NEUItems.isVanillaItem
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.toPrimitiveStackOrNull
import at.hannibal2.skyhanni.utils.PrimitiveRecipe
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

@SkyHanniModule
object CraftableItemList {
    private val config get() = SkyHanniMod.feature.inventory.craftableItemList

    private var display = listOf<Renderable>()
    private var inInventory = false
    private val craftItemPattern by RepoPattern.pattern(
        "craftableitemlist.craftitem",
        "Craft Item",
    )

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!isEnabled()) return
        if (!craftItemPattern.matches(event.inventoryName)) return
        inInventory = true

        val pricePer = mutableMapOf<NEUInternalName, Double>()
        val lines = mutableMapOf<NEUInternalName, Renderable>()
        loadItems(pricePer, lines)

        display = if (lines.isEmpty()) {
            listOf(Renderable.string("§7No Items to craft"))
        } else {
            val items = pricePer.sortedDesc().keys.map { lines[it] ?: error("impossible") }
            listOf(
                Renderable.string("§eCraftable items (${items.size})"),
                Renderable.scrollList(items, height = 250, velocity = 20.0),
            )
        }
    }

    private fun loadItems(
        pricePer: MutableMap<NEUInternalName, Double>,
        lines: MutableMap<NEUInternalName, Renderable>,
    ) {
        val availableMaterial = readItems()
        for (internalName in NEUItems.allInternalNames) {
            if (config.excludeVanillaItems && internalName.isVanillaItem()) continue

            val recipes = NEUItems.getRecipes(internalName)
            for (recipe in recipes) {
                if (!recipe.isCraftingRecipe()) continue
                val renderable = createItemRenderable(recipe, availableMaterial, pricePer, internalName) ?: continue
                lines[internalName] = renderable
            }
        }
    }

    private fun createItemRenderable(
        recipe: PrimitiveRecipe,
        availableMaterial: Map<NEUInternalName, Long>,
        pricePer: MutableMap<NEUInternalName, Double>,
        internalName: NEUInternalName,
    ): Renderable? {
        val neededItems = ItemUtils.neededItems(recipe)
        // Just a fail save, should not happen normally
        if (neededItems.isEmpty()) return null

        val canCraftAmount = canCraftAmount(neededItems, availableMaterial)
        if (canCraftAmount <= 0) return null

        val amountFormat = canCraftAmount.addSeparators()
        val totalPrice = pricePer(neededItems)
        pricePer[internalName] = totalPrice
        val tooltip = buildList<String> {
            add(internalName.itemName)
            add("")
            add("§7Craft cost: §6${totalPrice.shortFormat()}")
            for ((item, amount) in neededItems) {
                val name = item.itemName
                val price = item.getPrice() * amount
                add(" §8x${amount.addSeparators()} $name §7(§6${price.shortFormat()}§7)")
            }
            add("")
            add("§7You have enough materials")
            val timeName = StringUtils.pluralize(canCraftAmount, "time", "times")
            add("§7to craft this item $amountFormat $timeName!")
            add("")
            add("§eClick to craft!")
        }
        return Renderable.clickAndHover(
            "§8x$amountFormat ${internalName.itemName}",
            tips = tooltip,
            onClick = {
                HypixelCommands.viewRecipe(internalName.asString())
            },
        )
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    private fun pricePer(neededItems: Map<NEUInternalName, Int>): Double = neededItems.map { it.key.getPrice() * it.value }.sum()

    private fun canCraftAmount(
        need: Map<NEUInternalName, Int>,
        available: Map<NEUInternalName, Long>,
    ): Int {
        val canCraftTotal = mutableListOf<Int>()
        for ((name, neededAmount) in need) {
            val inventory = available[name] ?: 0
            val sacks = if (config.includeSacks) name.getAmountInSacks() else 0
            val having = inventory + sacks
            val canCraft = floor(having.toDouble() / neededAmount).toInt()
            canCraftTotal.add(canCraft)
        }
        return canCraftTotal.min()
    }

    private fun readItems(): Map<NEUInternalName, Long> {
        val materials = mutableMapOf<NEUInternalName, Long>()
        for (stack in InventoryUtils.getItemsInOwnInventory()) {
            val item = stack.toPrimitiveStackOrNull() ?: continue
            materials.addOrPut(item.internalName, item.amount.toLong())
        }
        return materials
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!inInventory) return

        config.position.renderRenderables(display, posLabel = "Craft Materials From Bazaar")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
