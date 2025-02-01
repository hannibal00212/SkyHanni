package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import com.google.gson.annotations.Expose

data class RiftWoodenButtonsJson(
    @Expose val houses: Map<String, List<ButtonSpots>>
)

data class ButtonSpots(
    @Expose val position: SkyHanniVec3d,
    @Expose val buttons: List<SkyHanniVec3d>
)
