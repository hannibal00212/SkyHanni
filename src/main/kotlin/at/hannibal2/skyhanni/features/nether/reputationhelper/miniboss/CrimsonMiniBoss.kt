package at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import java.util.regex.Pattern

class CrimsonMiniBoss(
    val displayName: String,
    val displayItem: NEUInternalName,
    val location: LorenzVec?,
    val pattern: Pattern,
    var doneToday: Boolean = false,
)
