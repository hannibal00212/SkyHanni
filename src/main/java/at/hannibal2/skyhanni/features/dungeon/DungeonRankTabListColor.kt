package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.TabListLineRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrEmpty
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName
import at.hannibal2.skyhanni.utils.StringUtils.stripHypixelMessage
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DungeonRankTabListColor {

    private val config get() = SkyHanniMod.feature.dungeon.tabList
    private val patternGroup = RepoPattern.group("dungeon.tablist")

    /**
     * REGEX-TEST: §8[§r§9319§r§8] §r§bEmpa_ §r§7α §r§f(§r§dMage XXXIV§r§f)
     * REGEX-TEST: §8[§r§5393§r§8] §r§c[§r§fYOUTUBE§r§c] Remittal§r§f §r§7Σ§r§7♲ §r§f(§r§dMage XL§r§f)
     */
    @Suppress("MaxLineLength")
    private val pattern by patternGroup.pattern(
        "rank",
        "^(?:§.)*(?<sbLevel>\\[(?:§.)*\\d+(?:§.)*]) (?<rank>(?:§.)*\\[(?:§.)*[^]]+(?:§.)*])? ?(?<playerName>\\S+) (?<symbols>[^(]*)\\((?:§.)*(?<className>\\S+) (?<classLevel>[CLXVI]+)(?:§.)*\\)(?:§.)*$"
    )

    @SubscribeEvent
    fun onTabListText(event: TabListLineRenderEvent) {
        if (!isEnabled()) return

        pattern.matchMatcher(event.text.stripHypixelMessage()) {
            val sbLevel = group("sbLevel")
            val rank = groupOrEmpty("rank")
            val playerName = group("playerName")
            // val symbols = group("symbols")
            val className = group("className")
            val classLevel = group("classLevel")

            val cleanName = playerName.cleanPlayerName(true)
            val color = DungeonAPI.getColor(classLevel.romanToDecimalIfNecessary())

            event.text = "§8$sbLevel $rank$cleanName §f(§d$className $color$classLevel§f)"
        }
    }

    fun isEnabled() = DungeonAPI.inDungeon() && config.coloredClassLevel
}
