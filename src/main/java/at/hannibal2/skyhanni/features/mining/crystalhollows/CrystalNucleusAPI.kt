package at.hannibal2.skyhanni.features.mining.crystalhollows

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.events.mining.CrystalNucleusLootEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.fromItemNameOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CrystalNucleusAPI {

    private val patternGroup = RepoPattern.group("mining.crystalnucleus")

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

    private var inLoot = false
    private var unCheckedBooks: Int = 0
    private val loot = mutableMapOf<NEUInternalName, Int>()

    private val LAPIDARY_I_BOOK_ITEM by lazy { "LAPIDARY;1".asInternalName() }
    private val FORTUNE_IV_BOOK_ITEM by lazy { "FORTUNE;4".asInternalName() }

    @SubscribeEvent
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

    @SubscribeEvent
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

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!IslandType.CRYSTAL_HOLLOWS.isInIsland()) return

        val message = event.message

        if (startPattern.matches(message)) {
            unCheckedBooks = 0
            inLoot = true
            return
        }
        if (!inLoot) return

        if (endPattern.matches(message)) {
            // If there are unchecked books, the loot is not complete, and will be finished in the
            // pickup event handler.
            inLoot = false
            if (unCheckedBooks > 0) return
            CrystalNucleusLootEvent(loot).post()
            loot.clear()
            return
        }

        // All loot rewards start with 4 spaces.
        // To simplify regex statements, this check is done outside the main logic.
        // This also nerfs the "§r§a§lREWARDS" message.
        message.takeIf { it.startsWith("    ") }?.substring(4)?.let { lootMessage ->
            ItemUtils.readItemAmount(lootMessage)?.let { pair ->
                if (pair.first.startsWith("§fEnchanted")) {
                    unCheckedBooks += pair.second
                    return
                }
                val item = fromItemNameOrNull(pair.first) ?: return
                loot.addOrPut(item, pair.second)
            } ?: ErrorManager.logErrorStateWithData(
                "Failed to read item amount",
                "",
                "message" to lootMessage,
            )
        }
    }
}
