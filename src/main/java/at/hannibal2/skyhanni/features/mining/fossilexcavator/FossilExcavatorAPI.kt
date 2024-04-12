package at.hannibal2.skyhanni.features.mining.fossilexcavator

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.mining.FossilExcavationEvent
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object FossilExcavatorAPI {

    private val patternGroup = RepoPattern.group("mining.fossil.excavator")
    private val chatPatternGroup = patternGroup.group("chat")

    /**
     * REGEX-TEST:   §r§6§lEXCAVATION COMPLETE
     */
    private val startPattern by chatPatternGroup.pattern("start", " {2}§r§6§lEXCAVATION COMPLETE ")

    /**
     * REGEX-TEST: §a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
     */
    private val endPattern by chatPatternGroup.pattern("end", "§a§l▬{64}")

    /**
     * REGEX-TEST:     §r§6Tusk Fossil
     */
    private val itemPattern by chatPatternGroup.pattern("item", " {4}§r(?<item>.+)")

    private var inLoot = false
    private var loot = listOf<Pair<String, Int>>()

    var inInventory = false
    var inExcavatorMenu = false

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!IslandType.DWARVEN_MINES.isInIsland()) return
        if (event.inventoryName != "Fossil Excavator") return
        inInventory = true
    }

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!inInventory) return
        val slots = InventoryUtils.getItemsInOpenChest()
        val itemNames = slots.map { it.stack.displayName.removeColor() }
        inExcavatorMenu = itemNames.any { it == "Start Excavator" }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        inInventory = false
        inExcavatorMenu = false
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
        inExcavatorMenu = false
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        val message = event.message

        if (startPattern.matches(message)) {
            inLoot = true
            return
        }

        if (!inLoot) return

        if (endPattern.matches(message)) {
            FossilExcavationEvent(loot).postAndCatch()
            loot = emptyList()
            inLoot = false
            return
        }

        val pair = itemPattern.matchMatcher(message) {
            val itemLine = group("item")
            val newLine = itemLine.replace("§r", "")
            ItemUtils.readItemAmount(newLine) ?: return
        } ?: return
        loot = loot.editCopy {
            add(pair)
        }
    }
}
