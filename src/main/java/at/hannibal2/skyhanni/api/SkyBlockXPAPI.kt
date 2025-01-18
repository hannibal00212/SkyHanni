package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
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

    private val levelColors = mapOf(
        0..39 to LorenzColor.GRAY,
        40..79 to LorenzColor.WHITE,
        80..119 to LorenzColor.YELLOW,
        120..159 to LorenzColor.GREEN,
        160..199 to LorenzColor.DARK_GREEN,
        200..239 to LorenzColor.AQUA,
        240..279 to LorenzColor.DARK_AQUA,
        280..319 to LorenzColor.BLUE,
        320..359 to LorenzColor.LIGHT_PURPLE,
        360..399 to LorenzColor.DARK_PURPLE,
        400..439 to LorenzColor.GOLD,
        440..479 to LorenzColor.RED,
        480..Int.MAX_VALUE to LorenzColor.DARK_RED,
    )


    fun getLevelColor() = level?.let { getLevelColor(it) } ?: LorenzColor.BLACK

    fun getLevelColor(level: Int) = levelColors.entries.firstOrNull { level in it.key }?.value ?: LorenzColor.BLACK

    @HandleEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.SB_LEVEL)) return

        TabWidget.SB_LEVEL.matchMatcherFirstLine {
            val level = group("level")?.toIntOrNull()
            val xp = group("xp")?.toIntOrNull()

            if (level != null && xp != null) {
                storage = level * 100 + xp
            }
        }
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!UtilsPatterns.skyblockMenuGuiPattern.matches(event.inventoryName)) return

        val stack = event.inventoryItems.values.find { itemNamePattern.matches(it.displayName) } ?: return

        var foundLevel = false
        var foundXp = false
        var level: Int? = null
        var xp: Int? = null

        loop@ for (line in stack.getLore()) {
            if (foundLevel && foundXp) break@loop

            if (!foundLevel) {
                levelPattern.matchMatcher(line) {
                    level = group("level")?.toIntOrNull()
                    foundLevel = true
                    continue@loop
                }
            }

            if (!foundXp) {
                xpPattern.matchMatcher(line) {
                    xp = group("xp")?.toIntOrNull()
                    foundXp = true
                    continue@loop
                }
            }
        }

        if (level != null && xp != null) {
            storage = level * 100 + xp
        }
    }

}
