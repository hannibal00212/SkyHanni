package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object SpidersDenAPI {

    private val repoGroup = RepoPattern.group("combat.spidersden")

    /**
     * REGEX-TEST: §4Broodmother§7: §6Soon
     */
    val broodmotherPattern by repoGroup.pattern(
        "broodmother",
        "§4Broodmother§7: §[e64](?:Slain|Dormant|Soon|Awakening|Imminent|Alive!)",
    )

    fun inSpidersDen() = IslandType.SPIDER_DEN.isInIsland()

    fun isAtTopOfNest() = inSpidersDen() && broodmotherPattern.anyMatches(ScoreboardData.sidebarLinesFormatted)
}
