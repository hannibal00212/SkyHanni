package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NEUInternalName

data class PetData(
    val internalName: NEUInternalName? = null,
    val cleanName: String? = null,
    val rarity: LorenzRarity? = null,
    val petItem: NEUInternalName? = null,
    val level: Int? = null,
    val xp: Double? = null,
    val rawPetName: String? = null,
) {
    val displayName = internalName?.itemName

    override fun equals(other: Any?): Boolean {
        if (other !is PetData) return false
        return this.internalName == other.internalName &&
            this.cleanName == other.cleanName &&
            this.rarity == other.rarity &&
            this.petItem == other.petItem &&
            this.level == other.level &&
            this.rawPetName == other.rawPetName
    }

    override fun hashCode(): Int {
        var result = cleanName.hashCode()
        result = 31 * result + rarity.hashCode()
        result = 31 * result + (petItem?.hashCode() ?: 0)
        result = 31 * result + (level ?: 0)
        result = 31 * result + rawPetName.hashCode()
        return result
    }

    fun isInitialized(): Boolean {
        return internalName != null && cleanName != null && rarity != null && level != null && xp != null && rawPetName != null
    }
}
