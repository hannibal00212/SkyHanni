package at.hannibal2.skyhanni.data.model

import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import net.minecraft.client.Minecraft
import java.util.EnumMap

enum class SkyblockStat(val icon: String) {
    DAMAGE("§c❁"),
    HEALTH("§c❤"),
    DEFENSE("§a❈"),
    STRENGTH("§c❁"),
    INTELLIGENCE("§b✎"),

    CRIT_DAMAGE("§9☠"),
    CRIT_CHANCE("§9☣"),
    FEROCITY("§c⫽"),
    BONUS_ATTACK_SPEED("§e⚔"),
    ABILITY_DAMAGE("§c๑"),
    HEALTH_REGEN("§c❣"),
    VITALITY("§4♨"),
    MENDING("§a☄"),
    TRUE_DEFENCE("§7❂"),
    SWING_RANGE("§eⓈ"),
    SPEED("§f✦"),
    SEA_CREATURE_CHANCE("§3α"),
    MAGIC_FIND("§b✯"),
    PET_LUCK("§d♣"),
    FISHING_SPEED("§b☂"),
    BONUS_PEST_CHANCE("§2ൠ"),
    COMBAT_WISDOM("§3☯"),
    MINING_WISDOM("§3☯"),
    FARMING_WISDOM("§3☯"),
    FORAGING_WISDOM("§3☯"),
    FISHING_WISDOM("§3☯"),
    ENCHANTING_WISDOM("§3☯"),
    ALCHEMY_WISDOM("§3☯"),
    CARPENTRY_WISDOM("§3☯"),
    RUNECRAFTING_WISDOM("§3☯"),
    SOCIAL_WISDOM("§3☯"),
    TAMING_WISDOM("§3☯"),
    MINING_SPEED("§6⸕"),
    BREAKING_POWER("§2Ⓟ"),
    PRISTINE("§5✧"),
    FORAGING_FORTUNE("§☘"),
    FARMING_FORTUNE("§6☘"),

    MINING_SPREAD("§e▚"),
    MINING_FORTUNE("§6☘"),
    ORE_FORTUNE("§6☘"),
    DWARVEN_METAL_FORTUNE("§6☘"),
    BLOCK_FORTUNE("§6☘"),
    GEMSTONE_FORTUNE("§6☘"),

    FEAR("§a☠"),
    HEAT_RESISTANCE("§c♨"),

    UNKNOWN("§c?")
    ;

    val capitalizedName = name.lowercase().allLettersFirstUppercase()

    val iconWithName = "$icon $capitalizedName"

    fun asString(value: Int) = (if (value > 0) "+" else "") + value.toString() + " " + this.icon

    companion object {
        val fontSizeOfLargestIcon by lazy {
            entries.maxOf { Minecraft.getMinecraft().fontRendererObj.getStringWidth(it.icon) } + 1
        }

        fun getValueOrNull(string: String): SkyblockStat? = entries.firstOrNull { it.name == string }

        fun getValue(string: String): SkyblockStat = getValueOrNull(string) ?: UNKNOWN
    }
}

class SkyblockStatList : EnumMap<SkyblockStat, Double>(SkyblockStat::class.java), Map<SkyblockStat, Double> {
    operator fun minus(other: SkyblockStatList): SkyblockStatList {
        return SkyblockStatList().apply {
            val keys = this.keys + other.keys
            for (key in keys) {
                this[key] = (this@SkyblockStatList[key] ?: 0.0) - (other[key] ?: 0.0)
            }
        }
    }

    companion object {
        fun mapOf(vararg list: Pair<SkyblockStat, Double>) = SkyblockStatList().apply {
            for ((key, value) in list) {
                this[key] = value
            }
        }
    }
}

