package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import com.google.gson.annotations.Expose

data class RiftEffigiesJson(
    @Expose val locations: List<SkyHanniVec3d>,
)
