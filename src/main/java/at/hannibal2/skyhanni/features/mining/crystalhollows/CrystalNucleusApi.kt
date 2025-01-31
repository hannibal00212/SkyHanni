package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.mining.nucleus.CrystalNucleusTrackerConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.mining.CrystalNucleusLootEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.fromItemNameOrNull
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object CrystalNucleusApi {

    private val patternGroup = RepoPattern.group("mining.crystalnucleus")
    private val config get() = SkyHanniMod.feature.mining.crystalNucleusTracker

    /**
     * REGEX-TEST:   §r§5§lCRYSTAL NUCLEUS LOOT BUNDLE
     */
    private val startPattern by patternGroup.pattern(
        "loot.start",
        " \\s*§r§5§lCRYSTAL NUCLEUS LOOT BUNDLE.*",
    )

    /**
     * REGEX-TEST: §3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
     */
    private val endPattern by patternGroup.pattern(
        "loot.end",
        "§3§l▬{64}",
    )

    private var inLootLoop = false
    private var unCheckedBooks: Int = 0
    private val loot = mutableMapOf<NeuInternalName, Int>()

    private val LAPIDARY_I_BOOK_ITEM = "LAPIDARY;1".toInternalName()
    private val FORTUNE_IV_BOOK_ITEM = "FORTUNE;4".toInternalName()
    val EPIC_BAL_ITEM = "BAL;3".toInternalName()
    val LEGENDARY_BAL_ITEM = "BAL;4".toInternalName()
    private val PRECURSOR_APPARATUS_ITEM = "PRECURSOR_APPARATUS".toInternalName()
    val JUNGLE_KEY_ITEM = "JUNGLE_KEY".toInternalName()
    private val ROBOT_PARTS_ITEMS = listOf(
        "CONTROL_SWITCH",
        "ELECTRON_TRANSMITTER",
        "FTX_3070",
        "ROBOTRON_REFLECTOR",
        "SUPERLITE_MOTOR",
        "SYNTHETIC_HEART",
    ).map { it.toInternalName() }

    @HandleEvent
    fun onOwnInventoryItemUpdate(event: OwnInventoryItemUpdateEvent) {
        if (unCheckedBooks == 0) return
        if (event.itemStack.displayName != "§fEnchanted Book") return
        when (event.itemStack.getEnchantments()?.keys?.firstOrNull() ?: return) {
            "lapidary" -> loot.addOrPut(LAPIDARY_I_BOOK_ITEM, 1)
            "fortune" -> loot.addOrPut(FORTUNE_IV_BOOK_ITEM, 1)
        }
        unCheckedBooks--
        if (unCheckedBooks == 0) {
            CrystalNucleusLootEvent(loot).post()
            loot.clear()
        }
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (unCheckedBooks == 0 ||
            event.oldIsland != IslandType.CRYSTAL_HOLLOWS ||
            event.newIsland == IslandType.CRYSTAL_HOLLOWS
        ) return
        unCheckedBooks = 0
        if (loot.isNotEmpty()) {
            CrystalNucleusLootEvent(loot).post()
            loot.clear()
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.CRYSTAL_HOLLOWS)
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message

        if (startPattern.matches(message)) {
            unCheckedBooks = 0
            inLootLoop = true
            return
        }
        if (!inLootLoop) return

        // Add the loot to the map.
        event.getLoot()?.let { (item, amount) ->
            loot.addOrPut(item, amount)
        }

        // Close the loot loop if the end pattern is matched.
        if (endPattern.matches(message)) {
            inLootLoop = false
            // If there are unchecked books, the loot is not complete, and will be finished in the
            // pickup event handler.
            if (unCheckedBooks > 0) return
            CrystalNucleusLootEvent(loot).post()
            loot.clear()
        }
    }

    private fun SkyHanniChatEvent.getLoot(): Pair<NeuInternalName, Int>? {
        // All loot rewards start with 4 spaces.
        // To simplify regex statements, this check is done outside the main logic.
        // This also nerfs the "§r§a§lREWARDS" message.
        if (!message.startsWith("    ")) return null
        val lootMessage = message.substring(4)

        // Read the item and amount from the message.
        val (itemName, amount) = ItemUtils.readItemAmount(lootMessage) ?: return null

        // Ignore Mithril and Gemstone Powder
        if (itemName.contains(" Powder")) return null
        // Books are not directly added to the loot map, but are checked for later.
        if (itemName.startsWith("§fEnchanted")) {
            unCheckedBooks += amount
            return null
        }
        val item = fromItemNameOrNull(itemName) ?: return null
        return Pair(item, amount)
    }

    fun usesApparatus() =
        config.professorUsage.get() == CrystalNucleusTrackerConfig.ProfessorUsageType.PRECURSOR_APPARATUS

    fun getPrecursorRunPrice() =
        if (usesApparatus()) PRECURSOR_APPARATUS_ITEM.getPrice()
        else ROBOT_PARTS_ITEMS.sumOf {
            it.getPrice()
        }
}
