package at.hannibal2.skyhanni.utils.system

data class ModVersion(val major: Int, val minor: Int, val patch: Int) {

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

    val isBeta = patch == 0

    val asString: String
        get() = toString()

    override fun toString(): String {
        return "$major.$minor.$patch"
    }

    operator fun compareTo(other: ModVersion): Int {
        return when {
            major != other.major -> major.compareTo(other.major)
            minor != other.minor -> minor.compareTo(other.minor)
            else -> patch.compareTo(other.patch)
        }
    }
}
