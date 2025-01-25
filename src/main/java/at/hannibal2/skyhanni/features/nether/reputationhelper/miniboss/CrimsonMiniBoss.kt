package at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuInternalName
import java.util.regex.Pattern

class CrimsonMiniBoss(
    val displayName: String,
    val displayItem: NeuInternalName,
    val location: LorenzVec?,
    val pattern: Pattern,
    var doneToday: Boolean = false,
)
