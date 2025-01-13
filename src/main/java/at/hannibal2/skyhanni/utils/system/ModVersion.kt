package at.hannibal2.skyhanni.utils.system

data class ModVersion(val stable: Int, val beta: Int, val bugfix: Int) : Comparable<ModVersion> {

    val isBeta get() = beta != 0
    val isBackport get() = beta == 0 && bugfix != 0

    inline val asString get() = toString()

    override fun toString(): String = "$stable.$beta.$bugfix"

    override fun compareTo(other: ModVersion): Int {
        return when {
            stable != other.stable -> stable.compareTo(other.stable)
            beta != other.beta -> beta.compareTo(other.beta)
            else -> bugfix.compareTo(other.bugfix)
        }
    }

    companion object {
        fun fromString(version: String): ModVersion {
            val parts = version.split('.')
            return ModVersion(
                parts.getOrNull(0)?.toIntOrNull() ?: 0,
                parts.getOrNull(1)?.toIntOrNull() ?: 0,
                parts.getOrNull(2)?.toIntOrNull() ?: 0,
            )
        }
    }
}
