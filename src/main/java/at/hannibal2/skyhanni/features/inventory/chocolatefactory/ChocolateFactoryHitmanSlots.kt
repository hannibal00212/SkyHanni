package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.hitmanInventoryPattern
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.setLore
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ChocolateFactoryHitmanSlots {

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §cOn cooldown: 8h 38m 6s
     * REGEX-TEST: §cOn cooldown: 19m 57s
     * REGEX-TEST: §cOn cooldown: 3s
     */
    private val cooldownPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hitman.cooldown",
        "§cOn cooldown: (?<cooldown>[\\d hms]*)"
    )

    /**
     * REGEX-TEST: §cEgg Slot
     */
    private val slotOnCooldownPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "hitman.slotoncooldown",
        "§cEgg Slot"
    )
    // </editor-fold>

    private val config get() = ChocolateFactoryAPI.config
    private val hitmanRabbits = mutableListOf<HitmanRabbit>()

    private var cooldownSlotIndices = emptySet<Int>()

    data class HitmanRabbit (
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
    }

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!config.hitmanSlotInfo) return
        if (!hitmanInventoryPattern.matches(event.inventory.name)) return
        if (!cooldownSlotIndices.contains(event.slot)) return

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
        if (!config.hitmanSlotInfo) return
        if (!hitmanInventoryPattern.matches(event.inventoryName)) return
        if (hitmanRabbits.isEmpty()) return

        cooldownSlotIndices = event.inventoryItems.filterValues {
            it.hasDisplayName() && slotOnCooldownPattern.matches(it.displayName)
        }.keys
    }
}
