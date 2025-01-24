package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.SackApi.getAmountInSacks
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorOpenEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.PrimitiveIngredient.Companion.toPrimitiveItemStacks
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.init.Items
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenVisitorSupercraft {

    private val isSupercraftEnabled get() = VisitorApi.config.shoppingList.showSuperCraft

    private var hasIngredients = false
    private var lastClick = SimpleTimeMark.farPast()
    private var lastSuperCraftMaterial = ""

    private val superCraftItem by lazy {
        ItemUtils.createItemStack(
            Items.golden_pickaxe,
            "§bSupercraft",
            "§8(From SkyHanni)",
            "",
            "§7You have the items to craft.",
            "§7Click me to open the supercrafter!",
        )
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (hasIngredients) {
            hasIngredients = false
        }
    }

    // needs to run later than onVisitorOpen at GardenVisitorFeatures
    @HandleEvent(priority = HandleEvent.LOW)
    fun onVisitorOpen(event: VisitorOpenEvent) {
        val visitor = event.visitor
        visitor.offer?.offerItem ?: return
        for ((internalName, amount) in visitor.shoppingList) {
            if (isSupercraftEnabled) {
                try {
                    getSupercraftForSacks(internalName, amount)
                } catch (e: NoSuchElementException) {
                    ErrorManager.logErrorWithData(
                        e,
                        "Failed to calculate supercraft recipes for visitor",
                        "internalName" to internalName,
                        "amount" to amount,
                        "visitor" to visitor.visitorName,
                        "visitor.offer?.offerItem" to visitor.offer?.offerItem,
                    )
                }
            }
        }
    }

    private fun getSupercraftForSacks(internalName: NeuInternalName, amount: Int) {
        val ingredients = NeuItems.getRecipes(internalName)
            // TODO describe what this line does
            .firstOrNull { !it.ingredients.first().internalName.contains("PEST") }
            ?.ingredients ?: return
        val requiredIngredients = mutableMapOf<NeuInternalName, Int>()
        for ((key, count) in ingredients.toPrimitiveItemStacks()) {
            requiredIngredients.addOrPut(key, count)
        }
        hasIngredients = true
        for ((key, value) in requiredIngredients) {
            val sackItem = key.getAmountInSacks()
            lastSuperCraftMaterial = internalName.asString()
            if (sackItem < value * amount) {
                hasIngredients = false
                break
            }
        }
    }

    @HandleEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!hasIngredients) return
        if (event.inventory is InventoryPlayer) return

        if (event.slot == 31) {
            event.replace(superCraftItem)
        }
    }

    @HandleEvent(priority = HandleEvent.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!hasIngredients) return

        if (event.slotId != 31) return
        event.cancel()
        if (lastClick.passedSince() > 0.3.seconds) {
            HypixelCommands.viewRecipe(lastSuperCraftMaterial)
            lastClick = SimpleTimeMark.now()
        }
    }
}
