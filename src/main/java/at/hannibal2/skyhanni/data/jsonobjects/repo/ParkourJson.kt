package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import com.google.gson.annotations.Expose

data class ParkourJson(
    @Expose val locations: List<SkyHanniVec3d>,
    @Expose val shortCuts: List<ParkourShortCut> = listOf(),
)

data class ParkourShortCut(
    @Expose val from: Int,
    @Expose val to: Int,
)
