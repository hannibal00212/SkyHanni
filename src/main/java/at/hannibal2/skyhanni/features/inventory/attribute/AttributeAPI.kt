package at.hannibal2.skyhanni.features.inventory.attribute

import at.hannibal2.skyhanni.data.jsonobjects.repo.AttributeGoodRollsJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeAPI.AttributeType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.equalsIgnoreOrder
import at.hannibal2.skyhanni.utils.CollectionUtils.toPair
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getAttributes
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

enum class RollType {
    GOOD_ROLL,
    PARTIAL_ROLL,
    BAD_ROLL,
}

data class Attribute(val type: AttributeType, val level: Int)

@SkyHanniModule
object AttributeAPI {

    private var goodRolls = listOf<GoodRollItem>()

    enum class AttributeType(val displayName: String, val internalName: String, val shortName: String) {
        ARACHNO("Arachno", "arachno", "AR"),
        ATTACK_SPEED("Attack Speed", "attack_speed", "AS"),
        BLAZING("Blazing", "blazing", "BL"),
        COMBO("Combo", "combo", "CO"),
        ELITE("Elite", "elite", "EL"),
        ENDER("Ender", "ender", "EN"),
        IGNITION("Ignition", "ignition", "IG"),
        LIFE_RECOVERY("Life Recovery", "life_recovery", "LR"),
        MANA_STEAL("Mana Steal", "mana_steal", "MS"),
        MIDAS_TOUCH("Midas Touch", "midas_touch", "MT"),
        UNDEAD("Undead", "undead", "UN"),
        WARRIOR("Warrior", "warrior", "WA"),
        DEADEYE("Deadeye", "deadeye", "DE"),
        ARACHNO_RESISTANCE("Arachno Resistance", "arachno_resistance", "AR"),
        BLAZING_RESISTANCE("Blazing Resistance", "blazing_resistance", "BR"),
        BREEZE("Breeze", "breeze", "BZ"),
        DOMINANCE("Dominance", "dominance", "DO"),
        ENDER_RESISTANCE("Ender Resistance", "ender_resistance", "ER"),
        EXPERIENCE("Experience", "experience", "EX"),
        FORTITUDE("Fortitude", "fortitude", "FO"),
        LIFE_REGENERATION("Life Regeneration", "life_regeneration", "LR"),
        LIFELINE("Lifeline", "lifeline", "LL"),
        MAGIC_FIND("Magic Find", "magic_find", "MF"),
        MANA_POOL("Mana Pool", "mana_pool", "MP"),
        MANA_REGENERATION("Mana Regeneration", "mana_regeneration", "MR"),
        VITALITY("Vitality", "mending", "VI"),
        SPEED("Speed", "speed", "SP"),
        UNDEAD_RESISTANCE("Undead Resistance", "undead_resistance", "UR"),
        VETERAN("Veteran", "veteran", "VE"),
        BLAZING_FORTUNE("Blazing Fortune", "blazing_fortune", "BF"),
        FISHING_EXPERIENCE("Fishing Experience", "fishing_experience", "FE"),
        INFECTION("Infection", "infection", "IN"),
        DOUBLE_HOOK("Double Hook", "double_hook", "DH"),
        FISHERMAN("Fisherman", "fisherman", "FH"),
        FISHING_SPEED("Fishing Speed", "fishing_speed", "FS"),
        HUNTER("Hunter", "hunter", "HU"),
        TROPHY_HUNTER("Trophy Hunter", "trophy_hunter", "TH"),
        UNKNOWN("Unknown", "unknown", "??"),
        ;

        override fun toString() = "§b$displayName"

        companion object {

            fun getByInternalNameOrNull(internalName: String) = entries.find { it.internalName.equals(internalName, true) }

            fun getByInternalName(internalName: String) = getByInternalNameOrNull(internalName) ?: UNKNOWN
        }
    }

    private data class GoodRollItem(val regex: Pattern, val attributes: List<Pair<AttributeType, AttributeType>>)

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<AttributeGoodRollsJson>("AttributeGoodRolls")
        goodRolls = data.goodRolls.values.map {
            val regex = it.regex.toPattern()
            val list = it.list.map { combo ->
                val first = AttributeType.getByInternalName(combo[0])
                val second = AttributeType.getByInternalName(combo[1])
                first to second
            }
            GoodRollItem(regex, list)
        }
    }

    fun ItemStack.getAttributesLevels(): Pair<Attribute, Attribute>? {
        val attributes = getAttributes() ?: return null
        if (attributes.isEmpty()) return null
        return attributes.mapNotNull { (name, level) ->
            AttributeType.getByInternalNameOrNull(name)?.let { Attribute(it, level) }
        }.toPair()
    }

    /**
     * Assumes it's already not a Good Roll
     */
    fun AttributeType.isPartialRoll(internalName: NEUInternalName): Boolean {
        val rolls = goodRolls.find { it.regex.matches(internalName.asString()) } ?: return false
        return rolls.attributes.any { it.first == this || it.second == this }
    }

    fun Pair<Attribute, Attribute>.getRollType(internalName: NEUInternalName): RollType {
        val rolls = goodRolls.find { it.regex.matches(internalName.asString()) } ?: return RollType.BAD_ROLL
        val firstType = first.type
        val secondType = second.type
        val pair = firstType to secondType
        var partialRoll = false
        for (combination in rolls.attributes) {
            if (pair.equalsIgnoreOrder(combination)) return RollType.GOOD_ROLL
            val (attr1, attr2) = combination
            if (attr1 == firstType || attr1 == secondType || attr2 == firstType || attr2 == secondType) {
                partialRoll = true
            }
        }
        return if (partialRoll) RollType.PARTIAL_ROLL else RollType.BAD_ROLL
    }

    fun Pair<Attribute, Attribute>.isGoodRoll(internalName: NEUInternalName): Boolean =
        getRollType(internalName) == RollType.GOOD_ROLL

}
