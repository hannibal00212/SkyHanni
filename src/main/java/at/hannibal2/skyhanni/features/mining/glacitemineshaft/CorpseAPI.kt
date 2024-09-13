package at.hannibal2.skyhanni.features.mining.glacitemineshaft

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.mining.CorpseLootedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CorpseAPI {

    private val patternGroup = RepoPattern.group("mining.mineshaft")
    private val chatPatternGroup = patternGroup.group("chat")

    /**
     * REGEX-TEST:   §r§b§l§r§9§lLAPIS §r§b§lCORPSE LOOT!
     * REGEX-TEST:   §r§b§l§r§7§lTUNGSTEN §r§b§lCORPSE LOOT!
     * REGEX-TEST:   §r§b§l§r§6§lUMBER §r§b§lCORPSE LOOT!
     * REGEX-TEST:   §r§b§l§r§f§lVANGUARD §r§b§lCORPSE LOOT!
     */
    private val startPattern by chatPatternGroup.pattern(
        "start",
        " {2}§r§b§l§r§(?<color>.)§l(?<name>.*) §r§b§lCORPSE LOOT! ?"
    )

    /**
     * REGEX-TEST: §a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
     */
    private val endPattern by chatPatternGroup.pattern("end", "§a§l▬{64}")

    /**
     * REGEX-TEST:     §r§9☠ Fine Onyx Gemstone §r§8x2
     */
    private val itemPattern by chatPatternGroup.pattern("item", " {4}§r(?<item>.+)")

    private var inLoot = false
    private val loot = mutableListOf<Pair<String, Int>>()

    private var corpseType: CorpseType? = null

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!IslandType.MINESHAFT.isInIsland()) return

        val message = event.message

        startPattern.matchMatcher(message) {
            inLoot = true
            val name = group("name")
            corpseType = CorpseType.valueOf(name)
            return
        }

        if (!inLoot) return

        if (endPattern.matches(message)) {
            corpseType?.let {
                CorpseLootedEvent(it, loot.toList()).postAndCatch()
            }
            corpseType = null
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
//             pair = "Paleontologist I" to pair.second
            pair = "§9Ice Cold I" to pair.second
        }
        loot.add(pair)
    }
}
