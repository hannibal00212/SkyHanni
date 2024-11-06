package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.data.SackAPI.getAmountInSacks
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorOpenEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.PrimitiveIngredient.Companion.toPrimitiveItemStacks
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.entity.player.InventoryPlayer
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenVisitorSupercraft {

    private val isSupercraftEnabled get() = VisitorAPI.config.shoppingList.showSuperCraft

    private var hasIngredients = false
    private var lastClick = SimpleTimeMark.farPast()
    private var lastSuperCraftMaterial = ""

    private val superCraftItem by lazy {
        val neuItem = "GOLD_PICKAXE".asInternalName().getItemStack()
        ItemUtils.createItemStack(
            neuItem.item,
            "§bSuper Craft",
            "§8(From SkyHanni)",
            "",
            "§7You have the items to craft",
            "§7Click me to open the super crafter!",
        )
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (hasIngredients) {
            hasIngredients = false
        }
    }

    // needs to run later than onVisitorOpen at GardenVisitorFeatures
    @SubscribeEvent(priority = EventPriority.LOW)
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

    private fun getSupercraftForSacks(internalName: NEUInternalName, amount: Int) {
        val ingredients = NEUItems.getRecipes(internalName)
            // TODO describe what this line does
            .firstOrNull { !it.ingredients.first().internalName.contains("PEST") }
            ?.ingredients ?: return
        val requiredIngredients = mutableMapOf<NEUInternalName, Int>()
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

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!hasIngredients) return
        if (event.inventory is InventoryPlayer) return

        if (event.slot == 31) {
            event.replace(superCraftItem)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
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
