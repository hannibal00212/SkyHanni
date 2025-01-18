package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.UtilsPatterns
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object SkyBlockXPAPI {

    private val group = RepoPattern.group("skyblockxpapi.inventory")

    private val itemNamePattern by group.pattern("itemname", "§aSkyBlock Leveling")

    /**
     * REGEX-TEST: §7Your SkyBlock Level: §8[§9287§8]
     */
    private val levelPattern by group.pattern("level", "§7Your SkyBlock Level: §8\\[§.(?<level>\\d+)§8\\]")

    /**
     * REGEX-TEST: §3§l§m      §f§l§m                   §r §b24§3/§b100 §bXP
     */
    private val xpPattern by group.pattern("xp", "[§\\w\\s]+§b(?<xp>\\d+)§3\\/§b100 §bXP")

    val levelXpPair get() = storage?.toLevelXpPair()

    // Stored as 12345, 123 is the level, 45 is the xp
    private var storage
        get() = ProfileStorageData.profileSpecific?.totalSkyBlockXP
        set(value) {
            ProfileStorageData.profileSpecific?.totalSkyBlockXP = value
        }

    private fun Int.toLevelXpPair() = this / 100 to this % 100

    @HandleEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.SB_LEVEL)) return

        TabWidget.SB_LEVEL.matchMatcherFirstLine {
            val level = group("level")?.toIntOrNull()
            val xp = group("xp")?.toIntOrNull()

            updateStorage(level, xp)
        }
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!UtilsPatterns.skyblockMenuGuiPattern.matches(event.inventoryName)) return

        val stack = event.inventoryItems.values.find { itemNamePattern.matches(it.displayName) } ?: return

        var level: Int? = null
        var xp: Int? = null

        loop@ for (line in stack.getLore()) {
            if (level != null && xp != null) break@loop

            if (level == null) {
                levelPattern.matchMatcher(line) {
                    level = group("level")?.toIntOrNull()
                    continue@loop
                }
            }

            if (xp == null) {
                xpPattern.matchMatcher(line) {
                    xp = group("xp")?.toIntOrNull()
                    continue@loop
                }
            }
        }

        updateStorage(level, xp)
    }

    private fun updateStorage(level: Int?, xp: Int?) {
        storage = calculateTotalXp(level ?: return, xp ?: return)
    }

    fun calculateTotalXp(level: Int, xp: Int): Int = level * 100 + xp

}
