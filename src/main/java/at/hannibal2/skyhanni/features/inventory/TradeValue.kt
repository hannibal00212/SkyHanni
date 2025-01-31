package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.features.inventory.ChestValue.addToList
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValueCalculator
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryDetector
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderDisplayHelper
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.item.ItemStack

@SkyHanniModule
object TradeValue {
    val config get() = SkyHanniMod.feature.inventory.trade
    // other person's trade slots
    val list = (5..8).flatMap { x ->
        (0..3).map { y -> x + 9 * y }
    }.toSet()

    private var prevTotal = 0.0
    private var display = emptyList<Renderable>()
    //detects trade menu thx NEU
    val inventory = InventoryDetector { name -> name.startsWith("You     ") }
     init {
        RenderDisplayHelper(
            inventory,
            condition = { isEnabled() },
        ) {
            config.position.renderRenderables(display, posLabel = "Trade Value")
        }
    }

    private fun isEnabled(): Boolean {
    return LorenzUtils.inSkyBlock && config.enabled
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!inventory.isInside()) {
            prevTotal = 0.0
            update(emptyMap())
            return
        }
        var total = 0.0
        val map = mutableMapOf<Int,ItemStack>()
        // Gets total value of trade
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            if (slot.slotIndex in list) {
                map[slot.slotIndex] = slot.stack
                val stack = slot.stack
                total += (EstimatedItemValueCalculator.calculate(stack, mutableListOf()).first * stack.stackSize)
            }
        }
        println("total: ${total.shortFormat()}")
        if (total != prevTotal) {
            prevTotal = total
            val items = ChestValue.createItems(map)

            update(items)
        }
    }
    //display trade value breakdown
    private fun update(items: Map<String, ChestValue.Item>) {
        display = buildList {
            addToList(items.values, "§eTrade Value")
        }
    }

}
