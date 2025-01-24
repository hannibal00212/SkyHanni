package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.DungeonHubRacesJson
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.minecraft.RenderWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.ParkourHelper
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object DungeonsRaceGuide {

    private val config get() = SkyHanniMod.feature.dungeon.dungeonsRaceGuide

    /**
     * REGEX-TEST: §D§LPRECURSOR RUINS RACE §e00:05.443            §b1577/1577✎ Mana
     */
    private val raceActivePattern by RepoPattern.pattern(
        "dungeon.race.active",
        "§.§.(?<race>[\\w ]+) RACE §.[\\d:.]+",
    )

    private val parkourHelpers: MutableMap<String, ParkourHelper> = mutableMapOf()

    private var currentRace: String? = null

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        parkourHelpers.forEach { it.value.reset() }
        currentRace = null
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<DungeonHubRacesJson>("DungeonHubRaces")
        for ((key, map) in data.data) {
            val nothingNoReturn = map["nothing:no_return"]
            parkourHelpers[key] = ParkourHelper(
                nothingNoReturn?.locations.orEmpty(),
                nothingNoReturn?.shortCuts.orEmpty(),
                platformSize = 1.0,
                detectionRange = 7.0,
                depth = false,
            )
        }
        updateConfig()
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.rainbowColor, config.monochromeColor, config.lookAhead) {
            updateConfig()
        }
    }

    @HandleEvent
    fun onActionBarUpdate(event: ActionBarUpdateEvent) {
        if (!isEnabled()) return
        currentRace = null
        raceActivePattern.findMatcher(event.actionBar) {
            currentRace = group("race").replace(" ", "_").lowercase()
        }
        if (currentRace == null) {
            parkourHelpers.forEach { it.value.reset() }
        }
    }

    private fun updateConfig() {
        parkourHelpers.values.forEach {
            it.rainbowColor = config.rainbowColor.get()
            it.monochromeColor = config.monochromeColor.get().toSpecialColor()
            it.lookAhead = config.lookAhead.get() + 1
        }
    }

    @HandleEvent
    fun onRenderWorld(event: RenderWorldEvent) {
        if (!isEnabled()) return
        if (currentRace == null) return

        parkourHelpers[currentRace]?.render(event)
    }

    fun isEnabled() = IslandType.DUNGEON_HUB.isInIsland() && config.enabled
}
