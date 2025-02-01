package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LocationFixJson(
    @Expose val locationFixes: Map<String, LocationFix>,
)

data class LocationFix(
    @Expose val a: SkyHanniVec3d,
    @Expose val b: SkyHanniVec3d,
    @Expose @SerializedName("island_name") val islandName: String,
    @Expose @SerializedName("real_location") val realLocation: String,
)
