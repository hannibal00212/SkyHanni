package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object WarpMenuUniques {

    /**
     * REGEX-TEST: §bSkyBlock Hub
     * REGEX-TEST: §aThe Barn§7 - §bSpawn
     * REGEX-TEST: §aCrystal Hollows§7 - §bEntrance
     */
    private val islandNamePattern by RepoPattern.pattern(
        "inventory.warpmenu.island.name",
        "^§[ab](?<name>[\\w ']+)(?:§7 - §b.*)?\$"
    )

    private val collectedEggStorage: MutableMap<IslandType, MutableSet<LorenzVec>>?
        get() = ChocolateFactoryAPI.profileStorage?.collectedEggLocations

    private val config get() = SkyHanniMod.feature.event.hoppityEggs

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.uniquesWarpMenu) return
        if (!HoppityAPI.isHoppityEvent()) return
        if (event.slot.inventory.name != "Fast Travel") return

        islandNamePattern.matchMatcher(event.slot.stack.displayName) {
            val island = when (val name = group("name")) {
                "SkyBlock Hub" -> IslandType.HUB
                "The Barn" -> IslandType.THE_FARMING_ISLANDS
                else -> IslandType.getByNameOrNull(name) ?: return
            }
            if (island == IslandType.DUNGEON_HUB) return

            val maxEggs = if (HoppityEggLocations.apiEggLocations[island]?.size != null) {
                15
            } else return
            val collectedEggs = collectedEggStorage?.get(island)?.size ?: 0

            if (collectedEggs == maxEggs && config.uniquesWarpMenuHideMax) return

            event.toolTip.add(2, "§7Collected Eggs: ${if (collectedEggs == maxEggs) "§a" else ""}$collectedEggs/$maxEggs")
        }
    }
}
