package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.minecraft.ToolTipEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object WarpMenuUniques {

    /**
     * REGEX-TEST: §bSkyBlock Hub
     * REGEX-TEST: §aThe Barn§7 - §bSpawn
     * REGEX-TEST: §aCrystal Hollows§7 - §bEntrance
     */
    private val islandNamePattern by RepoPattern.pattern(
        "inventory.warpmenu.island.name",
        "§[ab](?<name>[\\w ']+)(?:§7 - §b.*)?",
    )

    private val collectedEggStorage: MutableMap<IslandType, MutableSet<LorenzVec>>?
        get() = ChocolateFactoryApi.profileStorage?.collectedEggLocations

    private val config get() = SkyHanniMod.feature.event.hoppityEggs.warpMenu

    @HandleEvent(onlyOnSkyblock = true)
    fun onToolTip(event: ToolTipEvent) {
        if (!config.enabled) return
        if (!HoppityApi.isHoppityEvent()) return
        if (event.slot.inventory.name != "Fast Travel") return

        val name = islandNamePattern.matchMatcher(event.slot.stack.displayName) {
            group("name")
        } ?: return

        val island = when (name) {
            "SkyBlock Hub" -> IslandType.HUB
            "The Barn" -> IslandType.THE_FARMING_ISLANDS
            else -> IslandType.getByNameOrNull(name) ?: return
        }

        if (HoppityEggLocations.apiEggLocations[island]?.size == null) return
        val maxEggs = 15
        val collectedEggs = collectedEggStorage?.get(island)?.size ?: 0

        if (collectedEggs >= maxEggs && config.hideWhenMaxed) return

        event.toolTip.add(2, "§7Collected Hoppity Eggs: ${if (collectedEggs == maxEggs) "§a" else ""}$collectedEggs/$maxEggs")
    }
}
