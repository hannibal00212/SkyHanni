package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.CurrentPetAPI
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.PetUtils.rarityByColorGroup
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getExtraAttributes
import at.hannibal2.skyhanni.utils.StringUtils.convertToUnformatted
import com.google.gson.Gson
import net.minecraft.item.ItemStack

// <editor-fold desc="Patterns">
/**
 * REGEX-TEST: §e⭐ §7[Lvl 100] §6Ender Dragon
 * REGEX-TEST: §e⭐ §7[Lvl 100] §dBlack Cat§d ✦
 * REGEX-TEST: §7[Lvl 100] §6Mole
 */
private val petNameMenuPattern by CurrentPetAPI.patternGroup.pattern(
    "menu.pet.name",
    "^(?:§e(?<favorite>⭐) )?(?:§.)*\\[Lvl (?<level>\\d+)] §(?<rarity>.)(?<name>[\\w ]+)(?<skin>§. ✦)?\$",
)

/**
 * REGEX-TEST: §7§cClick to despawn!
 */
private val petDespawnMenuPattern by CurrentPetAPI.patternGroup.pattern(
    "menu.pet.despawn",
    "§7§cClick to despawn!",
)
// </editor-fold>

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

    companion object {
        // <editor-fold desc="Pet Data Extractors (General)">
        fun parsePetData(
            lines: List<String>,
            itemHandler: (String) -> NEUInternalName?,
            xpHandler: (String) -> Double?,
            petHandler: (String) -> PetData?
        ): Pair<PetData, Double>? {
            return parsePetDataLists(
                lines,
                itemHandlerList = { it.firstNotNullOfOrNull(itemHandler) },
                xpHandlerList = { it.firstNotNullOfOrNull(xpHandler) },
                petHandlerList = { it.firstNotNullOfOrNull(petHandler) }
            )
        }

        fun parsePetDataLists(
            lines: List<String>,
            itemHandlerList: (List<String>) -> NEUInternalName?,
            xpHandlerList: (List<String>) -> Double?,
            petHandlerList: (List<String>) -> PetData?
        ): Pair<PetData, Double>? {
            val petItem = itemHandlerList(lines) ?: return null
            val overflowXP = xpHandlerList(lines) ?: 0.0

            val data = petHandlerList(lines) ?: return null
            val petData = PetData(
                internalName = data.internalName,
                cleanName = data.cleanName,
                rarity = data.rarity,
                petItem = petItem,
                level = data.level,
                xp = data.xp,
                rawPetName = data.rawPetName,
            )

            return petData to overflowXP
        }

        private fun parsePetNBT(item: ItemStack): PetData {
            val petInfo = Gson().fromJson(item.getExtraAttributes()?.getString("petInfo"), PetNBT::class.java)

            return PetData(
                internalName = NEUInternalName.NONE,
                cleanName = "",
                level = 0,
                rarity = LorenzRarity.getByName(petInfo.tier) ?: ErrorManager.skyHanniError(
                    "Couldn't parse pet rarity.",
                    Pair("petNBT", petInfo),
                    Pair("rarity", petInfo.tier),
                ),
                petItem = petInfo.heldItem?.asInternalName(),
                xp = petInfo.exp,
                rawPetName = "",
            )
        }

        fun parsePetName(displayName: String): PetData? {
            petNameMenuPattern.matchMatcher(displayName) {
                val name = group("name").orEmpty()
                val rarity = rarityByColorGroup(group("rarity"))
                val level = group("level").toInt()
                val skin = group("skin").orEmpty()

                return PetData(
                    internalName = petNameToInternalName(name, rarity),
                    cleanName = name,
                    rarity = rarity,
                    petItem = null,
                    level = level,
                    xp = 0.0,
                    rawPetName = skin,
                )
            }
            return null
        }

        fun petNameToInternalName(name: String, rarity: LorenzRarity): NEUInternalName =
            "${name.convertToUnformatted()}${rarity.id}".toInternalName()

        fun parsePetAsItem(item: ItemStack): PetData? {
            val lore = item.getLore()
            if (petDespawnMenuPattern.anyMatches(lore)) return null
            return getPetDataFromItem(item)
        }

        @Suppress("DestructuringDeclarationWithTooManyEntries")
        private fun getPetDataFromItem(item: ItemStack): PetData? {
            val (_, _, rarity, petItem, _, petXP, _) = parsePetNBT(item)
            val (internalName, name, _, _, level, _, skin) = parsePetName(item.displayName) ?: return null

            return PetData(
                internalName,
                name,
                rarity,
                petItem,
                level,
                petXP,
                "§r§7[Lvl $level] §r${rarity?.chatColorCode}$name${if (skin != "") "§r$skin" else ""}",
            )
        }
        // </editor-fold>
    }
}

data class PetNBT(
    val type: String,
    val active: Boolean,
    val exp: Double,
    val tier: String,
    val hideInfo: Boolean,
    val heldItem: String?,
    val candyUsed: Int,
    val skin: String?,
    val uuid: String,
    val uniqueId: String,
    val hideRightClick: Boolean,
    val noMove: Boolean,
)
