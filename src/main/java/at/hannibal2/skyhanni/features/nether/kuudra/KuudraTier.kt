package at.hannibal2.skyhanni.features.nether.kuudra

import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.SkyHanniVec3d

class KuudraTier(
    val name: String,
    val displayItem: NeuInternalName,
    val location: SkyHanniVec3d?,
    val tierNumber: Int,
    var doneToday: Boolean = false,
) {
    fun getDisplayName() = "Tier $tierNumber ($name)"
}
