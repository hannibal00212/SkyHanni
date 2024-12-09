package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.hitmanInventoryPattern
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryStrayTracker.formLoreToSingleLine
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils.isTopInventory
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.setLore
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ChocolateFactoryHitmanSlots {

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §cEgg Slot
     */
    private val slotOnCooldownPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hitman.slotoncooldown",
        "§cEgg Slot"
    )

    /**
     * REGEX-TEST: §7Hitman can store more eggs you miss! §7Cost §620,000,000 Coins §eClick to purchase!
     */
    private val slotCostPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hitman.slotcost",
        ".*§7Cost §6(?<cost>[\\d,]+) Coins.*"
    )

    // </editor-fold>

    private val config get() = ChocolateFactoryAPI.config
    private val hitmanRabbits = mutableListOf<HitmanRabbit>()

    private var cooldownSlotIndices = emptySet<Int>()
    private var slotPricesPaid: List<Long> = emptyList()
    private var slotPricesLeft: List<Long> = emptyList()
    private var inInventory = false

    data class HitmanRabbit(
        val rabbitName: String,
        val claimedAt: SimpleTimeMark,
        var expiresAt: SimpleTimeMark? = null,
        var claimedBySlot: Boolean = false
    )

    @HandleEvent
    fun onRabbitFound(event: RabbitFoundEvent) {
        if (event.eggType != HoppityEggType.HITMAN) return
        hitmanRabbits.add(HitmanRabbit(event.rabbitName, SimpleTimeMark.now()))
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        hitmanRabbits.removeIf { it.expiresAt?.isInPast() == true }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        // Free all slots when the inventory is closed
        hitmanRabbits.forEach { it.claimedBySlot = false }
        inInventory = false
    }

    @HandleEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!config.hitmanSlotInfo) return
        if (!inInventory) return
        if (!cooldownSlotIndices.contains(event.slot)) return
        if (!event.inventory.isTopInventory()) return

        val hitmanRabbit = hitmanRabbits.sortedBy { it.claimedAt }.firstOrNull { !it.claimedBySlot }
            ?: return
        hitmanRabbit.claimedBySlot = true
        val originalItemStack = event.originalItem
        val lore = originalItemStack.getLore().toMutableList()
        /**
         * Lore will be formatted as follows:
         *  '§7§7Once you miss your next egg, your'
         *  '§7Rabbit Hitman will snipe it for you.'
         *  ''
         *  '§cOn cooldown: 7h 47m'
         *
         *  We want to add "Rabbit: ..." above the cooldown line.
         */
        lore.add(lore.size - 1, "§7Last Rabbit: ${hitmanRabbit.rabbitName}")

        // Because the cooldown is constantly changing, we can't cache the replacement itemstack
        val newItemStack = originalItemStack.copy()
        newItemStack.setLore(lore)
        event.replace(newItemStack)
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inInventory = hitmanInventoryPattern.matches(event.inventoryName)
        if (!inInventory) return
        handleInventoryHitmanSlotRename(event)
        handleSlotStorageUpdate(event)
    }

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!config.hitmanCosts || slotPricesLeft.isEmpty()) return
        if (!inInventory) return
        config.hitmanCostsPosition.renderRenderable(
            getSlotPriceRenderable(),
            posLabel = "Hitman Slot Costs"
        )
    }

    private fun handleInventoryHitmanSlotRename(event: InventoryOpenEvent) {
        if (!config.hitmanSlotInfo) return
        if (hitmanRabbits.isEmpty()) return

        cooldownSlotIndices = event.inventoryItems.filterValues {
            it.hasDisplayName() && slotOnCooldownPattern.matches(it.displayName)
        }.keys
    }

    private fun handleSlotStorageUpdate(event: InventoryOpenEvent) {
        if (!config.hitmanCosts) return
        val leftToPurchase = event.inventoryItems.filterNotBorderSlots().count { (_, item) ->
            item.hasDisplayName() && item.getLore().isNotEmpty() &&
                slotCostPattern.matches(formLoreToSingleLine(item.getLore()))
        }
        val ownedSlots = ChocolateFactoryAPI.hitmanCosts.size - leftToPurchase

        slotPricesPaid = ChocolateFactoryAPI.hitmanCosts.take(ownedSlots)
        slotPricesLeft = ChocolateFactoryAPI.hitmanCosts.drop(ownedSlots)
    }

    private fun Map<Int, ItemStack>.filterNotBorderSlots() = filterKeys {
        it !in 0..8 && it !in 45..53 && // Horizontal borders
            it % 9 != 0 && (it + 1) % 9 != 0 // Vertical borders
    }

    private fun getSlotPriceRenderable(): Renderable = Renderable.verticalContainer(
        buildList {
            add(Renderable.string("§eHitman Slot Progress"))

            if (slotPricesPaid.isNotEmpty()) {
                add(
                    Renderable.hoverTips(
                        "§aPurchased Slots§7: §a${slotPricesPaid.size}",
                        listOf("§7Total Paid: §6${slotPricesPaid.sum().addSeparators()} Coins")
                    )
                )
            }

            val remainingSlotsText = buildList {
                add("§7Total Remaining: §6${slotPricesLeft.sum().addSeparators()} Coins")
                slotPricesLeft.take(5).forEachIndexed { index, price ->
                    add("§7Slot ${slotPricesPaid.size + index + 1}: §6${price.addSeparators()} Coins")
                }
                if (slotPricesLeft.size > 5) {
                    add("§8... and ${slotPricesLeft.size - 5} more")
                }
            }

            add(
                Renderable.hoverTips(
                    "§cRemaining Slots§7: §c${slotPricesLeft.size}",
                    remainingSlotsText
                )
            )
        }
    )
}
