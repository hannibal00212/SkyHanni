package at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss

import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import java.util.regex.Pattern

class CrimsonMiniBoss(
    val displayName: String,
    val displayItem: NeuInternalName,
    val location: SkyHanniVec3d?,
    val pattern: Pattern,
    var doneToday: Boolean = false,
)
