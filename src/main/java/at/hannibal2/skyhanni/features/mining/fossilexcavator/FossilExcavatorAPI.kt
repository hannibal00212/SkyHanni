package at.hannibal2.skyhanni.features.mining.fossilexcavator

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.mining.FossilExcavationEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
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

    /**
     * REGEX-TEST: §cYou didn't find anything. Maybe next time!
     */
    private val emptyPattern by chatPatternGroup.pattern("empty", "§cYou didn't find anything. Maybe next time!")

    private var inLoot = false
    private val loot = mutableListOf<Pair<String, Int>>()

    var inInventory = false
    var inExcavatorMenu = false

    val scrapItem = "SUSPICIOUS_SCRAP".toInternalName()

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
        if (!IslandType.DWARVEN_MINES.isInIsland()) return

        val message = event.message

        if (emptyPattern.matches(message)) {
            FossilExcavationEvent(emptyList()).postAndCatch()
        }


        if (startPattern.matches(message)) {
            inLoot = true
            return
        }

        if (!inLoot) return

        if (endPattern.matches(message)) {
            FossilExcavationEvent(loot.toList()).postAndCatch()
            loot.clear()
            inLoot = false
            return
        }
        var pair = itemPattern.matchMatcher(message) {
            /**
             * TODO fix the bug that readItemAmount produces two different outputs:
             * §r§fEnchanted Book -> §fEnchanted
             * §fEnchanted Book §r§8x -> §fEnchanted Book
             *
             * also maybe this is no bug, as enchanted book is no real item?
             */
            ItemUtils.readItemAmount(group("item"))
        } ?: return
        // Workaround: If it is an enchanted book, we assume it is a paleontologist I book
        if (pair.first.let { it == "§fEnchanted" || it == "§fEnchanted Book" }) {
            pair = "§9Paleontologist I" to pair.second
        }
        loot.add(pair)
    }
}
