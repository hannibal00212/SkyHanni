package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.UtilsPatterns
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SkyBlockXPAPI {

    private val storage get() = ProfileStorageData.profileSpecific?.skyblockXP

    var xp: Int?
        get() = storage?.xp
        set(value) {
            storage?.let { it.xp = value }
        }

    var level: Int?
        get() = storage?.level
        set(value) {
            storage?.let { it.level = value }
        }

    fun getLevelColor() = level?.let { getLevelColor(it) } ?: LorenzColor.BLACK

    fun getLevelColor(level: Int) = levelColors.entries.firstOrNull { level in it.key }?.value ?: LorenzColor.BLACK

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


    @SubscribeEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.SB_LEVEL)) return

        event.widget.pattern.firstMatcher(event.lines) {
            level = group("level")?.toIntOrNull()
            xp = group("xp")?.toIntOrNull()
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!UtilsPatterns.skyblockMenuGuiPattern.matches(event.inventoryName)) return

        val stack = event.inventoryItems.values.find { itemNamePattern.matches(it.displayName) } ?: return

        var foundLevel = false
        var foundXp = false

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
    }

}
