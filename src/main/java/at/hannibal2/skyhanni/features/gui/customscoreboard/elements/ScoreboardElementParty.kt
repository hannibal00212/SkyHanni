package at.hannibal2.skyhanni.features.gui.customscoreboard.elements

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.MiningApi
import at.hannibal2.skyhanni.data.PartyApi
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.informationFilteringConfig
import at.hannibal2.skyhanni.features.gui.customscoreboard.CustomScoreboard.partyConfig
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland

// internal
// add party update event
object ScoreboardElementParty : ScoreboardElement() {
    // TODO cache until next party update event
    override fun getDisplay() = buildList {
        if (PartyApi.partyMembers.isEmpty() && informationFilteringConfig.hideEmptyLines) return@buildList

        add(if (PartyApi.partyMembers.isEmpty()) "§9§lParty" else "§9§lParty (${PartyApi.partyMembers.size})")

        if (partyConfig.showPartyLeader && PartyApi.partyLeader != null) {
            add(" §7- §f${PartyApi.partyLeader} §e♚")
        }

        if (partyConfig.showPartyLeader) {
            PartyApi.partyMembers.filter { it != PartyApi.partyLeader }
        } else {
            PartyApi.partyMembers
        }.take(partyConfig.maxPartyList.get()).forEach { add(" §7- §f$it") }
    }

    override fun showWhen() =
        when {
            DungeonApi.inDungeon() -> false // Hidden because the scoreboard lines already exist
            partyConfig.showPartyEverywhere -> true
            else -> inAnyIsland(IslandType.DUNGEON_HUB, IslandType.KUUDRA_ARENA, IslandType.CRIMSON_ISLE) || MiningApi.inColdIsland()
        }

    override val configLine = "§9§lParty (4):\n §7- §fhannibal2\n §7- §fMoulberry\n §7- §fEmpa_\n §7- §fSkirtwearer"

    override fun showIsland() = !DungeonApi.inDungeon()
}

// click (title): run /party list
// click (members): run /pv <name> or  /party kick <name> (maybe option?)
