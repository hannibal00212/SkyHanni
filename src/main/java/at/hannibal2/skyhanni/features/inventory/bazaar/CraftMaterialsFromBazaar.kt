package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.isBazaarItem
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.PrimitiveItemStack
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CraftMaterialsFromBazaar {

    private val config get() = SkyHanniMod.feature.inventory.bazaar

    private val materialSlots = listOf(10, 11, 12, 19, 20, 21, 28, 29, 30)

    private var inRecipeInventory = false
    private var purchasing = false
    private var display = listOf<Renderable>()
    private var neededMaterials = listOf<PrimitiveItemStack>()
    private var multiplier = 1

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        val items = event.inventoryItems
        val correctItem = items[23]?.name == "§aCrafting Table"
        val correctSuperCraftItem = items[32]?.name == "§aSupercraft"

        inRecipeInventory = correctSuperCraftItem && correctItem && !purchasing
        if (!inRecipeInventory) return

        val recipeName = items[25]?.itemName ?: return
        showRecipe(calculateMaterialsNeeded(event.inventoryItemsPrimitive), recipeName)
    }

    private fun showRecipe(
        recipeMaterials: List<PrimitiveItemStack>,
        recipeName: String,
    ) {
        val neededMaterials = mutableListOf<PrimitiveItemStack>()
        display = buildList {
            val totalPrice = calculateTotalPrice(recipeMaterials, 1)
            add(Renderable.string("§7Craft $recipeName §7(§6${totalPrice.shortFormat()}§7)"))
            for (item in recipeMaterials) {
                val material = item.internalName
                val amount = item.amount
                var text = "§8${amount.addSeparators()}x " + material.itemName
                if (material.isBazaarItem()) {
                    neededMaterials.add(item)
                    text += " §6${(material.getPrice() * amount).shortFormat()}"
                }
                add(Renderable.string(text))
            }
            if (neededMaterials.isNotEmpty()) {
                add(
                    Renderable.clickAndHover(
                        "§eGet from bazaar!",
                        listOf("§eClick here to buy the items from bazaar!"),
                        onClick = {
                            getFromBazaar(neededMaterials)
                        }
                    )
                )
            }
        }
    }

    private fun calculateMaterialsNeeded(items: Map<Int, PrimitiveItemStack>): List<PrimitiveItemStack> {
        val recipeMaterials = mutableMapOf<NEUInternalName, Int>()
        for (slot in materialSlots) {
            val item = items[slot] ?: continue
            val internalName = item.internalName
            recipeMaterials.addOrPut(internalName, item.amount)
        }
        return recipeMaterials.map { it.key.makePrimitiveStack(it.value) }
    }

    private fun getFromBazaar(neededMaterials: MutableList<PrimitiveItemStack>) {
        this.neededMaterials = neededMaterials
        this.multiplier = 1
        purchasing = true
        updateBazaarDisplay()
    }

    private fun updateBazaarDisplay() {
        display = buildList {
            add(Renderable.string("§7Buy items from Bazaar:"))
            for ((material, amount) in neededMaterials) {
                val priceMultiplier = amount * multiplier
                val text = "§8${priceMultiplier.addSeparators()}x " + material.itemName +
                    " §6${(material.getPrice() * priceMultiplier).shortFormat(false)}"
                add(
                    Renderable.optionalLink(
                        text,
                        onClick = {
                            BazaarApi.searchForBazaarItem(material, priceMultiplier)
                        }
                    )
                )
            }
            add(
                Renderable.clickAndHover(
                    "§eStop!",
                    listOf("§eClick here to stop this view!"),
                    onClick = {
                        purchasing = false
                        display = emptyList()
                    }
                )
            )
            addMultipliers()
        }
    }

    private fun MutableList<Renderable>.addMultipliers() {
        for (m in listOf(1, 5, 16, 32, 64, 512)) {
            val isThisMultiply = m == multiplier
            val nameColor = if (isThisMultiply) "§a" else "§e"
            val priceColor = if (isThisMultiply) "§6" else "§7"
            val price = priceColor + calculateTotalPrice(neededMaterials, m).shortFormat()
            val text = "${nameColor}Mulitply x$m $price"
            if (!isThisMultiply) {
                add(
                    Renderable.clickAndHover(
                        text,
                        listOf("§eClick here to multiply the items needed times $m!"),
                        onClick = {
                            multiplier = m
                            updateBazaarDisplay()
                        }
                    )
                )
            } else {
                addString(text)
            }
        }
    }

    private fun calculateTotalPrice(neededMaterials: List<PrimitiveItemStack>, multiplier: Int): Double =
        neededMaterials
            .filter { it.internalName.isBazaarItem() }
            .sumOf { it.internalName.getPrice() * it.amount * multiplier }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inRecipeInventory = false
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!inRecipeInventory && !purchasing) return

        config.craftMaterialsFromBazaarPosition.renderRenderables(display, posLabel = "Craft Materials From Bazaar")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.craftMaterialsFromBazaar

}
