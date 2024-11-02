package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.jsonobjects.repo.LocationFixJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object LocationFixData {

    private val locationFixes = mutableMapOf<IslandType, List<LocationFix>>()

    private data class LocationFix(val area: AxisAlignedBB, val realLocation: String)

    // priority set to low so that IslandType can load their island names from repo earlier
    @SubscribeEvent(priority = EventPriority.LOW)
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<LocationFixJson>("LocationFix")
        locationFixes.clear()

        for (fix in data.locationFixes.values) {
            val island = IslandType.getByName(fix.islandName)
            val area = fix.a.axisAlignedTo(fix.b)
            val realLocation = fix.realLocation

            val list = locationFixes[island]

            val locationFix = LocationFix(area, realLocation)

            if (list == null) locationFixes[island] = listOf(locationFix)
            else locationFixes[island] = list + locationFix
        }
    }

    fun fixLocation(skyBlockIsland: IslandType): String? =
        locationFixes[skyBlockIsland]
            ?.find { it.area.isPlayerInside() }
            ?.realLocation

}
