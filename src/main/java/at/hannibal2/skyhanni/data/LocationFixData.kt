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

    private var locationFixes = mutableListOf<LocationFix>()

    class LocationFix(val island: IslandType, val area: AxisAlignedBB, val realLocation: String)

    // priority set to low so that IslandType can load their island names from repo earlier
    @SubscribeEvent(priority = EventPriority.LOW)
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<LocationFixJson>("LocationFix")
        locationFixes.clear()

        for (fix in data.locationFixes.values) {
            val island = IslandType.getByName(fix.islandName)
            val area = fix.a.axisAlignedTo(fix.b)
            val realLocation = fix.realLocation

            locationFixes.add(LocationFix(island, area, realLocation))
        }
    }

    fun fixLocation(skyBlockIsland: IslandType) = locationFixes
        .firstOrNull { skyBlockIsland == it.island && it.area.isPlayerInside() }
        ?.realLocation
}
