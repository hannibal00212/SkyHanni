package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.utils.NEUInternalName

data class ArrowType(val arrow: String, val internalName: NEUInternalName) {
    override fun toString(): String {
        return internalName.asString()
    }
}
