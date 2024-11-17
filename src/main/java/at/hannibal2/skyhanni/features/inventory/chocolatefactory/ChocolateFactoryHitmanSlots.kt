package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.hoppity.RabbitFoundEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI.hitmanInventoryPattern
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.setLore
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    private var slotToHitmanMap = mutableMapOf<Int, HitmanRabbit>()

    data class HitmanRabbit (
        val rabbitName: String,
        val claimedAt: SimpleTimeMark,
        var expiresAt: SimpleTimeMark? = null,
    )

    /**
     * Given a string in the format of "8h 38m 6s", parse it into a [SimpleTimeMark].
     */
    private fun String.parseCooldownToTimeMark(): SimpleTimeMark {
        val time = this.split(" ").mapNotNull {
            when {
                it.endsWith("h") -> it.dropLast(1).toLongOrNull()?.hours
                it.endsWith("m") -> it.dropLast(1).toLongOrNull()?.minutes
                it.endsWith("s") -> it.dropLast(1).toLongOrNull()?.seconds
                else -> null
            }
        }.reduceOrNull { acc, duration -> acc + duration } ?: Duration.ZERO

        return SimpleTimeMark.now().plus(time)
    }

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
    fun replaceItem(event: ReplaceItemEvent) {
        if (!config.hitmanSlotInfo) return
        if (!hitmanInventoryPattern.matches(event.inventory.name)) return

        val hitmanRabbit = slotToHitmanMap[event.slot] ?: return
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

        val cooldownSlots = event.inventoryItems.filterValues {
            it.hasDisplayName() && slotOnCooldownPattern.matches(it.displayName)
        }

        // Remove the first [claimedSlotCount] slots from the cooldown slots
        var claimedSlotCount = hitmanRabbits.count { it.expiresAt != null }
        var claimedSlotKeys = cooldownSlots.keys.sorted().take(claimedSlotCount).toSet()
        var unclaimedSlotKeys = cooldownSlots.keys - claimedSlotKeys

        // Ensure that unclaimedSlotKeys is the same size as the list of uninitialized hitman rabbits
        // If not we need to pop that discrepancy from the hitmanRabbits list
        val nonProcessedRabbits = hitmanRabbits.filter { it.expiresAt == null }
        while(unclaimedSlotKeys.size < nonProcessedRabbits.size) {
            // Remove the rabbit that expires soonest
            hitmanRabbits.filter { it.expiresAt != null }.minByOrNull { it.expiresAt!! }?.let {
                hitmanRabbits.remove(it)
                claimedSlotCount--
                claimedSlotKeys = cooldownSlots.keys.sorted().take(claimedSlotCount).toSet()
                unclaimedSlotKeys = cooldownSlots.keys - claimedSlotKeys
            } ?: break
        }

        // Assign the expiration time to the uninitialized hitman rabbits
        unclaimedSlotKeys.forEach { slotKey ->
            val slot = cooldownSlots[slotKey] ?: return@forEach
            cooldownPattern.firstMatcher(slot.getLore()) {
                val cooldown = group("cooldown").parseCooldownToTimeMark()
                // Get the hitman rabbit that has the oldest claimedAt time, and is not yet assigned an expiration time
                val hitmanRabbit = hitmanRabbits.filter { it.expiresAt == null }.minByOrNull { it.claimedAt } ?: return@firstMatcher
                hitmanRabbit.expiresAt = cooldown
                slotToHitmanMap[slotKey] = hitmanRabbit
            }
        }
    }
}
