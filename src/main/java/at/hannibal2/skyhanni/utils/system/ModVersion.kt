package at.hannibal2.skyhanni.utils.system

data class ModVersion(val major: Int, val minor: Int, val beta: Int) : Comparable<ModVersion> {

    val isBeta get() = beta != 0

    inline val asString get() = toString()

    override fun toString(): String = "$major.$minor.$beta"

    override fun compareTo(other: ModVersion): Int {
        return when {
            major != other.major -> major.compareTo(other.major)
            minor != other.minor -> minor.compareTo(other.minor)
            else -> beta.compareTo(other.beta)
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
