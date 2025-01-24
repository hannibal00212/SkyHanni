package at.hannibal2.skyhanni.utils

class IdentityCharacteristics<T>(val value: T) {

    override fun equals(other: Any?): Boolean {
        if (other !is IdentityCharacteristics<*>) return false
        return this.value === other.value
    }

    override fun hashCode(): Int {
        return System.identityHashCode(value)
    }
}
