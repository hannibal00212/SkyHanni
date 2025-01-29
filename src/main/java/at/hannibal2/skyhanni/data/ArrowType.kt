package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.utils.NeuInternalName

data class ArrowType(val arrow: String, val internalName: NeuInternalName) {
    override fun toString(): String {
        return internalName.asString()
    }
}
